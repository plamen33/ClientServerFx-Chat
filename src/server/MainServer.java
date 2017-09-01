package server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainServer extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/server.fxml"));

        Scene scene = new Scene(root, 300, 275);

        stage.setScene(scene);
        stage.setTitle("ServerFX");
        stage.setMinHeight(577);
        stage.setMinWidth(519);
        stage.setOnCloseRequest(event->System.exit(0));  /// to completely shutdown the Server
        stage.show();
    }

    ///@param args the command line arguments

    public static void main(String[] args) {
        launch(args);
    }

}
