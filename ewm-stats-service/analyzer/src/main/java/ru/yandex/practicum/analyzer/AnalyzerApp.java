package ru.yandex.practicum.analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import ru.yandex.practicum.analyzer.starter.InteractionStarter;
import ru.yandex.practicum.analyzer.starter.SimilarityStarter;

@SpringBootApplication
public class AnalyzerApp {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AnalyzerApp.class, args);

        InteractionStarter interactionStarter = context.getBean(InteractionStarter.class);
        SimilarityStarter similarityStarter = context.getBean(SimilarityStarter.class);

        Thread interactionThread = new Thread(interactionStarter);
        interactionThread.setName("InteractionStarter-Thread");
        interactionThread.start();
        similarityStarter.start();
    }
}
