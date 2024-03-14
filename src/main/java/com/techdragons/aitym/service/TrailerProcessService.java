package com.techdragons.aitym.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendVideo;
import com.techdragons.aitym.model.Segment;
import com.techdragons.aitym.model.Trailer;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class TrailerProcessService {

    private final ImageGenerationService imageService;
    private final TextToSpeechService speechService;
    private final VideoGenerationService videoService;
    private final TelegramBot bot;

    // Injecting OpenAI API key and Telegram bot through the constructor
    public TrailerProcessService(OkHttpClient httpClient, @Value("${openai.api.key}") String openAiApiKey, TelegramBot bot) {
        this.imageService = new ImageGenerationService(httpClient, openAiApiKey);
        this.speechService = new TextToSpeechService(httpClient, openAiApiKey);
        this.videoService = new VideoGenerationService();
        this.bot = bot;
    }

    public void processAndSendTrailer(String scenario, Long chatId) {
        try {
            System.out.println("Starting trailer processing.");
            Trailer trailer = new Trailer();
            trailer.parseScenario(scenario);
            System.out.println("Trailer parsed successfully.");
            System.out.println("Trailer contains " + trailer.getSegments().size() + " segments.");

            // Проверяем, есть ли сегменты в трейлере перед генерацией изображений и аудио
            if (trailer.getSegments().isEmpty()) {
                System.err.println("No segments found in the trailer. Exiting process.");
                return; // Если сегментов нет, прерываем обработку
            }
            // Generating images and audio for each segment
            for (Segment segment : trailer.getSegments()) {
                System.out.println("Generating image for segment.");
                imageService.generateImageForSegment(segment);
                System.out.println("Image generated successfully.");

                System.out.println("Generating speech for segment.");
                speechService.generateSpeechForSegment(segment);
                System.out.println("Speech generated successfully.");
            }

            // Creating a temporary file for the output video
            Path outputVideoPath = Files.createTempFile("trailer", ".mp4");
            System.out.println("Temporary video file created: " + outputVideoPath);

            // Creating video from the trailer
            System.out.println("Generating video from trailer.");
            videoService.generateVideoFromTrailer(trailer, outputVideoPath.toString());
            System.out.println("Video generated successfully.");

            // Sending the video to the user
            System.out.println("Sending video to the user.");
            bot.execute(new SendVideo(chatId, outputVideoPath.toFile()));
            System.out.println("Video sent successfully.");

            // After sending the video, the temporary file can be deleted
            Files.deleteIfExists(outputVideoPath);
            System.out.println("Temporary video file deleted.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error processing and sending trailer: " + e.getMessage());
            // Handle errors, e.g., sending an error message to the user
        }
    }
}
