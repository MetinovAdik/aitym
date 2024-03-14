package com.techdragons.aitym.model;



import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class Trailer {
    private List<Segment> segments;

    public Trailer() {
        this.segments = new ArrayList<>();
    }

    public void parseScenario(String scenario) {
        String[] lines = scenario.split("\n\n"); // Разделяем на сегменты по двойному переносу строки
        for (String line : lines) {
            String[] parts = line.split("\n"); // Разделяем описание и текст
            if (parts.length >= 2) {
                String frameDescription = parts[0].substring(parts[0].indexOf(":") + 1).trim();
                String text = parts[1].substring(parts[1].indexOf(":") + 1).trim();
                segments.add(new Segment(frameDescription, text));
            }
        }
    }

    public List<Segment> getSegments() {
        return segments;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Segment segment : segments) {
            sb.append(segment).append("\n");
        }
        return sb.toString();
    }
}