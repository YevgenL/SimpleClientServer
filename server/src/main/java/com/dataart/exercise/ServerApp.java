package com.dataart.exercise;

import com.dataart.exercise.server.SocketServer;

/**
 * Server Application.
 *
 * @author Eugene Lapin
 * @version 1.0
 */
public class ServerApp {

    public static void main(String args[]) throws Exception {
        new SocketServer().start(args);
    }
}
