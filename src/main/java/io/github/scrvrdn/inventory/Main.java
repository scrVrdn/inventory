package io.github.scrvrdn.inventory;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import io.github.scrvrdn.inventory.events.StageReadyEvent;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        context = new SpringApplicationBuilder(InventoryApplication.class)
                    .headless(false)
                    .run();
    }

    @Override
    public void start(Stage stage) {
        context.publishEvent(new StageReadyEvent(stage));
    }

    @Override
    public void stop() {
        if (context != null) {
            context.close();
        }
    }

}
