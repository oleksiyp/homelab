package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UpscaleController {
    public static void main(String[] args) {
        new SpringApplication(UpscaleController.class).run(args);
    }
}
