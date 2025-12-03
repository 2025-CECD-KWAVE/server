package com.example.kwave.domain.video.schedule;

import com.example.kwave.domain.news.service.NewsService;
import com.example.kwave.domain.video.service.NewsVideoService;
import com.example.kwave.domain.voicecloning.service.VoiceCloningService;
import com.example.kwave.global.FFMPEG.service.FFMPEGService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class NewsVideoBatch {

    private final NewsVideoService newsVideoService;
    private final FFMPEGService ffmpegService;
    private final VoiceCloningService voiceCloningService;
    private final NewsService newsService;

//    @PostConstruct
//    public void init() {
//        // 1분 후 실행
//        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
//            try {
//                createNewsVideos();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }, 0, TimeUnit.SECONDS);
//
//        System.out.println("테스트 스케줄 등록 완료");
//    }
//
//    @Scheduled(cron = "0 0 3 * * *")
//    public void createNewsVideos() throws Exception{
//        String newsId = "01100201.20251024000935002";
//        ClassPathResource bgmResource = new ClassPathResource("static/bgm.mp3");
//        String voiceId = "eyllFuqDDENgrMbwK2Jd";
//        File bgmFile = bgmResource.getFile();
//
//        // newsVideoService.getScenesFromNews(newsId);
//        // List<String> videoUrls = newsVideoService.generateVideoFromNews(newsId);
//        List<String> videoUrls = new ArrayList<>();
//        videoUrls.add("https://dnznrvs05pmza.cloudfront.net/veo3.1/projects/vertex-ai-claude-431722/locations/us-central1/publishers/google/models/veo-3.1-fast-generate-preview/operations/836ead9a-a113-403c-a30c-0bf2278bdca6/Capture_the_exterior_of_a_national_theater_with_dramatic_lighting__focusing_on_the_architecture__Fra.mp4?_jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJrZXlIYXNoIjoiM2QxODNkNzEyMWJiMjZhZiIsImJ1Y2tldCI6InJ1bndheS10YXNrLWFydGlmYWN0cyIsInN0YWdlIjoicHJvZCIsImV4cCI6MTc2NDI4ODAwMH0.bRmiZ_58TT95ydZ7mbwlBg2J50lCtoi_z6ytCM9YxiA");
//        videoUrls.add("https://dnznrvs05pmza.cloudfront.net/veo3.1/projects/vertex-ai-claude-431722/locations/us-central1/publishers/google/models/veo-3.1-fast-generate-preview/operations/86018f3c-b9a6-4ba9-a5f2-f5df85a6ee90/Zoom_in_on_an_ornate_cultural_medal__highlighting_its_intricate_design_and_shine__Ensure_a_vertical_.mp4?_jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJrZXlIYXNoIjoiNTNiZjEwMDdiNDYyMjRiNCIsImJ1Y2tldCI6InJ1bndheS10YXNrLWFydGlmYWN0cyIsInN0YWdlIjoicHJvZCIsImV4cCI6MTc2NDI4ODAwMH0.qK3QwBH9LKYuKc7Wacf5jZexKZe21NKEyJDw-uPoYq4");
//        videoUrls.add("https://dnznrvs05pmza.cloudfront.net/veo3.1/projects/vertex-ai-claude-431722/locations/us-central1/publishers/google/models/veo-3.1-fast-generate-preview/operations/e17425a6-ecb8-4e3c-a31e-8074348b2ca8/Show_the_stage_setup_for_an_awards_ceremony__with_elegant_decorations_and_ambient_lighting__Use_a_9_.mp4?_jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJrZXlIYXNoIjoiNDdjMDI5Y2RiOThjZGY2OCIsImJ1Y2tldCI6InJ1bndheS10YXNrLWFydGlmYWN0cyIsInN0YWdlIjoicHJvZCIsImV4cCI6MTc2NDI4ODAwMH0.2Or8W7XDi2FH3M4QFSlC4-YvteFbNQvt5ZNHVMFIoGk");
//        videoUrls.add("https://dnznrvs05pmza.cloudfront.net/veo3.1/projects/vertex-ai-claude-431722/locations/us-central1/publishers/google/models/veo-3.1-fast-generate-preview/operations/606b0d50-54a0-42dc-bcb2-ff27afdb02b7/Focus_on_an_elegant_table_adorned_with_awards_and_trophies__with_soft_lighting_creating_a_warm_atmos.mp4?_jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJrZXlIYXNoIjoiNTRlMzhlZDU4MjdkY2IzZSIsImJ1Y2tldCI6InJ1bndheS10YXNrLWFydGlmYWN0cyIsInN0YWdlIjoicHJvZCIsImV4cCI6MTc2NDI4ODAwMH0.m9xLvZSSuRU9g2jLkNeaajMPOyJx0VEKF8eFrPTz-7o");
//        videoUrls.add("https://dnznrvs05pmza.cloudfront.net/veo3.1/projects/vertex-ai-claude-431722/locations/us-central1/publishers/google/models/veo-3.1-fast-generate-preview/operations/de7c27a4-3872-4d63-ad86-4b176a8d6a07/Display_a_large_screen_with_a_list_of_award_recipients__softly_illuminated_in_the_background__Ensure.mp4?_jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJrZXlIYXNoIjoiMTExYjAwNWFmMzBhOGYzNSIsImJ1Y2tldCI6InJ1bndheS10YXNrLWFydGlmYWN0cyIsInN0YWdlIjoicHJvZCIsImV4cCI6MTc2NDI4ODAwMH0.P_0aUJN3BPJ3dE-a8iQO_Re2ayQd-8AsVhOnKazJcK0");
//        String newsSummary = newsService.getNewsSummaryById(newsId).getSummary();
//        File result = ffmpegService.mergeVideosWithBgmAndGeneratedVoice(
//                videoUrls,
//                bgmFile,
//                newsSummary,
//                voiceId
//        );
//
//        System.out.println("생성된 파일 경로: " + result.getAbsolutePath());
//
//    }
}
