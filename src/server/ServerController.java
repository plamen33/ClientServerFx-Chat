package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;

public class ServerController implements Initializable {

    @FXML
    private TextArea serverLogs;
    @FXML
    private TextArea comments;
    @FXML
    private CustomTextField enterMessage;
    @FXML
    private ListView<String> userList;
    private ObservableList<String> users;


    private int clientNumber = 0;
    private Server server = new Server();

    //// when you click on send button send the comment to the chat
    @FXML
    private void sendCommand(ActionEvent event) {
        String text = enterMessage.getText();
        comments.appendText("Server says: " + text + "\n"); /// print server comments to server comments log
        server.addComment("Server says: " + text); // send server comments to clients
        enterMessage.clear(); // when type from client clear the text box
    }
    //// when you click Enter button on keyboard send the comment to the chat
    @FXML
    public void handleKeyPressed(KeyEvent keyEvent) {
        if(keyEvent.getCode().equals(KeyCode.ENTER)){
            String text = enterMessage.getText();
            comments.appendText("Server says: " + text + "\n"); /// print server comments to server comments log
            server.addComment("Server says: " + text);  // send server comments to clients
            enterMessage.clear(); // when type from client clear the text box
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // server = new Server();

        new Thread( () -> {
            try {
                // Create a server socket
                ServerSocket serverSocket = new ServerSocket(8000);
                // clear button implementation from controlfx for the enterMessage CustomTextField
                users = FXCollections.observableArrayList();
                userList.setItems(users);
                try {
                    Method m = TextFields.class.getDeclaredMethod("setupClearButtonField", TextField.class, ObjectProperty.class);
                    m.setAccessible(true);
                    m.invoke(null, enterMessage, enterMessage.rightProperty());
                }catch (Exception e){
                    e.printStackTrace();
                }
                //// end of  clear button implementation

                while (true) {
                    // Listen for a new connection request
                    Socket socket = serverSocket.accept();
                    clientNumber++;

                    // Create and start a new thread for the connection
                    new Thread(new ServerThread(socket,server,serverLogs,comments, clientNumber, users)).start();
                }
            }
            catch(IOException ex) {
                System.err.println(ex);
            }
        }).start();
    }
    @FXML
    public void handleExit(){
        Platform.exit();
        System.exit(0);
    }
    @FXML
    private void onAbout(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("ServerFX JavaFX Application");
        alert.setTitle("About");
        alert.setContentText("ClientServer FX Application version: Basic\nVersion Release: 30.08.2017\nDevelopment platform: Java\nDeveloper: Plamen Petkov\n\nPowered by Java 8");
        alert.show();

    }
}

class ServerThread implements Runnable {
    private Socket socket; // socket to which we connect
    private Server server;
    private TextArea serverLogs;
    private TextArea comments;
    private String username;
    private ObservableList<String> users;

    private int clientNumber;

    public ServerThread(Socket socket, Server server, TextArea serverLogs, TextArea comments, int clientNumber, ObservableList<String> users) {
        this.socket = socket;
        this.server = server;
        this.serverLogs = serverLogs;
        this.comments = comments;
        this.clientNumber = clientNumber;
        this.users = users;

    }

    public void run() {
        try {

            // Create reading and writing streams to and from the Client
            BufferedReader inputFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter outputToClient = new PrintWriter(socket.getOutputStream());

            // read from the client and take actions all the time
            while (true) {
                // Receive request code from the client
                String request = inputFromClient.readLine();
                // Process request
                switch(request) {
                    case "code_username" : {
                        username = inputFromClient.readLine();
                        serverLogs.appendText("Starting thread for MainClient " + clientNumber  + " at " + new Date() + "  " + "Username: " + username + '\n');
                        users.add(username); // we add the user to the user list
                        break;
                    }
                    case "code_send_comment" : {
                        String comment = inputFromClient.readLine();
                        server.addComment(username + "> " + comment);   // show messages in the client log
                        comments.appendText(username + "> " + comment + "\n"); /// show messages in the server message log

                        break;
                    }
                    case "code_comments_count": {
                        outputToClient.println(server.getSize());
                        outputToClient.flush();
                        break;
                    }
                    case "code_get_comment": {
                        int commentIndex = Integer.parseInt(inputFromClient.readLine());
                        outputToClient.println(server.getComment(commentIndex));
                        outputToClient.flush();
                    }
                }
            }
        }
        catch(IOException ex) {
            Platform.runLater(()->serverLogs.appendText("MainClient " + clientNumber +" (username: "+username+") " + " has terminated. "+"\n"));

            if(username!=null){ // if user click cancel button before login - do not show in the comments chat log
                Platform.runLater(()->comments.appendText("User: "+username+" " + " has left the building. "+"\n"));
                server.addComment("User: "+username+" " + " has left the chat. ");   // show messages in the client log
            }
        }
    }
}