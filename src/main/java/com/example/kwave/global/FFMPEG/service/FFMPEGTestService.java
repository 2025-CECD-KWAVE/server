package com.example.kwave.global.FFMPEG.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class FFMPEGTestService {
    public File mergeVideos(List<MultipartFile> files) throws IOException, InterruptedException {
        if (files == null || files.size() == 0) {
            throw new IllegalArgumentException("업로드된 파일이 없습니다.");
        }

        // 임시 디렉토리 생성 (요청마다 유니크하게)
        Path tempDir = Files.createTempDirectory("video-merge-");
        File concatFile = new File(tempDir.toFile(), "concat.txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(concatFile))) {
            int index = 0;
            for (MultipartFile mf : files) {
                if (mf.isEmpty()) continue;

                String ext = getExtensionOrDefault(mf.getOriginalFilename(), ".mp4");
                File saved = new File(tempDir.toFile(), "input_" + (index++) + ext);

                try (InputStream in = mf.getInputStream()) {
                    Files.copy(in, saved.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                // concat.txt에 경로 기록
                writer.write("file '" + saved.getAbsolutePath().replace("\\", "/") + "'\n");
            }
        }

        File output = new File(tempDir.toFile(), "merged_output.mp4");

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-f", "concat",
                "-safe", "0",
                "-i", concatFile.getAbsolutePath(),
                "-c", "copy",
                output.getAbsolutePath()
        );
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // FFmpeg 로그 찍기 (원하면 Logger로 변경)
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println("[ffmpeg] " + line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0 || !output.exists()) {
            throw new RuntimeException("FFmpeg 병합 실패, exitCode=" + exitCode);
        }

        return output;
    }

    private String getExtensionOrDefault(String filename, String def) {
        if (filename == null) return def;
        int dot = filename.lastIndexOf('.');
        if (dot == -1 || dot == filename.length() - 1) return def;
        return filename.substring(dot);
    }

    public File mergeVideosWithBgmAndSubtitle(List<MultipartFile> files,
                                              MultipartFile bgmFile)
            throws IOException, InterruptedException {

        if (bgmFile == null || bgmFile.isEmpty()) {
            throw new IllegalArgumentException("BGM 파일이 없습니다.");
        }
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("업로드된 영상 파일이 없습니다.");
        }

        Path tempDir = Files.createTempDirectory("video-merge-bgm-sub-");

        // 1) 먼저 영상 병합
        File concatFile = new File(tempDir.toFile(), "concat.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(concatFile))) {
            int index = 0;
            for (MultipartFile mf : files) {
                if (mf.isEmpty()) continue;

                String ext = getExtensionOrDefault(mf.getOriginalFilename(), ".mp4");
                File saved = new File(tempDir.toFile(), "input_" + (index++) + ext);

                try (InputStream in = mf.getInputStream()) {
                    Files.copy(in, saved.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                writer.write("file '" + saved.getAbsolutePath().replace("\\", "/") + "'\n");
            }
        }

        File mergedVideo = new File(tempDir.toFile(), "merged_output.mp4");

        ProcessBuilder mergePb = new ProcessBuilder(
                "ffmpeg",
                "-f", "concat",
                "-safe", "0",
                "-i", concatFile.getAbsolutePath(),
                "-c", "copy",
                mergedVideo.getAbsolutePath()
        );
        mergePb.redirectErrorStream(true);
        runAndLog(mergePb);

        if (!mergedVideo.exists()) {
            throw new RuntimeException("FFmpeg 영상 병합 실패 (중간 영상 없음)");
        }

        // 2) BGM 저장
        String bgmExt = getExtensionOrDefault(bgmFile.getOriginalFilename(), ".mp3");
        File bgm = new File(tempDir.toFile(), "bgm" + bgmExt);
        bgmFile.transferTo(bgm);

        // 3) BGM + 자막 합성
        //   텍스트: "안녕하세요 테스트입니다"
        //   위치: 화면 중앙
        //   box=1 로 검은 반투명 배경 박스
        //   enable='lte(t,3)' → t(초)가 3 이하일 때만 보이게 = 첫 3초
        String drawTextFilter =
                "drawtext=text='안녕하세요 테스트입니다':" +
                        "fontcolor=white:fontsize=40:" +
                        "x=(w-text_w)/2:y=(h-text_h)/2:" +
                        "box=1:boxcolor=black@0.5:boxborderw=10:" +
                        "enable='lte(t,3)'";

        File finalOutput = new File(tempDir.toFile(), "merged_with_bgm_subtitle.mp4");

        ProcessBuilder bgmSubPb = new ProcessBuilder(
                "ffmpeg",
                "-i", mergedVideo.getAbsolutePath(),   // 0:v,0:a (영상)
                "-i", bgm.getAbsolutePath(),           // 1:a (배경음악)
                "-filter:v", drawTextFilter,           // 자막 필터
                "-map", "0:v",                         // 영상은 0번 입력
                "-map", "1:a",                         // 오디오는 bgm(1번 입력)
                "-c:v", "libx264",                     // drawtext를 쓰려면 영상은 재인코딩 필요
                "-c:a", "aac",
                "-shortest",
                finalOutput.getAbsolutePath()
        );
        bgmSubPb.redirectErrorStream(true);
        runAndLog(bgmSubPb);

        if (!finalOutput.exists()) {
            throw new RuntimeException("FFmpeg BGM+자막 합성 실패 (최종 출력 없음)");
        }

        return finalOutput;
    }

    private void runAndLog(ProcessBuilder pb) throws IOException, InterruptedException {
        Process process = pb.start();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println("[ffmpeg] " + line);
            }
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg 실행 실패, exitCode=" + exitCode);
        }
    }

}
