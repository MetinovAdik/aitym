package com.techdragons.aitym.service;

import com.techdragons.aitym.model.Segment;
import com.techdragons.aitym.model.Trailer;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.*;

public class VideoGenerationService {

    private final String mediaDirPath;

    public VideoGenerationService() {
        this.mediaDirPath = System.getProperty("java.io.tmpdir") + File.separator + "media";
        try {
            Files.createDirectories(Paths.get(mediaDirPath));
        } catch (IOException e) {
            throw new RuntimeException("Could not create media directory", e);
        }
    }
    private void printFileListContent(Path fileListPath) throws IOException {
        System.out.println("Current content of file_list.txt:");
        Files.readAllLines(fileListPath).forEach(System.out::println);
    }
    private Path downloadImage(String imageUrl, String imageName) throws IOException {
        URL url = new URL(imageUrl);
        Path imagePath = Paths.get(mediaDirPath, imageName);
        Files.copy(url.openStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);
        return imagePath;
    }
    public void generateVideoFromTrailer(Trailer trailer, String outputVideoPath) throws IOException, InterruptedException {
        Path fileListPath = Paths.get(mediaDirPath, "file_list.txt");
        Files.deleteIfExists(fileListPath); // Удалить, если существует
        Files.createFile(fileListPath); // Создать новый

        for (Segment segment : trailer.getSegments()) {
            double segmentDuration = getAudioDuration(segment.getSpeechUrl());
            String segmentImageName = "img_" + segment.hashCode() + ".png";
            Path downloadedImagePath = downloadImage(segment.getImageUrl(), segmentImageName); // Загрузка изображения
            String segmentAudioPath = segment.getSpeechUrl();
            System.out.println(segmentAudioPath);
            String segmentVideoPath = Paths.get(mediaDirPath, "temp_" + segment.hashCode() + ".mp4").toString();
            String finalSegmentVideoPath = Paths.get(mediaDirPath, "final_" + segment.hashCode() + ".mp4").toString();

            executeCommand(String.format("ffmpeg -y -loop 1 -i \"%s\" -c:v libx264 -t %f -pix_fmt yuv420p \"%s\"", downloadedImagePath, segmentDuration, segmentVideoPath));
            executeCommand(String.format("ffmpeg -y -i \"%s\" -i \"%s\" -c:v copy -c:a aac -strict experimental \"%s\"", segmentVideoPath, segmentAudioPath, finalSegmentVideoPath));

            // Удаление загруженного изображения после использования
            Files.deleteIfExists(downloadedImagePath);

            // Добавление пути к видеофайлу в file_list.txt
            Files.writeString(fileListPath, "file '" + finalSegmentVideoPath + "'\n", StandardOpenOption.APPEND);
            printFileListContent(fileListPath);
        }

        // Конкатенация всех видеосегментов в один файл
        executeCommand(String.format("ffmpeg -y -f concat -safe 0 -i \"%s\" -c copy \"%s\"", fileListPath, outputVideoPath));
    }

    private double getAudioDuration(String audioFilePath) throws IOException, InterruptedException {
        Process process = new ProcessBuilder("ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "default=noprint_wrappers=1:nokey=1", audioFilePath).start();
        process.waitFor();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            return Double.parseDouble(reader.readLine());
        } catch (NumberFormatException e) {
            throw new IOException("Failed to parse audio duration.", e);
        }
    }

    private void executeCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
        processBuilder.redirectErrorStream(true); // Перенаправляем stderr в stdout
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Command execution failed with exit code " + exitCode);
        }
    }
}
