package io.github.cmassa;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Renderer {

    public static final String DEFAULT_OUTPUT_FORMAT = "mp4";

    public String render(Recording recording) throws IOException {
        String framesPath = recording.getFinalPath();
        List<File> imageFiles = this.getFrames(framesPath);

        if (imageFiles.isEmpty()) {
            throw new IOException("No images found in directory: " + framesPath);
        }

        int framesPerSecond = recording.getFramesPerSecond();
        String outputVideoPath = framesPath + File.separator + recording.getUuid() + "." + DEFAULT_OUTPUT_FORMAT;
        int width = recording.getWidth();
        int height = recording.getHeight();
        try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputVideoPath, width, height)) {
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setFormat(DEFAULT_OUTPUT_FORMAT);
            recorder.setFrameRate(framesPerSecond);
            recorder.start();
            this.addFramesToRecorder(recorder, imageFiles);
            recorder.stop();
            recorder.release();
        } finally {
            deleteImageFiles(imageFiles);
        }
        return outputVideoPath;
    }

    private void addFramesToRecorder(FFmpegFrameRecorder recorder, List<File> imageFiles) throws IOException {
        try (Java2DFrameConverter converter = new Java2DFrameConverter()) {
            for (File imageFile : imageFiles) {
                Frame frame = this.convertImageToFrame(converter, imageFile);
                recorder.record(frame);
            }
        }
    }

    private Frame convertImageToFrame(Java2DFrameConverter converter, File imageFile) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(imageFile);
        return converter.convert(bufferedImage);
    }

    private List<File> getFrames(String framesPath) {
        File folder = new File(framesPath);
        File[] frameFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        return Arrays.stream(Objects.requireNonNull(frameFiles))
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList());
    }

    private void deleteImageFiles(List<File> imageFiles) throws IOException {
        for (File file : imageFiles) {
            Files.delete(Path.of(file.getPath()));
        }
    }
}
