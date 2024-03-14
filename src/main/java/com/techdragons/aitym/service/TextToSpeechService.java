package com.techdragons.aitym.service;

import com.techdragons.aitym.model.Segment;
import okhttp3.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class TextToSpeechService {
    private OkHttpClient client;
    private String apiKey; // Ваш API ключ

    public TextToSpeechService(OkHttpClient client,@Value("${openai.api.key}") String openAiApiKey) {
        this.client = client;
        this.apiKey = openAiApiKey;
    }

    public void generateSpeechForSegment(Segment segment) throws IOException {
        JSONObject body = new JSONObject();
        body.put("model", "tts-1"); // Используйте желаемую модель TTS
        body.put("input", segment.getText());
        body.put("voice", "alloy"); // Выберите желаемый голос

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/audio/speech")
                .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            // Сохранение аудиофайла из ответа
            byte[] audioBytes = response.body().bytes();
            Path path = Files.createTempFile("speech", ".mp3"); // Создайте временный файл для аудио
            Files.write(path, audioBytes, StandardOpenOption.WRITE);

            // Пример того, как вы можете обработать путь к файлу дальше
            String localFilePath = path.toString();
            // Здесь вы можете загрузить файл в облачное хранилище и получить URL
            // После чего сохранить URL в вашем объекте Segment
            segment.setSpeechUrl(localFilePath); // Пример использования локального пути или URL после загрузки
        }
    }
}