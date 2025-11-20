package com.example.kwave.domain.voicecloning.controller;

import com.example.kwave.domain.voicecloning.dto.VoiceCloningRequestDto;
import com.example.kwave.domain.voicecloning.service.VoiceCloningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/voice")
@Tag(name = "Voice Cloning", description = "ElevenLabs API")
public class VoiceCloningController {

    private final VoiceCloningService voicecloningService;

    @PostMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE, 
        produces = "audio/mpeg"
    )
    @Operation(summary = "보이스 클로닝 mp3 생성", description = "텍스트와 Voice ID를 받아 ElevenLabs MP3 음성 반환")
    public ResponseEntity<byte[]> generateAudio(@RequestBody VoiceCloningRequestDto request) {
        
        // 1. VoiceCloningService 호출 -> MP3 파일 데이터를 byte[]로 받음
        byte[] audioData = voicecloningService.VoiceCloning(
            request.getVoiceId(),
            request.getText()
        );

        // 2. 스트리밍(chunked) 대신, 전체 파일(byte[])을 응답
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("audio/mpeg"));
        headers.setContentLength(audioData.length); // (파일 전체 크기 명시)

        // 3. byte[] 데이터와 헤더, 상태 코드를 반환
        return new ResponseEntity<>(audioData, headers, HttpStatus.OK);
    }
}
/*
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
*/