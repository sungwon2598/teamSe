package com.seoil.team.controller;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.seoil.team.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/calendar")
@RequiredArgsConstructor
public class CalendarController {
    private static final Logger logger = LoggerFactory.getLogger(CalendarController.class);

    private final CalendarService calendarService;

    @GetMapping
    public String getCalendarPage(Model model) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        logger.info("Fetching calendar page for user: {}", userEmail);

        List<Event> events = calendarService.getEvents(userEmail);
        model.addAttribute("events", events);
        return "calendar/index";
    }

    @GetMapping("/create")
    public String getCreateEventPage(Model model) {
        logger.info("Accessing create event page");
        model.addAttribute("event", new Event());
        return "calendar/create";
    }

    @PostMapping("/create")
    public String createEvent(@RequestParam("summary") String summary,
                              @RequestParam("description") String description,
                              @RequestParam("start.dateTime") String startDateTime,
                              @RequestParam("end.dateTime") String endDateTime) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        logger.info("Creating new event for user: {}", userEmail);

        Event event = new Event()
                .setSummary(summary)
                .setDescription(description);

        DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        ZoneId zoneId = ZoneId.systemDefault();

        LocalDateTime startLDT = LocalDateTime.parse(startDateTime, inputFormatter);
        LocalDateTime endLDT = LocalDateTime.parse(endDateTime, inputFormatter);

        String startRfc3339 = startLDT.atZone(zoneId).format(outputFormatter);
        String endRfc3339 = endLDT.atZone(zoneId).format(outputFormatter);

        logger.debug("Converted start time: {}", startRfc3339);
        logger.debug("Converted end time: {}", endRfc3339);

        EventDateTime start = new EventDateTime().setDateTime(new DateTime(startRfc3339));
        EventDateTime end = new EventDateTime().setDateTime(new DateTime(endRfc3339));

        event.setStart(start);
        event.setEnd(end);

        Event createdEvent = calendarService.createEvent(userEmail, event);
        logger.info("Created event with ID: {}", createdEvent.getId());
        return "redirect:/calendar";
    }
}