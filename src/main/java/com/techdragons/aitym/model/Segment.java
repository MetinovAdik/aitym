package com.techdragons.aitym.model;

import java.util.ArrayList;
import java.util.List;

public class Segment {
    private String frameDescription;
    private String text;
    private String imageUrl; // URL изображения для сегмента
    private String speechUrl; // URL аудиофайла с речью

    // Конструктор
    public Segment(String frameDescription, String text) {
        this.frameDescription = frameDescription;
        this.text = text;
        this.imageUrl = ""; // Инициализация пустым значением
        this.speechUrl = ""; // Инициализация пустым значением
    }

    // Геттеры и сеттеры
    public String getFrameDescription() { return frameDescription; }
    public void setFrameDescription(String frameDescription) { this.frameDescription = frameDescription; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getSpeechUrl() { return speechUrl; }
    public void setSpeechUrl(String speechUrl) { this.speechUrl = speechUrl; }

    @Override
    public String toString() {
        return "Segment{" +
                "frameDescription='" + frameDescription + '\'' +
                ", text='" + text + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", speechUrl='" + speechUrl + '\'' +
                '}';
    }
}