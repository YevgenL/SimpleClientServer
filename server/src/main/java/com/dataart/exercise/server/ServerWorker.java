package com.dataart.exercise.server;

import com.alibaba.fastjson.JSON;
import com.dataart.exercise.dto.Message;
import com.dataart.exercise.entity.Bird;
import com.dataart.exercise.entity.Sighting;
import com.dataart.exercise.request.SightingsRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * A server worker executes the server logic in a separate thread
 *
 * @author Eugene Lapin
 * @version 1.0
 */
public class ServerWorker implements Callable<Boolean> {
    /**
     * Socket for current server worker
     */
    private final Socket s;
    /**
     * Link to the in-memory storage for the birds
     */
    private final Map<String, Bird> birds;
    /**
     * Link to the in-memory storage for the sightings
     */
    private final Map<String, List<Sighting>> sightings;
    /**
     * If TRUE stop the server
     */
    private boolean isStoppingServer;

    /**
     * Constructor for ServiceWorker creation
     *
     * @param s socket
     * @param birds map of the birds
     * @param sightings map of the sightings
     */
    public ServerWorker(Socket s, Map<String, Bird> birds, Map<String, List<Sighting>> sightings){
        this.s = s;
        this.birds = birds;
        this.sightings = sightings;
    }

    /**
     * Run ServerWorker in a separate thread
     *
     * @return Boolean if server should be stopped
     * @throws Exception
     */
    public Boolean call() throws Exception {
        try (BufferedReader is = new BufferedReader(new InputStreamReader(s.getInputStream()));
             PrintWriter os = new PrintWriter(s.getOutputStream())) {
            isStoppingServer = false;
            String line = is.readLine();
            Message request = JSON.parseObject(line, Message.class);
            if (request == null) {
                return false;
            }
            Message response = handleMessage(request);
            os.println(JSON.toJSONString(response));
            os.flush();
            System.out.println("Response to Client  :  " + JSON.toJSONString(response));
        } finally {
            try{
                System.out.println("Connection Closing..");
                if (!s.isClosed()){
                    System.out.println("Socket Closing");
                    s.close();
                    System.out.println("Socket Closed");
                }
            } catch(IOException ie){
                System.err.println("Socket Close Error");
            }
        }
        return isStoppingServer;
    }

    /**
     * Handle a request from a client and prepare a response
     *
     * @param message from the client
     * @return Message response to the client
     */
    private Message handleMessage(Message message) {
        Object object = message.getObject();
        switch (message.getAction()) {
            case ADD_BIRD:
                Bird bird = JSON.parseObject(object.toString(), Bird.class);
                if (birds.containsKey(bird.getName())) {
                    return new Message("FAILURE: Bird " + bird.getName() + " already exists");
                } else {
                    birds.put(bird.getName(), bird);
                    return new Message("Bird " + bird.getName() + " successfully added to the database");
                }
            case ADD_SIGHTING:
                Sighting sighting = JSON.parseObject(object.toString(), Sighting.class);
                if (!birds.containsKey(sighting.getBirdName())) {
                    return new Message("FAILURE: Sighting was not added because Bird " + sighting.getBirdName() + " does not exist");
                }
                List<Sighting> sightingList = sightings.getOrDefault(sighting.getBirdName(), new ArrayList());
                if (sightingList.contains(sighting)) {
                    return new Message("FAILURE: Sighting with such parameters already exists");
                } else {
                    sightingList.add(sighting);
                    sightings.putIfAbsent(sighting.getBirdName(), sightingList);
                    return new Message("Sighting for " + sighting.getBirdName() + " successfully added to the database");
                }
            case LIST_BIRDS:
                return new Message(message.getAction(), birds.values());
            case LIST_SIGHTING:
                SightingsRequest sightingsRequest = JSON.parseObject(object.toString(), SightingsRequest.class);
                try {
                    sightingList = sightings.values().stream().flatMap(list -> list.stream())
                            .filter(s -> s.getBirdName().matches(sightingsRequest.getBirdNamePattern())
                                    && s.getSightingDateTime().isAfter(LocalDateTime.of(sightingsRequest.getSightingDateStart(), LocalTime.MIN))
                                    && s.getSightingDateTime().isBefore(LocalDateTime.of(sightingsRequest.getSightingDateEnd(), LocalTime.MAX)))
                            .collect(Collectors.toList());
                    return new Message(message.getAction(), sightingList);
                } catch (PatternSyntaxException e) {
                    return new Message("Pattern error: " + e.getMessage());
                }
            case REMOVE:
                if (!birds.containsKey(object)) {
                    return new Message("FAILURE: Bird " + object + " does not exist");
                }
                birds.remove(object);
                sightings.remove(object);
                return new Message("Bird " + object + " successfully removed from database");
            case QUIT:
                isStoppingServer = true;
        }
        return new Message();
    }
}
