package client;

/**
 * Java is the best, who's the next
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class ClientNetworkGateway {

  
    private BufferedReader inputFromServer;
    private PrintWriter outputToServer;
    private TextArea textArea;

    // Establish the connection to the client.
    public ClientNetworkGateway(TextArea textArea) {
        this.textArea = textArea;
        try {
            // Create a socket to connect to the client
            Socket socket = new Socket("localhost", 8000);

            // Create an output stream to send data to the server
            outputToServer = new PrintWriter(socket.getOutputStream());

            // Create an input stream to read data from the server
            inputFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        } catch (IOException ex) {
            Platform.runLater(() -> textArea.appendText("Exception in gateway constructor: " + ex.toString() + "\n"));
        }
    }

    // Start the chat by sending in the user's username.
    public void sendUsername(String username) {
        if (username.equals(null)){username = "Guest";}
        outputToServer.println("code_username");
        outputToServer.println(username);
        outputToServer.flush();
    }

    // Send a new comment to the client.
    public void sendComment(String comment) {
        outputToServer.println("code_send_comment");
        outputToServer.println(comment);
        outputToServer.flush();
    }

    // Ask the client to send us a count of how many comments are
    // currently in the transcript.
    public int getCommentCount() {
        outputToServer.println("code_comments_count");
        outputToServer.flush();
        int count = 0;
        try {
            count = Integer.parseInt(inputFromServer.readLine());
        } catch (IOException ex) {
            // this is commented not to show this service message in the chat textarea of the client
            // this message is for development process rather than for the customer view
            // Chat message is shown in ClientController > ClientThread > run method
            // Platform.runLater(() -> textArea.appendText("Error in getCommentCount: " + ex.toString() + "\n"));

            // if server connection is stopped
            count = -1;
            ////
        }
        catch (NumberFormatException nfe){
            /// we receive java.lang.NumberFormatException: null at ClientNetworkGateway.java:61  when we stop the server for some clients
            /// Happens as we cannot read count because inputFromServer.readLine() is null already - the connection is closed and the stream is closed
            //when we stop the server for some clients we receive this exception - the network channel is stopped fine, but they do not see the
            // message that connection is stopped and might not understand what is happening, that is why we do here count = -1; below
            System.out.println("=========== NumberFormatException was registered ===========");
            count = -1;
        }
        return count;
    }

    // Fetch comment n of the transcript from the client.
    public String getComment(int commentIndex) {
        outputToServer.println("code_get_comment");
        outputToServer.println(commentIndex);
        outputToServer.flush();
        String comment = "";
        try {
            comment = inputFromServer.readLine();

        } catch (IOException ex) {
            Platform.runLater(() -> textArea.appendText("Error in getComment: " + ex.toString() + "\n"));
        }
        return comment;
    }
    // method to get the users from Server
    public String getUsersFromServer(){
        outputToServer.println("code_get_users_from_server");
        outputToServer.flush();
        String usersString = "";
        try {
            usersString = inputFromServer.readLine();

        } catch (IOException ex) {
            Platform.runLater(() -> textArea.appendText("Error in getUsersFromServer: " + ex.toString() + "\n"));
        }
        return usersString;
    }

}
