package io.github.scrvrdn.inventory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import io.github.scrvrdn.inventory.Main.StageReadyEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

@Component
public class StageInitializer implements ApplicationListener<StageReadyEvent> {
    @Value("classpath:fxml/main.fxml")
    private Resource mainFxmlResource;

    @Value("classpath:fxml/details.fxml")
    private Resource detailsFxmlResource;

    @Value("classpath:css/style.css")
    private Resource styleResource;

    private final ApplicationContext context;
    private String appTitle;

    public StageInitializer(ApplicationContext context, @Value("${spring.application.name}") String appTitle) {
        this.context = context;
        this.appTitle = appTitle;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(mainFxmlResource.getURL());
            loader.setControllerFactory(context::getBean);
            Parent parent = loader.load();

            Scene scene = new Scene(parent);
            scene.getStylesheets().add(styleResource.getURL().toExternalForm());

            Stage stage = event.getStage();
            stage.setScene(scene);
            stage.setTitle(appTitle);
            stage.show();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
