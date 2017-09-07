package client;

import javafx.beans.property.ObjectProperty;
import javafx.event.Event;
import javafx.fxml.Initializable;

/**
 * Java is the best, who's the next
 */
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;

public class ClientController implements Initializable {
    private ClientNetworkGateway gateway;
    @FXML
    private TextArea commentLogs;
    @FXML
    private CustomTextField enterMessage;

    //// when you click on send button send the comment to the chat
    @FXML
    private void sendCommand(ActionEvent event) {
        String text = enterMessage.getText();
        gateway.sendComment(text);
        enterMessage.clear(); // when type from client clear the text box
    }
    //// when you click Enter button on keyboard send the comment to the chat
    @FXML
    public void handleKeyPressed(KeyEvent keyEvent) {
        if(keyEvent.getCode().equals(KeyCode.ENTER)){
            String text = enterMessage.getText();
            gateway.sendComment(text);
            enterMessage.clear(); // when type from client clear the text box
        }
    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        gateway = new ClientNetworkGateway(commentLogs);

        // Put up a dialog to get a username from the user
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Start MainClient");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter username:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(name -> {     /// if one does not input name then assign Guest as MainClient username
            if (name.equals("")){
                gateway.sendUsername("Guest");
            }
            else {gateway.sendUsername(name);}
        });
        if(result.get() == null){Platform.exit();};

        // clear button implementation from controlfx for the enterMessage CustomTextField
        try {
            Method m = TextFields.class.getDeclaredMethod("setupClearButtonField", TextField.class, ObjectProperty.class);
            m.setAccessible(true);
            m.invoke(null, enterMessage, enterMessage.rightProperty());
        }catch (Exception e){
            e.printStackTrace();
        }
        //// end of  clear button implementation

        // Start the Client main thread
        new Thread(new ClientThread (gateway,commentLogs)).start();
    }
    @FXML
    public void handleExit(){
        Platform.exit();
        System.exit(0);
    }
    @FXML
    private void onAbout(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("MainClient FX JavaFX Application");
        alert.setTitle("About");
        alert.setContentText("MainClient Server FX version: Basic\nVersion Release: 30.08.2017\nDevelopment platform: Java\nDeveloper: Plamen Petkov\n\nPowered by Java 8");
        alert.show();

    }
}

class ClientThread implements Runnable {
    private ClientNetworkGateway gateway; // Gateway to the client
    private TextArea textArea; // Where to display comments
    private int commentIndex; // Index of the comment from commentsList in Server class

    /** Construct a thread */
    public ClientThread (ClientNetworkGateway gateway,TextArea textArea) {
        this.gateway = gateway;
        this.textArea = textArea;
        this.commentIndex = 0;
    }

    /** Run a thread */
    public void run() {
        while(true) {
            // if server connection is stopped with the button
            if(gateway.getCommentCount() == -1){
                Platform.runLater(() -> textArea.appendText("...\n.......\n............\nServer connection was lost. \nRestart the client to try a new connection to chat."));
                break;
            }
            if(gateway.getCommentCount() > commentIndex) {
                String newComment = gateway.getComment(commentIndex);
                Platform.runLater(()->textArea.appendText(newComment + "\n")); ///show the comment in the Client text area window
                commentIndex++; // we increase the commentIndex number as we add more comments
            } else {
                try {
                    Thread.sleep(333); /// as no new comments are added we the ClientThread is sleeping for 1/3 of a second
                } catch(InterruptedException ex) {
                    System.out.println("test print for client exception");
                }
            }
        }
    }
}
