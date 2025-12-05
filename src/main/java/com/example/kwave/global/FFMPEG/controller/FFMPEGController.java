package com.example.kwave.global.FFMPEG.controller;

import com.example.kwave.global.FFMPEG.service.FFMPEGService;
import com.example.kwave.global.FFMPEG.service.FFMPEGTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/video")
@RequiredArgsConstructor
public class FFMPEGController {

    private final FFMPEGService ffmpegService;
    private final FFMPEGTestService ffmpegTestService;

    // 업로드 테스트 페이지
    @GetMapping("/merge-test")
    public String showMergeTestPage() {
        return "redirect:/merge-test.html";
    }

    // 실제 병합 + 다운로드
    @PostMapping(value = "/merge", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Resource> mergeAndDownload(@RequestParam("files") List<MultipartFile> files) {
        try {
            File merged = ffmpegTestService.mergeVideos(files);
            Resource resource = new FileSystemResource(merged);

            String filename = URLEncoder.encode("merged_output.mp4", StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("video/mp4"));
            headers.setContentDisposition(
                    ContentDisposition.attachment()
                            .filename(filename, StandardCharsets.UTF_8)
                            .build()
            );

            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PostMapping(value = "/merge-with-bgm", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Resource> mergeWithBgm(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("bgm") MultipartFile bgmFile
    ) {
        try {
            File merged = ffmpegTestService.mergeVideosWithBgmAndSubtitle(files, bgmFile);
            Resource resource = new FileSystemResource(merged);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("video/mp4"));
            headers.setContentDisposition(
                    ContentDisposition.attachment()
                            .filename("merged_with_bgm.mp4", StandardCharsets.UTF_8)
                            .build()
            );

            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
