package com.example.kwave.global.FFMPEG.service;

import com.example.kwave.domain.voicecloning.service.VoiceCloningService;
import com.example.kwave.global.FFMPEG.dto.SubtitleSegment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FFMPEGService {

    private final VoiceCloningService voiceCloningService;

    public File mergeVideosWithBgmAndGeneratedVoice(
            List<String> videoUrls,
            File bgmFile,
            String newsText,
            String voiceId
    ) throws Exception {

        if (videoUrls == null || videoUrls.isEmpty())
            throw new IllegalArgumentException("영상 URL이 없습니다.");

        if (bgmFile == null || !bgmFile.exists())
            throw new IllegalArgumentException("BGM 파일이 없습니다.");

        Path tempDir = Files.createTempDirectory("final-video-processor-");
        File mergedVideo = mergeVideosFromUrls(videoUrls, tempDir);

        List<String> sentences = splitSentences(newsText);

        List<File> voiceParts = new ArrayList<>();
        int index = 0;

        for (String sent : sentences) {
            byte[] bytes = voiceCloningService.VoiceCloning(voiceId, sent);
            File part = new File(tempDir.toFile(), "tts_" + index + ".mp3");
            Files.write(part.toPath(), bytes);
            voiceParts.add(part);
            index++;
        }

        List<SubtitleSegment> subtitleSegments = new ArrayList<>();
        double current = 0.0;

        for (int i = 0; i < voiceParts.size(); i++) {
            double duration = getAudioDuration(voiceParts.get(i));
            subtitleSegments.add(new SubtitleSegment(sentences.get(i), current, current + duration));
            current += duration;
        }

        File voiceFinal = concatAudioFiles(voiceParts, tempDir);
        String subtitleFilter = buildDrawTextFilter(subtitleSegments);

        File bgm = new File(tempDir.toFile(), "bgm.mp3");
        Files.copy(bgmFile.toPath(), bgm.toPath(), StandardCopyOption.REPLACE_EXISTING);

        File finalOutput = new File(tempDir.toFile(), "final_output.mp4");

        String videoFilter = subtitleFilter.isBlank()
                ? "null"
                : subtitleFilter;

        String scalePad = "scale=1920:1080:force_original_aspect_ratio=decrease,pad=1920:1080:(ow-iw)/2:(oh-ih)/2";

        String filter =
                "[0:v]" + scalePad + "," + videoFilter + "[v];" +
                        "[1:a]volume=1.3[a_tts];" +
                        "[2:a]volume=0.2[a_bgm];" +
                        "[a_tts][a_bgm]amix=inputs=2:duration=longest[a]";

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-stream_loop", "-1",
                "-i", mergedVideo.getAbsolutePath().replace("\\", "/"),
                "-i", voiceFinal.getAbsolutePath().replace("\\", "/"),
                "-i", bgm.getAbsolutePath().replace("\\", "/"),
                "-filter_complex", filter,
                "-map", "[v]",
                "-map", "[a]",
                "-t", String.valueOf(current),
                "-c:v", "libx264",
                "-c:a", "aac",
                finalOutput.getAbsolutePath().replace("\\", "/")
        );

        pb.redirectErrorStream(true);
        runAndLog(pb);
        cleanUpTempDirectory(tempDir, finalOutput);

        return finalOutput;
    }

    private File mergeVideosFromUrls(List<String> videoUrls, Path tempDir) throws Exception {

        File concatList = new File(tempDir.toFile(), "concat.txt");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(concatList))) {

            int idx = 0;

            for (String url : videoUrls) {

                File downloaded = new File(tempDir.toFile(), "video_" + idx + ".mp4");
                downloadFileFromUrl(url, downloaded);

                if (!downloaded.exists() || downloaded.length() < 20000 ||
                        Files.probeContentType(downloaded.toPath()).contains("text")) {
                    throw new RuntimeException("영상 다운로드 실패: " + url);
                }

                bw.write("file '" + downloaded.getAbsolutePath().replace("\\", "/") + "'\n");
                idx++;
            }
        }

        File merged = new File(tempDir.toFile(), "merged.mp4");

        String concatPath = concatList.getAbsolutePath().replace("\\", "/");
        String mergedPath = merged.getAbsolutePath().replace("\\", "/");

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-f", "concat",
                "-safe", "0",
                "-i", concatPath,
                "-c", "copy",
                mergedPath
        );

        pb.redirectErrorStream(true);
        runAndLog(pb);

        if (!merged.exists())
            throw new RuntimeException("URL 영상 병합 실패");

        return merged;
    }

    private List<String> splitSentences(String text) {
        return Arrays.stream(
                        text.split("(?<=[.!?])\\s+|(?<=[。！？])\\s*|(?<=\\?)\\s+")
                )
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private double getAudioDuration(File audioFile) throws Exception {

        ProcessBuilder pb = new ProcessBuilder(
                "ffprobe",
                "-i", audioFile.getAbsolutePath(),
                "-show_entries", "format=duration",
                "-v", "quiet",
                "-of", "csv=p=0"
        );

        Process proc = pb.start();
        String out = new String(proc.getInputStream().readAllBytes()).trim();
        proc.waitFor();

        if (out.isBlank()) return 0.0;
        return Double.parseDouble(out);
    }

    private void cleanUpTempDirectory(Path tempDir, File finalOutput) {
        try {
            Files.list(tempDir)
                    .filter(path -> !path.toFile().equals(finalOutput)) // final_output.mp4 제외
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {}
                    });
        } catch (IOException ignored) {}
    }


    private File concatAudioFiles(List<File> audioFiles, Path tempDir)
            throws Exception {

        File concatList = new File(tempDir.toFile(), "audio_concat.txt");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(concatList))) {
            for (File f : audioFiles) {
                bw.write("file '" + f.getAbsolutePath().replace("\\", "/") + "'\n");
            }
        }

        File output = new File(tempDir.toFile(), "tts_final.mp3");

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-f", "concat",
                "-safe", "0",
                "-i", concatList.getAbsolutePath(),
                "-c", "copy",
                output.getAbsolutePath()
        );

        pb.redirectErrorStream(true);
        runAndLog(pb);

        return output;
    }

    private String buildDrawTextFilter(List<SubtitleSegment> segments) {

        if (segments.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        int maxCharsPerLine = 40;
        int fontSize = 40;
        int lineSpacing = 10;

        for (SubtitleSegment seg : segments) {

            String wrapped = wrapText(seg.text, maxCharsPerLine);
            String[] lines = wrapped.split("\n");

            int lineCount = lines.length;
            int totalHeight = (fontSize + lineSpacing) * (lineCount - 1);

            for (int i = 0; i < lineCount; i++) {

                String line = lines[i]
                        .replace(":", "\\:")
                        .replace("'", "\\'")
                        .replace("\"", "\\\"")
                        .replace("'", "")
                        .replace(",", "\\,");

                int yOffset = 100 + (totalHeight - (i * (fontSize + lineSpacing)));

                String f = String.format(
                        "drawtext=text='%s':fontcolor=white:fontsize=%d:"
                                + "x=(w-text_w)/2:y=h-%d:"
                                + "box=1:boxcolor=black@0.5:boxborderw=10:"
                                + "enable='between(t\\,%f\\,%f)'",
                        line, fontSize, yOffset, seg.start, seg.end
                );

                if (sb.length() > 0) sb.append(",");
                sb.append(f);
            }
        }

        return sb.toString();
    }


    private String wrapText(String text, int maxCharsPerLine) {

        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        StringBuilder result = new StringBuilder();

        for (String word : words) {

            if (line.length() == 0) {
                line.append(word);
            } else if (line.length() + 1 + word.length() <= maxCharsPerLine) {
                line.append(" ").append(word);
            } else {
                result.append(line).append("\n");
                line = new StringBuilder(word);
            }
        }

        if (line.length() > 0) {
            result.append(line);
        }

        return result.toString();
    }

    private void runAndLog(ProcessBuilder pb) throws Exception {

        Process p = pb.start();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null)
                System.out.println("[ffmpeg] " + line);
        }
        int exit = p.waitFor();
        if (exit != 0)
            throw new RuntimeException("FFmpeg 실패, exit=" + exit);
    }

    private void downloadFileFromUrl(String urlString, File dest) throws Exception {

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);

        try (InputStream in = conn.getInputStream()) {
            Files.copy(in, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
