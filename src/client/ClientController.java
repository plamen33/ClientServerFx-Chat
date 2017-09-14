package client;

import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;


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
    @FXML
    private CustomTextField hostIP;

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

        String hostIPAddress = hostIP.getText();
        if(hostIPAddress.equals("") || hostIPAddress == null){
            hostIPAddress = "localhost";
        }
        gateway = new ClientNetworkGateway(commentLogs, hostIPAddress);

        // display a login entry in the comment Log:
        String entryMessage = "Successful connection. Address: " + gateway.getInternetAddress() + " port number: " + gateway.getSocketPort();
        displayAtCommentLog(entryMessage);

        // front end for User: - this is the text before logged user
        userText.setFont(Font.font(null, FontWeight.BOLD, 14));
        /// username of logged user text front end
        username.setFill(Color.DARKGREEN);
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
        // disable input fields
//        hostIP.setEditable(false);
//        inputUsername.setEditable(false);
        hostIP.setDisable(true);
        inputUsername.setDisable(true);
        // set button outlook
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
        // enable input fields
        hostIP.setDisable(false);
        inputUsername.setDisable(false);
    }

    @FXML
    public void handleExit(){
        Platform.exit();
        System.exit(0);
    }
    @FXML
    private void onAbout(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Client FX JavaFX Application");
        alert.setTitle("About");
        alert.setContentText("Client FX version: Advanced\nVersion Release: 14.09.2017\nDevelopment platform: Java\nDeveloper: Plamen Petkov\n\nPowered by Java 8");
        ((Stage)alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("/images/chat.png")); // add icon to the alert window
        alert.show();
        ///stage.getIcons().add(new Image(getClass().getResource("../images/chat.png").toExternalForm()));
    }
    @FXML
    private void onHelp(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("ClientFX Help");
        alert.setTitle("Help");
        alert.setContentText("In Host IP field type IP address of the Server: \nExamples:\nlocalhost\n12.7.87.571 (sample IP address of server machine)\nMYCOMP (example domain name of Server machine)");
        ((Stage)alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("/images/chat.png")); // add icon to the alert window
        alert.show();
    }

    private void displayAtCommentLog(String message) {
        commentLogs.appendText(message + "\n"); // append to the ServerChatArea
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
                Arrays.asList(arrayWithUsers).stream().forEach(user -> { Platform.runLater(() -> { users.add(user.trim());});});
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
                List<String> arrayWithUsers = new ArrayList<String>(Arrays.asList(usersString.split(",")).stream().map(String::trim).collect(Collectors.toList()));
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
