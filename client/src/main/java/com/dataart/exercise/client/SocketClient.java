package com.dataart.exercise.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dataart.exercise.dto.Message;
import com.dataart.exercise.Action;
import com.dataart.exercise.entity.Bird;
import com.dataart.exercise.entity.Sighting;
import com.dataart.exercise.request.SightingsRequest;

/**
 * Client class
 *
 * @author Eugene Lapin
 * @version 1.0
 */
public class SocketClient {

    /**
     * A name of the server port parameter
     */
    private final String serverPortParameter = "-serverPort";
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
     * An action selected for execution on the server
     */
    private String action;

    /**
     * Start of SocketClient
     *
     * @param args the command line arguments
     * @throws Exception
     */
    public void start(String args[]) throws Exception {

        parseCommandlineArgs(args);

        if (action == null) {
            System.err.println("No action was chosen");
            return;
        }

        try (Socket s = new Socket(InetAddress.getLocalHost(), serverPort);
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                BufferedReader is = new BufferedReader(new InputStreamReader(s.getInputStream()));
                PrintWriter os = new PrintWriter(s.getOutputStream())) {

            Message request = prepareRequest(br);
            if (request == null) {
                return;
            }
            os.println(JSON.toJSONString(request));
            os.flush();

            handleResponse(JSON.parseObject(is.readLine(), Message.class));
        } catch (DateTimeParseException e) {
            System.err.println("ERROR: Date and/or time entered in a wrong format");
        } catch (IOException e){
            e.printStackTrace();
            System.err.println("Socket read Error");
        }
    }

    /**
     * Parsing of the command line arguments
     *
     * @param commandlineArgs the command line arguments
     */
    private void parseCommandlineArgs(String[] commandlineArgs) {
        boolean isPortValueNext = false;
        for (String parameter : commandlineArgs) {
            if (actionFromParam(parameter) != null) {
                action = parameter;
            } else if (serverPortParameter.equals(parameter)) {
                isPortValueNext = true;
            } else if (isPortValueNext) {
                serverPort = Integer.parseInt(parameter);
                if (serverPort < minServerPort || serverPort > maxServerPort) {
                    throw new IllegalArgumentException("Server port should be in a range 1..65535");
                }
                isPortValueNext = false;
            }
        }
    }

    /**
     * Action by its string <b>param</b> value
     *
     * @param parameter
     * @return Action
     * @see com.dataart.exercise.Action
     */
    private Action actionFromParam(String parameter) {
        for (Action action : Action.values()) {
            if (action.param.equalsIgnoreCase(parameter)) {
                return action;
            }
        }
        return null;
    }

    /**
     * Prepare request to the server
     *
     * @param br
     * @return Message that will be send to server
     * @throws IOException if there is an issue when read from <b>br</b>
     * @see com.dataart.exercise.dto.Message
     */
    private Message prepareRequest(BufferedReader br) throws IOException {
        switch (actionFromParam(action)) {
            case ADD_BIRD:
                System.out.println("You selected an option to add information about a bird.\n");
                Bird bird = new Bird();
                System.out.print("Please enter name of the bird and then press ENTER: " );
                String value = br.readLine();
                if (value.isEmpty()) {
                    System.err.println("Name of a bird cannot be empty.");
                    return null;
                }
                bird.setName(value);
                System.out.print("Please enter color of the bird and then press ENTER: ");
                bird.setColor(br.readLine());
                System.out.print("Please enter weight of the bird and then press ENTER: ");
                bird.setWeight(br.readLine());
                System.out.print("Please enter height of the bird and then press ENTER: ");
                bird.setHeight(br.readLine());
                return new Message(Action.ADD_BIRD, bird, null);
            case ADD_SIGHTING:
                System.out.println("You selected an option to add information about a sighting.\n");
                Sighting sighting = new Sighting();
                System.out.print("Please enter name of the bird and then press ENTER: " );
                value = br.readLine();
                if (value.isEmpty()) {
                    System.err.println("Name of a bird cannot be empty.");
                    return null;
                }
                sighting.setBirdName(value);
                System.out.print("Please enter a location and then press ENTER :");
                sighting.setLocation(br.readLine());
                System.out.print("Please enter data and time in a format 'yyyy-MM-dd HH:mm' and then press ENTER: ");
                String[] values = br.readLine().split(" ");
                sighting.setSightingDateTime(LocalDateTime.parse(values[0] + " " + values[1], DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                return new Message(Action.ADD_SIGHTING, sighting, null);
            case LIST_BIRDS:
                System.out.println("You selected an option to see a list of the birds.\n");
                return new Message(Action.LIST_BIRDS, null);
            case LIST_SIGHTING:
                System.out.println("You selected an option to see a list of the sightings.\n");
                SightingsRequest sightingsRequest = new SightingsRequest();
                System.out.print("Please enter name of the bird as a pattern and then press ENTER: ");
                sightingsRequest.setBirdNamePattern(br.readLine());
                System.out.print("Please enter start date of a period you are looking for the sights (please use format yyyy-MM-dd) and then press ENTER: ");
                DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                sightingsRequest.setSightingDateStart(LocalDate.parse(br.readLine(), pattern));
                System.out.print("Please enter end date of a period you are looking for the sights (please use format yyyy-MM-dd) and then press ENTER: ");
                sightingsRequest.setSightingDateEnd(LocalDate.parse(br.readLine(), pattern));
                return new Message(Action.LIST_SIGHTING, sightingsRequest);
            case REMOVE:
                System.out.println("You selected an option to remove information about a bird.\n");
                System.out.print("Please enter name of the bird and then press ENTER: ");
                return new Message(Action.REMOVE, br.readLine());
            case QUIT:
                System.out.println("You selected an option to shutdown the server.\n");
                return new Message(Action.QUIT, null);
        }
        return null;
    }

    /**
     * Handle a response from the server
     *
     * @param message response from the server
     */
    private void handleResponse(Message message) {
        switch (actionFromParam(action)) {
            case ADD_BIRD:
            case ADD_SIGHTING:
            case REMOVE:
                System.out.println("\n" + message.getComment());
                break;
            case LIST_BIRDS:
                System.out.println("A list of the birds:\n");
                System.out.println("| NAME | COLOR | WEIGHT | HEIGHT |");
                List<JSONObject> birdListJson = (List<JSONObject>) message.getObject();
                List<Bird> birdList = birdListJson.stream()
                        .map(o -> JSON.parseObject(o.toString(), Bird.class))
                        .sorted(Comparator.comparing(Bird::getName))
                        .collect(Collectors.toList());
                birdList.forEach(b -> System.out.println("| " + b.getName() + " | " + b.getColor() + " | " + b.getWeight() + " | " + b.getHeight() + " |"));
                System.out.println("\nTOTAL: " + birdList.size());
                break;
            case LIST_SIGHTING:
                if (message.getObject() == null) {
                    System.err.println(message.getComment());
                } else {
                    System.out.println("A list of the sightings:\n");
                    System.out.println("| BIRD NAME | DATE |");
                    List<JSONObject> sightingListJson = (List<JSONObject>) message.getObject();
                    List<Sighting> sightingList = sightingListJson.stream()
                            .map(o -> JSON.parseObject(o.toString(), Sighting.class))
                            .sorted(Comparator.comparing(Sighting::getBirdName).thenComparing(Sighting::getSightingDateTime))
                            .collect(Collectors.toList());
                    sightingList.forEach(s -> System.out.println("| " + s.getBirdName() + " | " + s.getSightingDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
                }
        }
    }
}
