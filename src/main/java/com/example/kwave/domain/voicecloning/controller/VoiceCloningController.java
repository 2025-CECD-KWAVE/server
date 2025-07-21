package com.example.kwave.domain.voicecloning.controller;

import com.example.kwave.domain.voicecloning.dto.VoiceCloningRequest;
import com.example.kwave.domain.voicecloning.service.VoiceCloningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Voice Cloning", description = "보이스클로닝 API")
public class VoiceCloningController {

    private final VoiceCloningService voiceCloningService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "audio/wav")
    @Operation(summary = "보이스 클로닝 요청", description = "텍스트와 언어를 요청 받아 보이스 클로닝 서버로 전달 후 WAV 음성 반환")
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
