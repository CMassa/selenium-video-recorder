# selenium-video-recorder


![CI Build](https://github.com/CMassa/selenium-video-recorder/actions/workflows/ci.yml/badge.svg)

## Description

selenium-video-recorder is a lightweight and easy-to-use library for recording videos of a WebDriver session


## Installation

### Maven
Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.cmassa</groupId>
    <artifactId>selenium-video-recorder</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

### Quick start

```java
public class Main {
    public static void main(String[] args) {
        // Initialize driver
        System.setProperty("webdriver.chrome.driver", "C:/tmp/chromedriver.exe");
        WebDriver driver = new ChromeDriver();

        // Enable video
        System.setProperty("video.enabled", String.valueOf(true));
        
        // Set path for the videos to be created (If not specified they will be created under System.getProperty("user.home")/Videos/Selenium
        System.setProperty("video.destination", "C:/tmp");

        // Create recorder and assign web driver to it
        WebDriverRecorder recorder = new WebDriverRecorder(driver);

        // Start recorder
        recorder.start();
        
        /*
            DO DRIVER STUFF
         */

        // Stop recorder (Render and returns video path)
        String videoPath = recorder.stop();

        driver.quit();
    }
}
```

### Junit5

```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

public class MyTest {
    
    private WebDriver driver;
    private WebDriverRecorder recorder;
    
    @BeforeEach
    public void startDriver() {
        System.setProperty("webdriver.chrome.driver", "C:/tmp/chromedriver.exe");
        System.setProperty("video.enabled", String.valueOf(true));
        System.setProperty("video.destination", "C:/tmp");
        driver = new ChromeDriver();
        recorder = new WebDriverRecorder(driver);
        recorder.start();
    }
    
    @Test
    void test() {
        /*
            DO DRIVER STUFF
         */
    }        
    
    
    @AfterEach
    public void startDriver() {
        String videoPath = recorder.stop();
        driver.quit();
    }
}
```


### Allure

```java
    String videoPath = recorder.stop();
    File videoFile = new File(videoPath);
    Allure.addAttachment("Test video", "video/mp4", Files.asByteSource(videoFile).openStream(), "mp4");
```
