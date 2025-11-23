package smarttask;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Smart Task");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        // save on close
        primaryStage.setOnCloseRequest(e -> {
            MainController controller = loader.getController();
            if (controller != null) controller.saveTasksToDisk();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
