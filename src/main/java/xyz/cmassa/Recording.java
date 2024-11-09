package xyz.cmassa;

import java.util.UUID;

public class Recording {

    private final String uuid;
    private final int fps;
    private String finalPath;
    private final int width;
    private final int height;

    public Recording(int fps, int width, int height) {
        this.uuid = String.valueOf(UUID.randomUUID());
        this.fps = fps;
        this.width = width;
        this.height = height;
    }

    public String getUuid() {
        return uuid;
    }

    public int getFramesPerSecond() {
        return fps;
    }

    public String getFinalPath() {
        return this.finalPath;
    }

    public void setFinalPath(String finalPath) {
        this.finalPath = finalPath;
    }

    public int getFps() {
        return fps;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
