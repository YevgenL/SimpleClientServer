package com.dataart.exercise;

import com.dataart.exercise.client.SocketClient;

/**
 * Client Application.
 *
 * @author Eugene Lapin
 * @version 1.0
 */
public class ClientApp {

    /**
     * An entry point of the client application.
     *
     * @param args the command line arguments.
     */
    public static void main(String args[]) throws Exception {
        new SocketClient().start(args);
    }
}
