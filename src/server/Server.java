package server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Server {
    private List<String> listOfComments = Collections.synchronizedList(new ArrayList<String>());
    /// server constructor
    public Server() {}

    public void addComment(String comment) { listOfComments.add(comment); }
    public int getSize() { return listOfComments.size(); }
    public String getComment(int commentIndex ) { return listOfComments.get(commentIndex ); }
}
