package com.example.kwave.global.util;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeUtils {
    public static String getTimeAgo(LocalDateTime publishedAt) {
        Duration duration = Duration.between(publishedAt, LocalDateTime.now());
        if (duration.toDays() > 0) return duration.toDays() + "일 전";
        if (duration.toHours() > 0) return duration.toHours() + "시간 전";
        if (duration.toMinutes() > 0) return duration.toMinutes() + "분 전";
        return "방금 전";
    }
}