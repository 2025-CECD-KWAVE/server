package com.example.kwave.global.FFMPEG.dto;

import lombok.Data;

@Data
public class SubtitleSegment {
    public String text;
    public double start;
    public double end;

    public SubtitleSegment(String text, double start, double end) {
        this.text = text;
        this.start = start;
        this.end = end;
    }
}
