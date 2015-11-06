/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ChatServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 *
 * @author Octaviano
 */
public class ChatServer {

    /**
     * The port that the server listens on.
     */
    private static final int PORT = 9001;
    /**
     * The set of all names of clients in the chat room. Maintained so that we
     * can check that new clients are not registering name already in use.
     */
    private static HashSet<String> names = new HashSet<String>();
    /**
     * The set of all the print writers for all the clients. This set is kept so
     * we can easily broadcast messages.
     */
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    private static Map<String, PrintWriter> personnalMessage = new HashMap<String, PrintWriter>();

    /**
     * The appplication main method, which just listens on a port and spawns
     * handler threads.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }

    /**
     * A handler thread class. Handlers are spawned from the listening loop and
     * are responsible for a dealing with a single client
     *
     */
    private static class Handler extends Thread {

        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        /**
         * Constructs a handler thread, squirreling away the socket. All the
         * interesting work is done in the run method.
         */
        public Handler(Socket socket) {
            this.socket = socket;
        }

        /**
         * Services this thread's client by repeatedly requesting a screen name
         * until a unique one has been submitted, then acknowledges the name and
         * registers the output stream for the client in a global set, then
         * repeatedly gets inputs and broadcasts them.
         */
        public void run() {
            try {
// Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
// Request a name from this client. Keep requesting until
// a name is submitted that is not already used. Note that
// checking for the existence of a name and adding the name
// must be done while locking the set of names.
                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    System.out.println(name);
                    if (name == null) {
                        return;
                    }
                    synchronized (names) {
                        if (!names.contains(name)) {
                            names.add(name);
                            break;
                        }
                    }
                }
// Now that a successful name has been chosen, add the
// socket's print writer to the set of all writers so
// this client can receive broadcast messages.
                out.println("NAMEACCEPTED");
                writers.add(out);
                //place the NAME and PRINTWRITER in MAP for easy access.
                personnalMessage.put(name, out);

                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }
                    //PERSONNAL MESSAGE
                    String[] keywords = input.split(" ");
                    if (keywords[0].equals("/whisper") && names.contains(keywords[1])) {
                        //PERSONNAL MESSAGE e.g. /whisper manolito
                        //temp count for substring
                        int count = keywords[0].length() + keywords[1].length() + 2;

                        //To self
                        PrintWriter onePerson = personnalMessage.get(name);
                        onePerson.println("MESSAGE " + name + ": " + input.substring(count));

                        //To the recipient.
                        onePerson = personnalMessage.get(keywords[1]);
                        onePerson.println("MESSAGE " + name + ": " + input.substring(count));
                    } //BROADCAST
                    else {
                        for (PrintWriter writer : writers) {
                            System.out.println("BROADCASTING: " + personnalMessage.get(name));
                            writer.println("MESSAGE " + name + ": " + input);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
// This client is going down! Remove its name and its print
// writer from the sets, and close its socket.
                if (name != null) {
                    names.remove(name);
                }
                if (out != null) {
                    writers.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
