package server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainServer extends Application {

    private BorderPane serverLayout;

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml/server.fxml"));

//        ServerController serverController = new ServerController();
//        loader.setController(serverController);
        serverLayout = (BorderPane) loader.load();
        Scene scene = new Scene(serverLayout);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm()); //++
        stage.setScene(scene);
        stage.setTitle("ServerFX");
        stage.setMinHeight(700);
        stage.setMinWidth(757);
        stage.setOnCloseRequest(event->System.exit(0));  /// to completely shutdown the Server
        stage.show();
    }

    ///@param args the command line arguments

    public static void main(String[] args) {
        launch(args);
    }

}
