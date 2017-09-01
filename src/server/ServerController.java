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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;

public class ServerController implements Initializable {

    @FXML
    private TextArea serverLogs;

    private int clientNumber = 0;
    private Server server = new Server();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        new Thread( () -> {
            try {
                // Create a server socket
                ServerSocket serverSocket = new ServerSocket(8000);

                while (true) {
                    // Listen for a new connection request
                    Socket socket = serverSocket.accept();

                    clientNumber++;

                    // Create and start a new thread for the connection
                    new Thread(new ServerThread(socket,server,serverLogs, clientNumber)).start();
                }
            }
            catch(IOException exception) {
                System.err.println(exception);
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
    private Socket socket; // The socket with which we connect
    private Server server;
    private TextArea serverLogs;

    private String username;
    private int clientNumber;

    public ServerThread(Socket socket, Server server, TextArea serverLogs, int clientNumber) {
        this.socket = socket;
        this.server = server;
        this.serverLogs = serverLogs;
        this.clientNumber = clientNumber;

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
                        break;
                    }
                    case "code_send_comment" : {
                        String comment = inputFromClient.readLine();
                        server.addComment(username + "> " + comment);   // show messages in the client log


                        break;
                    }
                    case "code_comments_count": {
                        outputToClient.println(server.getSize());
                        outputToClient.flush();
                        break;
                    }
                    case "code_get_comment": {
                        int commentIndex = Integer.parseInt(inputFromClient.readLine());
                        outputToClient.println(server.getComment(commentIndex)); // send the comment to the client
                        outputToClient.flush();
                    }
                }
            }
        }
        catch(IOException ex) {
            Platform.runLater(()->serverLogs.appendText("MainClient " + clientNumber +" (username: "+username+") " + " has terminated. "+"\n"));
            if(username!=null){ // if user click cancel button before login - do not show in the comments chat log
                server.addComment("User: "+username+" " + " has left the chat. ");   // show messages in the client log
            }
        }
    }
}