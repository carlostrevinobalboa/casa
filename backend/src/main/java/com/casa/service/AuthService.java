package com.casa.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.casa.api.dto.auth.AuthRequest;
import com.casa.api.dto.auth.AuthResponse;
import com.casa.api.dto.auth.HouseholdSummary;
import com.casa.api.dto.auth.MeResponse;
import com.casa.api.dto.auth.RegisterRequest;
import com.casa.api.dto.auth.UserSummary;
import com.casa.domain.household.Household;
import com.casa.domain.household.HouseholdMember;
import com.casa.domain.identity.AuthProvider;
import com.casa.domain.identity.UserAccount;
import com.casa.repository.HouseholdMemberRepository;
import com.casa.repository.UserAccountRepository;
import com.casa.security.JwtTokenService;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final HouseholdService householdService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(
        UserAccountRepository userAccountRepository,
        HouseholdMemberRepository householdMemberRepository,
        HouseholdService householdService,
        PasswordEncoder passwordEncoder,
        JwtTokenService jwtTokenService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.householdMemberRepository = householdMemberRepository;
        this.householdService = householdService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userAccountRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese email ya esta registrado");
        }

        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setDisplayName(request.displayName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setProvider(AuthProvider.LOCAL);
        userAccountRepository.save(user);

        Household household = householdService.createHousehold(user, request.householdName());

        return buildAuthResponse(user, household.getId());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        String email = normalizeEmail(request.email());

        UserAccount user = userAccountRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas"));

        if (user.getProvider() == AuthProvider.GOOGLE) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Este usuario debe entrar con Google");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas");
        }

        UUID activeHouseholdId = firstHouseholdId(user.getId());

        return buildAuthResponse(user, activeHouseholdId);
    }

    @Transactional
    public AuthResponse loginWithGoogle(String rawEmail, String displayName) {
        String email = normalizeEmail(rawEmail);

        UserAccount user = userAccountRepository.findByEmailIgnoreCase(email)
            .orElseGet(() -> {
                UserAccount created = new UserAccount();
                created.setEmail(email);
                created.setDisplayName(displayName.trim());
                created.setPasswordHash("GOOGLE_OAUTH2");
                created.setProvider(AuthProvider.GOOGLE);
                return userAccountRepository.save(created);
            });

        if (user.getDisplayName() == null || user.getDisplayName().isBlank()) {
            user.setDisplayName(displayName.trim());
        }

        if (user.getProvider() == null) {
            user.setProvider(AuthProvider.GOOGLE);
        }

        UUID activeHouseholdId = firstHouseholdId(user.getId());
        if (activeHouseholdId == null) {
            Household household = householdService.createHousehold(user, "Hogar de " + user.getDisplayName());
            activeHouseholdId = household.getId();
        }

        return buildAuthResponse(user, activeHouseholdId);
    }

    @Transactional(readOnly = true)
    public MeResponse me(UUID userId) {
        UserAccount user = userAccountRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        List<HouseholdSummary> households = toHouseholdSummaries(userId);
        UUID activeHouseholdId = households.isEmpty() ? null : households.get(0).id();

        return new MeResponse(
            new UserSummary(user.getId(), user.getEmail(), user.getDisplayName()),
            households,
            activeHouseholdId
        );
    }

    private AuthResponse buildAuthResponse(UserAccount user, UUID activeHouseholdId) {
        List<HouseholdSummary> households = toHouseholdSummaries(user.getId());

        return new AuthResponse(
            jwtTokenService.generateToken(user),
            "Bearer",
            new UserSummary(user.getId(), user.getEmail(), user.getDisplayName()),
            households,
            activeHouseholdId
        );
    }

    private List<HouseholdSummary> toHouseholdSummaries(UUID userId) {
        return householdMemberRepository.findByUserIdOrderByCreatedAtAsc(userId)
            .stream()
            .map(member -> new HouseholdSummary(
                member.getHousehold().getId(),
                member.getHousehold().getName(),
                member.getHousehold().getInviteCode(),
                member.getRole(),
                member.getColorHex()
            ))
            .toList();
    }

    private UUID firstHouseholdId(UUID userId) {
        return householdMemberRepository.findByUserIdOrderByCreatedAtAsc(userId)
            .stream()
            .map(HouseholdMember::getHousehold)
            .map(Household::getId)
            .findFirst()
            .orElse(null);
    }

    private String normalizeEmail(String rawEmail) {
        return rawEmail.trim().toLowerCase(Locale.ROOT);
    }
}
