package com.techdragons.aitym.service;

import com.pengrad.telegrambot.TelegramBot;
import com.techdragons.aitym.scenario.ScenarioService;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AppConfig {

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(30)) // 30 seconds in milliseconds
                .readTimeout(Duration.ofSeconds(30))    // 30 seconds in milliseconds
                .writeTimeout(Duration.ofSeconds(30))   // 30 seconds in milliseconds
                .build();
    }
    @Bean
    public ScenarioService scenarioService(@Value("${openai.api.key}") String apiKey, OkHttpClient client) {
        return new ScenarioService(apiKey, client);
    }
    @Bean
    public TelegramBot telegramBot(@Value("${telegram.bot.token}") String botToken) {
        return new TelegramBot(botToken);
    }
}
