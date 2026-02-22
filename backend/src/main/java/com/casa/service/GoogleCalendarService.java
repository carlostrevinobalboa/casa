package com.casa.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import com.casa.domain.calendar.CalendarEvent;
import com.casa.domain.calendar.CalendarRecurrenceFrequency;
import com.casa.domain.integrations.GoogleCalendarLink;
import com.casa.repository.GoogleCalendarLinkRepository;
import org.springframework.http.HttpStatus;

@Service
public class GoogleCalendarService {

    private static final String CALENDAR_NAME = "casaChiquitos";
    private static final String GOOGLE_CALENDAR_BASE = "https://www.googleapis.com/calendar/v3";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final DateTimeFormatter RRULE_UNTIL_FORMATTER =
        DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

    private final GoogleCalendarLinkRepository googleCalendarLinkRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    public GoogleCalendarService(GoogleCalendarLinkRepository googleCalendarLinkRepository) {
        this.googleCalendarLinkRepository = googleCalendarLinkRepository;
    }

    @Transactional
    public void storeTokens(UUID userId, String accessToken, OffsetDateTime expiresAt, String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        GoogleCalendarLink link = googleCalendarLinkRepository.findByUserId(userId).orElseGet(() -> {
            GoogleCalendarLink created = new GoogleCalendarLink();
            created.setId(UUID.randomUUID());
            created.setUserId(userId);
            created.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            return created;
        });

        link.setAccessToken(accessToken);
        link.setAccessTokenExpiresAt(expiresAt);
        link.setRefreshToken(refreshToken);
        link.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        googleCalendarLinkRepository.save(link);
    }

    @Transactional(readOnly = true)
    public Optional<GoogleCalendarLink> findLink(UUID userId) {
        return googleCalendarLinkRepository.findByUserId(userId);
    }

    @Transactional
    public String ensureCalendar(UUID userId) {
        GoogleCalendarLink link = googleCalendarLinkRepository.findByUserId(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Google Calendar no conectado"));

        if (link.getCalendarId() != null && !link.getCalendarId().isBlank()) {
            return link.getCalendarId();
        }

        String token = ensureAccessToken(link);
        HttpHeaders headers = bearerHeaders(token);

        ResponseEntity<Map> response = restTemplate.exchange(
            GOOGLE_CALENDAR_BASE + "/users/me/calendarList",
            org.springframework.http.HttpMethod.GET,
            new HttpEntity<>(headers),
            Map.class
        );

        Object items = response.getBody() == null ? null : response.getBody().get("items");
        if (items instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    Object summary = map.get("summary");
                    Object id = map.get("id");
                    if (CALENDAR_NAME.equals(summary) && id instanceof String idString) {
                        link.setCalendarId(idString);
                        link.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
                        googleCalendarLinkRepository.save(link);
                        return idString;
                    }
                }
            }
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("summary", CALENDAR_NAME);
        ResponseEntity<Map> created = restTemplate.postForEntity(
            GOOGLE_CALENDAR_BASE + "/calendars",
            new HttpEntity<>(payload, headers),
            Map.class
        );

        String calendarId = created.getBody() == null ? null : (String) created.getBody().get("id");
        if (calendarId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo crear el calendario en Google");
        }

        link.setCalendarId(calendarId);
        link.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        googleCalendarLinkRepository.save(link);
        return calendarId;
    }

