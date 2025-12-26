package Objects.exams;

import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable {
    private String user;
    private String password;
    private String content;
    private ArrayList<String> messages;

    public Message() {}

    public Message(String user, String password, String content, ArrayList<String> messages) {
        this.user = user;
        this.password = password;
        this.content = content;
        this.messages = messages;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ArrayList<String> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<String> messages) {
        this.messages = messages;
    }

    @Override
    public String toString() {
        return "Message {" +
                "user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", content='" + content + '\'' +
                ", messages=" + messages +
                '}';
    }
}
