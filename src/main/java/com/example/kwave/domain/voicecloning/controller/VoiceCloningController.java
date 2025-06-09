package com.example.kwave.domain.voicecloning.controller;

import com.example.kwave.domain.voicecloning.dto.VoiceCloningRequest;
import com.example.kwave.domain.voicecloning.service.VoiceCloningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/voice")
public class VoiceCloningController {

    private final VoiceCloningService voiceCloningService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "audio/wav")
    public ResponseEntity<StreamingResponseBody> streamVoice(@RequestBody VoiceCloningRequest request) {
        StreamingResponseBody stream = outputStream -> {
            voiceCloningService.streamAsWav(request.language(), request.text(), outputStream);
        };

        return ResponseEntity.ok()
                .header("Transfer-Encoding", "chunked")
                .contentType(MediaType.valueOf("audio/wav"))
                .body(stream);
    }
}
