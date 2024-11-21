package xyz.cmassa;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebDriverRecorder implements Runnable {

    private static final Logger logger = Logger.getLogger(WebDriverRecorder.class.getName());
    private static final long DEFAULT_FRAME_RATE = 10;
    private static final String SCREENSHOT_DATE_FORMAT = "yyyyMMdd_HHmmssSSS";

    private final WebDriver driver;

    private long frameRate;
    private final String recordingsDestination;
    private volatile boolean running = true;

    private Recording recording;
    private final Renderer renderer;

    private Thread thread;

    public WebDriverRecorder(WebDriver driver) {
        this(driver, DEFAULT_FRAME_RATE, null);
    }

    public WebDriverRecorder(WebDriver driver, String recordingsDestination) {
        this(driver, DEFAULT_FRAME_RATE, recordingsDestination);
    }

    public WebDriverRecorder(WebDriver driver, long frameRate) {
        this(driver, frameRate, null);
    }

    public WebDriverRecorder(WebDriver driver, long frameRate, String recordingsDestination) {
        this.driver = driver;
        this.frameRate = frameRate;
        this.recordingsDestination = (recordingsDestination == null || recordingsDestination.isEmpty())
                ? System.getProperty("video.destination", getDefaultRecordingsDestination())
                : recordingsDestination;
        this.renderer = new Renderer();
    }

    private String getDefaultRecordingsDestination() {
        return String.join(File.separator, System.getProperty("user.home"), "Videos", "Selenium");
    }

    public String getRecordingDestination() {
        return String.join(File.separator, recordingsDestination, recording.getUuid());
    }

    public Recording getRecording() {
        return this.recording;
    }

    public boolean isRunning() {
        return running;
    }

    public void start() {
        if (Boolean.parseBoolean(System.getProperty("video.enabled"))) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public String stop() throws InterruptedException, IOException {
        if (isRunning()) {
            this.thread.join();
            this.running = false;
            return renderer.render(this.recording);
        }
        return null;
    }

    @Override
    public void run() {
        if (frameRate == -1) frameRate = DEFAULT_FRAME_RATE;

        Dimension windowSize = driver.manage().window().getSize();
        int width = windowSize.getWidth();
        int height = windowSize.getHeight();

        this.recording = new Recording(1000 / (int) frameRate, width, height);
        String recordingPath = this.getRecordingDestination();
        this.recording.setFinalPath(recordingPath);

        File recordingsDestinationFolder = new File(recordingPath);
        if (!recordingsDestinationFolder.exists()) {
            boolean created = recordingsDestinationFolder.mkdirs();
            if (created) logger.log(Level.INFO, "Created {0} recording", recordingPath);
        }

        while (running) {
            takeScreenshot();
            try {
                TimeUnit.MILLISECONDS.sleep(frameRate);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "WebDriver recorder thread interrupted");
                Thread.currentThread().interrupt();
            }
        }
    }

    private void takeScreenshot() {
        String timestamp = new SimpleDateFormat(SCREENSHOT_DATE_FORMAT).format(new Date());
        File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String screenshotPath = String.join(File.separator, this.getRecordingDestination(), timestamp + ".png");
        File destinationFile = new File(screenshotPath);
        try {
            FileUtils.copyFile(screenshotFile, destinationFile);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while taking frame screenshot", e);
        }
    }
}
