/*
 * To change this license header, choose License Headers inputStream Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template inputStream the editor.
 */
package ChatServer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author Octaviano
 */
public class ChatClient {

    BufferedReader inputStream;
    PrintWriter outputStream;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(40);
    JTextArea messageArea = new JTextArea(8, 40);

    /**
     * Constructs the client by laying outputStream the GUI and registering a
     * listener with the textfield so that pressing Return inputStream the
     * listener sends the textfield contents to the server. Note however that
     * the textfield is initially NOT editable, and only becomes editable AFTER
     * the client receives the NAMEACCEPTED message from the server.
     */
    public ChatClient() {
// Layout GUI
        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, "North");
        frame.getContentPane().add(new JScrollPane(messageArea), "Center");
        frame.pack();
// Add Listeners
        textField.addActionListener(new ActionListener() {
            /**
             * Responds to pressing the enter key inputStream the textfield by
             * sending the contents of the text field to the server. Then clear
             * the text area inputStream preparation for the next message.
             */
            public void actionPerformed(ActionEvent e) {
                outputStream.println(textField.getText());
                textField.setText("");
            }
        });
    }

    /**
     * Prompt for and return the address of the server.
     */
    private String getServerAddress() {
        return JOptionPane.showInputDialog(
                frame,
                "Enter IP Address of the Server:",
                "Welcome to the Chatter",
                JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * Prompt for and return the desired screen name.
     */
    private String getName() {
        return JOptionPane.showInputDialog(
                frame,
                "Choose a screen name:",
                "Screen name selection",
                JOptionPane.PLAIN_MESSAGE);

    }

    /**
     * Connects to the server then enters the processing loop.
     */
    private void clientRun() throws IOException {
// Make connection and initialize streams
        String serverAddress = getServerAddress();
        Socket socket = new Socket(serverAddress, 9001);
        inputStream = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        outputStream = new PrintWriter(socket.getOutputStream(), true);
// Process all messages from server, according to the protocol.
        while (true) {
            String line = inputStream.readLine();
            if (line.startsWith("SUBMITNAME")) {
                outputStream.println(getName());
            } else if (line.startsWith("NAMEACCEPTED")) {
                textField.setEditable(true);
            } else if (line.startsWith("MESSAGE")) {
                messageArea.append(line.substring(8) + "\n");
            }
        }
    }

    /**
     * Runs the client as an application with a closeable frame.
     */
    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.clientRun();
    }
}
