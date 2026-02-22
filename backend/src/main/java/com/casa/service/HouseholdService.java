package com.casa.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.casa.api.dto.household.HouseholdMemberResponse;
import com.casa.domain.household.Household;
import com.casa.domain.household.HouseholdMember;
import com.casa.domain.household.HouseholdRole;
import com.casa.domain.identity.UserAccount;
import com.casa.repository.HouseholdMemberRepository;
import com.casa.repository.HouseholdRepository;
import com.casa.repository.UserAccountRepository;
import com.casa.service.support.InviteCodeGenerator;
import com.casa.service.support.MemberColorPicker;

@Service
public class HouseholdService {

    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final UserAccountRepository userAccountRepository;
    private final InviteCodeGenerator inviteCodeGenerator;
    private final MemberColorPicker memberColorPicker;

    public HouseholdService(
        HouseholdRepository householdRepository,
        HouseholdMemberRepository householdMemberRepository,
        UserAccountRepository userAccountRepository,
        InviteCodeGenerator inviteCodeGenerator,
        MemberColorPicker memberColorPicker
    ) {
        this.householdRepository = householdRepository;
        this.householdMemberRepository = householdMemberRepository;
        this.userAccountRepository = userAccountRepository;
        this.inviteCodeGenerator = inviteCodeGenerator;
        this.memberColorPicker = memberColorPicker;
    }

    @Transactional
    public Household createHousehold(UUID ownerUserId, String householdName) {
        UserAccount owner = userAccountRepository.findById(ownerUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        return createHousehold(owner, householdName);
    }

    @Transactional
    public Household createHousehold(UserAccount owner, String householdName) {
        Household household = new Household();
        household.setName(householdName.trim());
        household.setCreatedByUserId(owner.getId());
        household.setInviteCode(nextUniqueInviteCode());
        householdRepository.save(household);

        HouseholdMember member = new HouseholdMember();
        member.setHousehold(household);
        member.setUser(owner);
        member.setRole(HouseholdRole.OWNER);
        member.setColorHex(memberColorPicker.pick(owner.getId()));
        householdMemberRepository.save(member);

        return household;
    }

    @Transactional
    public Household joinHousehold(UUID userId, String inviteCode) {
        String normalizedInviteCode = inviteCode.trim().toUpperCase(Locale.ROOT);

        Household household = householdRepository.findByInviteCodeIgnoreCase(normalizedInviteCode)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Codigo de invitacion no valido"));

        if (householdMemberRepository.existsByHouseholdIdAndUserId(household.getId(), userId)) {
            return household;
        }

        UserAccount user = userAccountRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        HouseholdMember newMember = new HouseholdMember();
        newMember.setHousehold(household);
        newMember.setUser(user);
        newMember.setRole(HouseholdRole.MEMBER);
        newMember.setColorHex(memberColorPicker.pick(user.getId()));
        householdMemberRepository.save(newMember);

        return household;
    }

    @Transactional(readOnly = true)
    public List<HouseholdMember> listMemberships(UUID userId) {
        return householdMemberRepository.findByUserIdOrderByCreatedAtAsc(userId);
    }

    @Transactional(readOnly = true)
    public List<HouseholdMemberResponse> listMembers(UUID userId, UUID householdId) {
        requireMembership(userId, householdId);

        return householdMemberRepository.findByHouseholdId(householdId)
            .stream()
            .map(member -> new HouseholdMemberResponse(
                member.getUser().getId(),
                member.getUser().getDisplayName(),
                member.getRole(),
                member.getColorHex()
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public void requireMembership(UUID userId, UUID householdId) {
        if (!householdMemberRepository.existsByHouseholdIdAndUserId(householdId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No perteneces a este hogar");
        }
    }

    @Transactional(readOnly = true)
    public void requireAdminOrOwner(UUID userId, UUID householdId) {
        HouseholdMember member = householdMemberRepository.findByHouseholdIdAndUserId(householdId, userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No perteneces a este hogar"));

        if (member.getRole() != HouseholdRole.OWNER && member.getRole() != HouseholdRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos de administracion");
        }
    }

    @Transactional(readOnly = true)
    public List<UUID> householdUserIds(UUID householdId) {
        return householdMemberRepository.findByHouseholdId(householdId)
            .stream()
            .map(member -> member.getUser().getId())
            .toList();
    }

    private String nextUniqueInviteCode() {
        for (int i = 0; i < 20; i++) {
            String candidate = inviteCodeGenerator.nextCode();
            if (!householdRepository.existsByInviteCodeIgnoreCase(candidate)) {
                return candidate;
            }
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo generar codigo unico");
    }
}
