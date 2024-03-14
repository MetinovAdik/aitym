package com.techdragons.aitym.service;

import com.techdragons.aitym.model.Segment;
import okhttp3.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ImageGenerationService {
    private OkHttpClient client;
    private String apiKey; // Ваш API ключ для DALL·E

    public ImageGenerationService(OkHttpClient client,@Value("${openai.api.key}") String openAiApiKey) {
        this.client = client;
        this.apiKey = openAiApiKey;;
    }

    public void generateImageForSegment(Segment segment) throws IOException {
        JSONObject body = new JSONObject();
        body.put("prompt", segment.getFrameDescription());
        body.put("n", 1); // Генерировать одно изображение
        body.put("model", "dall-e-3"); // Вы можете выбрать модель, например "dall-e-2" или "dall-e-3"
        body.put("size", "1024x1024"); // Размер генерируемого изображения

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/images/generations")
                .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String responseBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseBody);
            // Предполагается, что ответ содержит массив данных с URL изображений
            String imageUrl = jsonResponse.getJSONArray("data").getJSONObject(0).getString("url");
            segment.setImageUrl(imageUrl); // Записываем URL в объект сегмента
        }
    }
}
