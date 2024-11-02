package com.seoil.team.util;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoogleCalendarUtil {
    private static final Logger logger = LoggerFactory.getLogger(GoogleCalendarUtil.class);

    public static Calendar getCalendarService(String accessToken) {
        logger.info("Creating Calendar service with access token: {}", accessToken);
        Credential credential = new GoogleCredential().setAccessToken(accessToken);
        Calendar service = new Calendar.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("Your Application Name")
                .build();
        logger.info("Calendar service created successfully");
        return service;
    }
}