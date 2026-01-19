package io.github.scrvrdn.inventory;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;
import javafx.application.Platform;
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
        context.close();
        Platform.exit();
    }

    static class  StageReadyEvent extends ApplicationEvent {
        
        public StageReadyEvent(Stage stage) {
            super(stage);
        }

        public Stage getStage() {
            return ((Stage) getSource());
        }
        
    }

}
