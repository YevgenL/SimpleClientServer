package com.dataart.exercise.server;

import com.dataart.exercise.entity.Bird;
import com.dataart.exercise.entity.Sighting;
import com.dataart.exercise.service.FileService;
import com.dataart.exercise.service.impl.FileServiceImpl;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Server class.
 *
 * @author Eugene Lapin
 * @version 1.0
 */
public class SocketServer {

    /**
     * Service for operations with file system
     */
    private FileService fileService = new FileServiceImpl();
    /**
     * Link to the in-memory storage for the birds
     */
    private Map<String, Bird> birds;
    /**
     * Link to the in-memory storage for the sightings
     */
    private Map<String, List<Sighting>> sightings;
    /**
     * Allowed command line parameters for the server
     */
    private final List<String> serverParameters = Arrays.asList("-port", "-data", "-proc_count");
    /**
     * A minimum allowed value for the server port
     */
    private final int minServerPort = 1;
    /**
     * A maximum allowed value for the server port
     */
    private final int maxServerPort = 65535;
    /**
     * The server port value. Initialized by default to 3000
     */
    private int serverPort = 3000;
    /**
     * The number of the workers which process different requests from the clients. Initialized by default to 2
     */
    private int procCount = 2;

    /**
     *  Location of the folder where server keeps its data store. By default is <i>serverdata</i> in user's home directory
     */
    private String folderToStore = System.getProperty("user.home") + File.separator + "serverdata";
    /**
     *  A name of a file where server stores information about birds
     */
    private final String birdsFileName = "birds.json";
    /**
     *  A name of a file where server stores information about sightings
     */
    private final String sightingsFileName = "sightings.json";

    /**
     *  Task periodically saves the contents of in-memory data-structure to the persistent data store
     */
    private class PersistenceTask extends TimerTask {

        /**
         * Run task in a separate thread
         */
        @Override
        public void run() {
            try {
                fileService.writeToFile(birds, folderToStore + File.separator + birdsFileName);
                fileService.writeToFile(sightings, folderToStore + File.separator + sightingsFileName);
            } catch (IOException e) {
                System.err.println("There is an exception during persistence to file: " + e);
            }
        }
    }

    /**
     * Start of SocketServer
     *
     * @param args the commandline arguments
     * @throws Exception
     */
    public void start(String args[]) throws Exception {

        parseCommandlineArgs(args);

        fileService.checkFilesOrCreate(folderToStore, birdsFileName, sightingsFileName);
        birds = fileService.readBirdsFromFile(folderToStore + File.separator + birdsFileName);
        sightings = fileService.readSightingsFromFile(folderToStore + File.separator + sightingsFileName);

        PersistenceTask persistenceTask = new PersistenceTask();
        Timer timer = new Timer(true);
        // save in-memory data structure to file system every 10 seconds
        timer.scheduleAtFixedRate(persistenceTask, 0, 10*1000);

        System.out.println("Server Listening......");

        try (ServerSocket ss = new ServerSocket(serverPort)) {
            ExecutorService executor = Executors.newFixedThreadPool(procCount);
            // connection timeout set to 5 seconds
            ss.setSoTimeout(5*1000);
            List<Future<Boolean>> results = new ArrayList<>();
            boolean stopRequired = false;
            while (!stopRequired){
                try {
                    Socket s = ss.accept();
                    System.out.println("connection Established");
                    Callable<Boolean> callable = new ServerWorker(s, birds, sightings);
                    Future<Boolean> isStopping = executor.submit(callable);
                    results.add(isStopping);
                } catch (SocketTimeoutException timeoutException) {
                    //do nothing, just go to the next circle
                }
                List<Future> futureToRemove = new ArrayList<>();
                for (Future<Boolean> future : results) {
                    if (future.isDone()) {
                        if (future.get()) {
                            stopRequired = true;
                            System.out.println("Server is shutting down");
                            break;
                        } else {
                            futureToRemove.add(future);
                        }
                    }
                }
                results.removeAll(futureToRemove);
            }
            executor.shutdown();
        } catch(Exception e){
            System.err.println("Server error: " + e.getMessage());
        } finally {
            timer.cancel();
            fileService.writeToFile(birds, folderToStore + File.separator + birdsFileName);
            fileService.writeToFile(sightings, folderToStore + File.separator + sightingsFileName);
        }
        System.out.println("Server is shutdown");
    }

    /**
     * Parsing of the command line arguments
     *
     * @param commandlineArgs the command line arguments
     */
    private void parseCommandlineArgs(String[] commandlineArgs) {
        String option = "";
        boolean isNextValue = false;
        for (String parameter : commandlineArgs) {
            if (serverParameters.contains(parameter)) {
                option = parameter;
                isNextValue = true;
            } else if (isNextValue) {
                switch (option) {
                    case "-port":
                        serverPort = Integer.parseInt(parameter);
                        if (serverPort < minServerPort || serverPort > maxServerPort) {
                            throw new IllegalArgumentException("Server port should be in a range 1..65535");
                        }
                        break;
                    case "-data":
                        folderToStore = parameter;
                        break;
                    case "-proc_count":
                        procCount = Integer.parseInt(parameter);
                        if (procCount < 1) {
                            throw new IllegalArgumentException("proc_count parameter should be a positive value");
                        }
                }
                isNextValue = false;
            }
        }
    }

}

