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
//        String newsId = "07100501.20251030140051001";
//        ClassPathResource bgmResource = new ClassPathResource("static/bgm.mp3");
//        String voiceId = "Z0OdZXiRr0rnqYIAcBJs";
//        File bgmFile = bgmResource.getFile();
//
//        //newsVideoService.getScenesFromNews(newsId);
//        //List<String> videoUrls = newsVideoService.generateVideoFromNews(newsId);
//        List<String> videoUrls = new ArrayList<>();
//        videoUrls.add("https://dnznrvs05pmza.cloudfront.net/19df8d58-c545-4886-9189-168ebb275aa3.mp4?_jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJrZXlIYXNoIjoiNmI2ZTcxMDIwOGE0NTk2ZSIsImJ1Y2tldCI6InJ1bndheS10YXNrLWFydGlmYWN0cyIsInN0YWdlIjoicHJvZCIsImV4cCI6MTc2NDk3OTIwMH0.hgWrE4zqmUjoGVsYdzkSYH-y2BxlQHaxHy1S6bazENk");
//        videoUrls.add("https://dnznrvs05pmza.cloudfront.net/veo3.1/projects/vertex-ai-claude-431722/locations/us-central1/publishers/google/models/veo-3.1-fast-generate-preview/operations/bf3641c8-e6ec-4019-845d-8ec18183e894/Capture_the_concert_stage_setup_with_vibrant_lights_and_equipment__Ensure_the_shot_is_vertical__9_16.mp4?_jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJrZXlIYXNoIjoiMDdhNjZjNjIyYWNhYjEyZSIsImJ1Y2tldCI6InJ1bndheS10YXNrLWFydGlmYWN0cyIsInN0YWdlIjoicHJvZCIsImV4cCI6MTc2NDk3OTIwMH0.WCi4uflYKgQOPrFwM4NTooO67mZhmUyadBY3UKX2_EY");
//        videoUrls.add("https://dnznrvs05pmza.cloudfront.net/veo3.1/projects/vertex-ai-claude-431722/locations/us-central1/publishers/google/models/veo-3.1-fast-generate-preview/operations/285d4327-66b8-483b-b65f-f1d8d1003edd/Show_a_distant_view_of_an_audience_silhouette_seated_in_anticipation__The_composition_should_be_vert.mp4?_jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJrZXlIYXNoIjoiYzdmMGIwNjA5Zjc0ZTI1NSIsImJ1Y2tldCI6InJ1bndheS10YXNrLWFydGlmYWN0cyIsInN0YWdlIjoicHJvZCIsImV4cCI6MTc2NDk3OTIwMH0.9YPdV-jpCDYOR3wH3nGeQqj1EP9oBTWDhsecHAm1u_g");
//        videoUrls.add("https://dnznrvs05pmza.cloudfront.net/veo3.1/projects/vertex-ai-claude-431722/locations/us-central1/publishers/google/models/veo-3.1-fast-generate-preview/operations/4b1f5762-2933-4781-8f9d-4a67b2e3955b/Focus_on_the_interior_of_a_uniquely_designed_hotel_room_that_tells_a_story__Frame_it_vertically__9_1.mp4?_jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJrZXlIYXNoIjoiMjg5OTA2YzhkZTY5OTFiNiIsImJ1Y2tldCI6InJ1bndheS10YXNrLWFydGlmYWN0cyIsInN0YWdlIjoicHJvZCIsImV4cCI6MTc2NDk3OTIwMH0.Ra408nvvBIJdUUy78FTAMpMzwTswgxqOtvi2Wb66Ur0");
//        videoUrls.add("https://dnznrvs05pmza.cloudfront.net/veo3.1/projects/vertex-ai-claude-431722/locations/us-central1/publishers/google/models/veo-3.1-fast-generate-preview/operations/89d64788-c380-403d-8ddd-73a350e5d63f/Capture_a_panoramic_view_of_the_concert_atmosphere__highlighting_the_energy_and_excitement__The_shot.mp4?_jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJrZXlIYXNoIjoiNzM3NGYzMGQ2ZTg1MzJkYSIsImJ1Y2tldCI6InJ1bndheS10YXNrLWFydGlmYWN0cyIsInN0YWdlIjoicHJvZCIsImV4cCI6MTc2NDk3OTIwMH0.E6wVGajWVGn2H_ny050sM62qGjGIA7GnFiAtKOKsEQ4");
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
