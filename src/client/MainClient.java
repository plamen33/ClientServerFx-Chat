package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainClient extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        try {
            Parent root = FXMLLoader.load(getClass().getResource("../fxml/client.fxml"));
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setTitle("ClientFX");
            stage.setMinHeight(570);
            stage.setMinWidth(527);
            stage.setOnCloseRequest(event -> System.exit(0));
            stage.show();
        }
        catch (Exception e){
            System.out.println("MainClient has been canceled. Application close.");
            //e.printStackTrace();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
