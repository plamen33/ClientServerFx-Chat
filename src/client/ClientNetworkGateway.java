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

            // Create an output stream to send data to the client
            outputToServer = new PrintWriter(socket.getOutputStream());

            // Create an input stream to read data from the client
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
            Platform.runLater(() -> textArea.appendText("Error in getCommentCount: " + ex.toString() + "\n"));
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
}
