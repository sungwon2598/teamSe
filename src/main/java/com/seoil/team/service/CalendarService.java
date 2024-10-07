package com.seoil.team.service;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.seoil.team.domain.member.Member;
import com.seoil.team.repository.MemberRepository;
import com.seoil.team.util.GoogleCalendarUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Service
public class CalendarService {
    private static final Logger logger = LoggerFactory.getLogger(CalendarService.class);

    private final MemberRepository memberRepository;
    private final OAuth2TokenRefreshService oAuth2TokenRefreshService;

    public CalendarService(MemberRepository memberRepository, OAuth2TokenRefreshService oAuth2TokenRefreshService) {
        this.memberRepository = memberRepository;
        this.oAuth2TokenRefreshService = oAuth2TokenRefreshService;
    }

    public List<Event> getEvents(String userEmail) throws IOException {
        String accessToken = getValidAccessToken(userEmail);
        //String accessToken = oAuth2TokenRefreshService.refreshAccessToken(userEmail);
        logger.info("Fetching events for user: {}", userEmail);
        Calendar service = GoogleCalendarUtil.getCalendarService(accessToken);
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = service.events().list("primary")
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();
        logger.info("Fetched {} events", items.size());
        return items;
    }

    public Event createEvent(String userEmail, Event event) throws IOException {
        String accessToken = getValidAccessToken(userEmail);
        logger.info("Creating new event for user: {}", userEmail);
        logger.debug("Event details: {}", event);
        Calendar service = GoogleCalendarUtil.getCalendarService(accessToken);
        Event createdEvent = service.events().insert("primary", event).execute();
        logger.info("Created event with ID: {}", createdEvent.getId());
        return createdEvent;
    }

    private String getValidAccessToken(String userEmail) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (isTokenExpired(member.getTokenExpirationTime())) {
            return oAuth2TokenRefreshService.refreshAccessToken(userEmail);
        }

        return member.getAccessToken();
    }

    private boolean isTokenExpired(Instant expirationTime) {
        return expirationTime != null && expirationTime.isBefore(Instant.now());
    }
}