package com.example.justaitest;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Random;

@RestController
@RequestMapping("/callback")
public class VkBotController {

    private static final String TOKEN = "YOUR AUTH TOKEN";
    private static final String VK_API_URL = "https://api.vk.com/method/messages.send";
    private static final String CONFIRMATION_TEXT = "YOUR CALLBACKAPI TEXT";

    /**
     * Подтверждаем адрес сервера для CallBackApi и обрабатываем ответ
     * @param payload
     * @return
     */
    @PostMapping
    public String handleCallback(@RequestBody String payload) {
        try {
            // Создаем ObjectMapper для парсинга JSON
            ObjectMapper objectMapper = new ObjectMapper();

            // Преобразуем payload в JsonNode
            JsonNode callback = objectMapper.readTree(payload);

            // Проверяем тип события
            String eventType = callback.get("type").asText();
            if ("confirmation".equals(eventType)) {
                // Возвращаем строку для подтверждения
                return CONFIRMATION_TEXT;
            } else if ("message_new".equals(eventType)) {
                // Обрабатываем новое сообщение
                handleNewMessage(callback);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "ok"; // Возвращаем "ok" для всех других типов событий
    }

    /**
     * Получаем текст сообщения и id чата
     * @param callback
     */
    private void handleNewMessage(JsonNode callback) {
        try {
            // Извлекаем данные о сообщении
            JsonNode messageObject = callback.get("object").get("message");
            if (messageObject != null) {
                String messageText = messageObject.get("text").asText(); // Текст сообщения
                Long peerId = messageObject.get("peer_id").asLong(); // ID чата/пользователя

                // Формируем ответ
                String messageToSend = "Вы сказали: " + messageText;

                // Отправляем ответное сообщение в тот же чат
                sendMessageToChat(peerId, messageToSend);
            } else {
                System.out.println("Message object is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод для отправки сообщения пользователю
     * @param peerId Id чата
     * @param messageText Текст сообщения для отправки
     */
    private void sendMessageToChat(Long peerId, String messageText) {
        // Генерируем случайный ID для сообщения
        Random random = new Random();
        int randomId = random.nextInt(1000000);

        String url = VK_API_URL +
                "?access_token=" + TOKEN + // Ваш токен доступа
                "&v=5.131" +  // Версия API VK
                "&peer_id=" + peerId + // ID чата или пользователя
                "&message=" + messageText + // Текст сообщения
                "&from_group=1" +  // Отправка от имени сообщества
                "&random_id=" + randomId; // Случайный ID

        // Отправляем POST-запрос на VK API для отправки сообщения
        try {
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.postForObject(url, null, String.class);
            System.out.println("VK API response: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

