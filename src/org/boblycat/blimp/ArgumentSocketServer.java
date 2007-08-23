/*
 * Copyright (C) 2007 Knut Arild Erstad
 *
 * This file is part of Blimp, a layered photo editor.
 *
 * Blimp is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Blimp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.boblycat.blimp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A socket server which serves two purposes:
 * 
 * 1. can be used to ensure that only one instance of an application is running.
 * 
 * 2. can be used to send extra arguments to the running "singleton application"
 * before shutting down.
 * 
 * @author Knut Arild Erstad
 */
public class ArgumentSocketServer extends Thread {
    public interface Listener {
        void handleArgument(String arg);
    }
    
    private class MyEventSource extends EventSource<Listener, String> {
        public void triggerListenerEvent(Listener li, String fileName) {
            li.handleArgument(fileName);
        }
    }
    
    private static final int PORT = 29707;
    
    ServerSocket serverSocket;
    Socket clientSocket;
    MyEventSource eventSource;
    
    public ArgumentSocketServer() {
        super("Blimp ArgumentSocketServer Thread");
        eventSource = new MyEventSource();
    }
    
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(PORT, 1);
            while (!interrupted()) {
                clientSocket = serverSocket.accept();
                try {
                    InputStream in = clientSocket.getInputStream();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(in));
                    String line;
                    while ((line = reader.readLine()) != null && !interrupted()) {
                        Util.info("socket server got: " + line);
                        handleArgument(line);
                    }
                    clientSocket.close();
                }
                catch (IOException e) {
                    Util.err("I/O exception in ArgumentSocketServer: "
                            + e.getMessage());
                }
            }
        }
        catch (IOException e) {
            // this is normal when closing the server socket
        }
        catch (Exception e) {
            Util.err("ArgumentSocketServer exception: " + e.getMessage());
        }
    }
    
    public void addListener(Listener listener) {
        synchronized (eventSource) {
            eventSource.addListener(listener);
        }
    }
    
    public void removeListener(Listener listener) {
        synchronized (eventSource) {
            eventSource.removeListener(listener);
        }
    }

    private void handleArgument(String arg) {
        synchronized (eventSource) {
            eventSource.triggerChangeWithEvent(arg);
        }
    }
    
    private static boolean connectToServerAndSendArguments(String[] args) {
        Socket sock;
        try {
            sock = new Socket(InetAddress.getLocalHost(), PORT);
        }
        catch (IOException e) {
            // could not connect to server
            return false;
        }
        try {
            OutputStream out = sock.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(out));
            for (String arg: args) {
                // Expand the argument as a file name if the file exists
                File file = new File(arg);
                if (file.exists())
                    arg = file.getAbsolutePath();
                writer.write(arg + "\n");
            }
            writer.flush();
            out.flush();
            sock.close();
        }
        catch (IOException e) {
            Util.err("I/O error while writing arguments to server: " + e.getMessage());
        }
        return true;
    }
    
    public void close() {
        try {
            if (serverSocket != null)
                serverSocket.close();
        }
        catch (IOException e) {
        }
        interrupt();
    }
    
    public static void startServerOrQuit(ArgumentSocketServer server,
            String[] args) {
        if (connectToServerAndSendArguments(args))
            System.exit(0);
        server.start();
    }
}
