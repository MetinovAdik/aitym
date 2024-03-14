package com.techdragons.aitym.service;

import com.pengrad.telegrambot.UpdatesListener;
import com.techdragons.aitym.scenario.ScenarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class BotRunner implements CommandLineRunner {

    private final TelegramService telegramService;
    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(BotRunner.class);
    private final BroadcastService broadcastService;

    private final ScenarioService scenarioService;
    private final TrailerProcessService trailerProcessService;

    @Autowired
    public BotRunner(TelegramService telegramService, AuthService authService, BroadcastService broadcastService, ScenarioService scenarioService, TrailerProcessService trailerProcessService) {
        this.telegramService = telegramService;
        this.authService = authService;
        this.broadcastService = broadcastService;
        this.scenarioService = scenarioService;
        this.trailerProcessService = trailerProcessService;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Bot is starting");
        //String broadcastMessage = "Привет всем это всеобщее уведомление";
        //broadcastService.broadcastMessage(broadcastMessage);

        telegramService.getBot().setUpdatesListener(updates -> {
            updates.forEach(update -> {
                if (update.message() != null && update.message().chat() != null) {
                    long chatId = update.message().chat().id();
                    Long telegramUserId = update.message().from().id();

                    // Проверка наличия текста в сообщении для избежания NullPointerException
                    String text = update.message().text();
                    if (text != null) {
                        switch (text) {
                            case "/start":
                                if (!authService.authenticate(telegramUserId)) {
                                    telegramService.sendMessage(chatId, "Привет! Пожалуйста, зарегистрируйтесь с помощью команды /register.");
                                } else {
                                    telegramService.sendMessage(chatId, "Вы уже зарегистрированы. Добро пожаловать обратно!");
                                }
                                break;
                            case "/register":
                                authService.registerOrUpdateUser(telegramUserId, true);
                                telegramService.sendMessage(chatId, "Вы успешно зарегистрированы. Теперь вы можете использовать все функции бота.");
                                break;
                            // Добавьте обработку других команд здесь, если необходимо.
                        }
                    }

                    // Проверка аутентификации перед обработкой медиа
                    if (authService.authenticate(telegramUserId)) {
                        try {
                            trailerProcessService.processAndSendTrailer(scenarioService.makeScenario(text),chatId);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        telegramService.sendMessage(chatId, "Пожалуйста, зарегистрируйтесь с помощью команды /register для доступа к функциям.");
                    }
                }
            });
            return UpdatesListener.CONFIRMED_UPDATES_ALL;

        });
    }
}