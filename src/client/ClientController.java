package client;

import com.sun.org.apache.xpath.internal.SourceTree;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.Initializable;

/**
 * Java is the best, who's the next
 */
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import server.ServerController;

public class ClientController{
    private ClientNetworkGateway gateway;
    @FXML
    private TextArea commentLogs;
    @FXML
    private CustomTextField enterMessage;
    @FXML
    private ListView<String> userList;       // user list for Client
    private ObservableList<String> users;    // user list for Client
    @FXML
    private Text userText; // text User: before logged user username
    @FXML
    private Text username; // logged user
    @FXML
    private Button buttonLogin;
    @FXML
    private Button buttonLogout;
    @FXML
    private CustomTextField inputUsername;
    private LinkedList<ClientThread> clientThreads = new LinkedList<>();
    private Thread newThread;
    private boolean isLogout = false;
    private String userName;

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

    @FXML
    public void loginClient() {


        gateway = new ClientNetworkGateway(commentLogs);

        // front end for User: - this is the text before logged user
        userText.setFont(Font.font(null, FontWeight.BOLD, 14));
        /// username of logged user text front end
        username.setFill(Color.LIMEGREEN);
        username.setFont(Font.font(null, FontWeight.BOLD, 16));

        String currentUserName = inputUsername.getText();
        currentUserName = currentUserName.replace(",", " "); // eliminame possible commas to avoid visual hack of the client
        userName = currentUserName; // set the String username which belongs to the class
        if (currentUserName.equals("")){
            gateway.sendUsername("Guest");
            Platform.runLater(() -> { username.setText("Guest");  });
        }
        else {gateway.sendUsername(currentUserName); // send username of Client to Server
            if(currentUserName.length() > 16){currentUserName = currentUserName.substring(0,12)+"...";}
            String loggedUserName = currentUserName;
            Platform.runLater(() -> { username.setText(loggedUserName);  });
        }


        /// Client user list initialization
        users = FXCollections.observableArrayList();
        userList.setItems(users);



        // clear button implementation from controlfx for the enterMessage CustomTextField
        try {
            Method m = TextFields.class.getDeclaredMethod("setupClearButtonField", TextField.class, ObjectProperty.class);
            m.setAccessible(true);
            m.invoke(null, enterMessage, enterMessage.rightProperty());
        }catch (Exception e){
            e.printStackTrace();
        }
        //// end of  clear button implementation


        ClientThread clientThread = new ClientThread (gateway,commentLogs, users, isLogout);
        newThread = new Thread(clientThread);
        newThread.start();
        clientThreads.add(clientThread);




        buttonLogin.setDisable(true);
        buttonLogout.setDisable(false);
    }
    @FXML
    public void logoutClient() {
        /// need to do this if we start the client for more than one time -> to clear everything
        users.clear();
        commentLogs.clear();
        /////////////
        gateway.sendRemoveUserToServer(userName);

        try{
            clientThreads.get(0).shutdown();
            newThread.stop();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        gateway.closeGateway();
        buttonLogin.setDisable(false);
        buttonLogout.setDisable(true);
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
    private ObservableList<String> users; // Client user list
    private boolean runFirstTime = false;
    private boolean isLogout;

    /** Construct a thread */
    public ClientThread (ClientNetworkGateway gateway,TextArea textArea, ObservableList<String> users, boolean isLogout ) {
        this.gateway = gateway;
        this.textArea = textArea;
        this.commentIndex = 0;
        this.users = users;// Client user list
        this.isLogout = isLogout;
    }
    public void shutdown() {
        isLogout = true;
    }


    /** Run a thread */
    public void run() {

        while(!isLogout) {
            if(!runFirstTime) {
                runFirstTime =true;
                String usersString = gateway.getUsersFromServer();
                String[] arrayWithUsers = usersString.split(",");
                Arrays.asList(arrayWithUsers).stream().forEach(user -> { Platform.runLater(() -> { users.add(user);});});
                // the same below in conventional way
//                for (int i = 0; i < arrayWithUsers.length; i++) {
//                    String user = arrayWithUsers[i];
//                    Platform.runLater(() -> {
//                        users.add(user);
//                    });
//                }
            }
            else{
                String usersString = gateway.getUsersFromServer();
                List<String> arrayWithUsers = new ArrayList<String>(Arrays.asList(usersString.split(",")));
                if(arrayWithUsers.size() != users.size()){
                    Platform.runLater(() -> {
                        users.clear();
                        users.addAll(arrayWithUsers); // better do this
                        System.out.println("clear operation");
                    });;
                }
            }


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
        }// end of while cycle
    }//end of run
}
