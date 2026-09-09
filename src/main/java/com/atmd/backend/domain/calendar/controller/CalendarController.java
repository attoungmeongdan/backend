package com.atmd.backend.domain.calendar.controller;

import com.atmd.backend.domain.calendar.dto.response.CalendarResponseDTO;
import com.atmd.backend.domain.calendar.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/calendars")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping
    public ResponseEntity<CalendarResponseDTO> getMonthlyCalendar(
            @RequestParam int year,
            @RequestParam int month) {

        // TODO: 추후 Spring Security 등에서 로그인된 사용자의 ID를 가져오도록 수정
        Long currentUserId = 1L;

        CalendarResponseDTO response = calendarService.getMonthlyCalendar(currentUserId, year, month);
        return ResponseEntity.ok(response);
    }
}