    @Transactional
    public String upsertEvent(UUID userId, CalendarEvent event) {
        GoogleCalendarLink link = googleCalendarLinkRepository.findByUserId(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Google Calendar no conectado"));

        String calendarId = ensureCalendar(userId);
        String token = ensureAccessToken(link);
        HttpHeaders headers = bearerHeaders(token);

        Map<String, Object> payload = mapEvent(event);
        String url = GOOGLE_CALENDAR_BASE + "/calendars/" + calendarId + "/events";

        if (event.getGoogleEventId() == null || event.getGoogleEventId().isBlank()) {
            ResponseEntity<Map> created = restTemplate.postForEntity(
                url,
                new HttpEntity<>(payload, headers),
                Map.class
            );
            return created.getBody() == null ? null : (String) created.getBody().get("id");
        }

        restTemplate.put(
            url + "/" + event.getGoogleEventId(),
            new HttpEntity<>(payload, headers)
        );
        return event.getGoogleEventId();
    }

    @Transactional
    public void deleteEvent(UUID userId, CalendarEvent event) {
        if (event.getGoogleEventId() == null || event.getGoogleEventId().isBlank()) {
            return;
        }

        GoogleCalendarLink link = googleCalendarLinkRepository.findByUserId(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Google Calendar no conectado"));

        String calendarId = ensureCalendar(userId);
        String token = ensureAccessToken(link);
        HttpHeaders headers = bearerHeaders(token);

        restTemplate.exchange(
            GOOGLE_CALENDAR_BASE + "/calendars/" + calendarId + "/events/" + event.getGoogleEventId(),
            org.springframework.http.HttpMethod.DELETE,
            new HttpEntity<>(headers),
            Void.class
        );
    }

    @Transactional
    public void refreshFromGoogle(UUID userId, CalendarImportHandler handler) {
        GoogleCalendarLink link = googleCalendarLinkRepository.findByUserId(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Google Calendar no conectado"));

        String calendarId = ensureCalendar(userId);
        String token = ensureAccessToken(link);
        HttpHeaders headers = bearerHeaders(token);

        String url = GOOGLE_CALENDAR_BASE + "/calendars/" + calendarId + "/events?singleEvents=true&orderBy=startTime";

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            org.springframework.http.HttpMethod.GET,
            new HttpEntity<>(headers),
            Map.class
        );

        Object items = response.getBody() == null ? null : response.getBody().get("items");
        if (items instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    handler.handleGoogleEvent(map);
                }
            }
        }
    }

    private String ensureAccessToken(GoogleCalendarLink link) {
        if (link.getAccessToken() != null && link.getAccessTokenExpiresAt() != null) {
            if (link.getAccessTokenExpiresAt().isAfter(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(2))) {
                return link.getAccessToken();
            }
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("refresh_token", link.getRefreshToken());
        form.add("grant_type", "refresh_token");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<Map> response = restTemplate.postForEntity(
            GOOGLE_TOKEN_URL,
            new HttpEntity<>(form, headers),
            Map.class
        );

        String accessToken = response.getBody() == null ? null : (String) response.getBody().get("access_token");
        Number expiresIn = response.getBody() == null ? null : (Number) response.getBody().get("expires_in");

        if (accessToken == null || expiresIn == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo refrescar el token de Google");
        }

        link.setAccessToken(accessToken);
        link.setAccessTokenExpiresAt(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(expiresIn.longValue()));
        link.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        googleCalendarLinkRepository.save(link);
        return accessToken;
    }

    private HttpHeaders bearerHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private Map<String, Object> mapEvent(CalendarEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("summary", event.getTitle());
        payload.put("description", event.getDescription());

        Map<String, Object> start = new HashMap<>();
        Map<String, Object> end = new HashMap<>();
        if (event.isAllDay()) {
            start.put("date", event.getStartAt().toLocalDate().toString());
            end.put("date", event.getEndAt().toLocalDate().toString());
        } else {
            start.put("dateTime", event.getStartAt().toString());
            end.put("dateTime", event.getEndAt().toString());
        }
        payload.put("start", start);
        payload.put("end", end);

        if (event.getRecurrenceFrequency() != null && event.getRecurrenceFrequency() != CalendarRecurrenceFrequency.NONE) {
            String rule = recurrenceRule(event);
            if (rule != null) {
                payload.put("recurrence", List.of(rule));
            }
        }

        return payload;
    }

    private String recurrenceRule(CalendarEvent event) {
        CalendarRecurrenceFrequency freq = event.getRecurrenceFrequency();
        if (freq == null || freq == CalendarRecurrenceFrequency.NONE) {
            return null;
        }

        StringBuilder rule = new StringBuilder("RRULE:FREQ=");
        rule.append(freq.name());
        rule.append(";INTERVAL=");
        rule.append(Math.max(1, event.getRecurrenceInterval()));

        if (event.getRecurrenceCount() != null) {
            rule.append(";COUNT=").append(event.getRecurrenceCount());
        } else if (event.getRecurrenceUntil() != null) {
            String until = RRULE_UNTIL_FORMATTER.format(event.getRecurrenceUntil().toInstant());
            rule.append(";UNTIL=").append(until);
        }

        return rule.toString();
    }

    public interface CalendarImportHandler {
        void handleGoogleEvent(Map<?, ?> googleEvent);
    }
}
