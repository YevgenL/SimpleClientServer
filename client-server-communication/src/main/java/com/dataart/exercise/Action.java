package com.dataart.exercise;

/**
 * Allowed actions on the server
 *
 * @author Eugene Lapin
 * @version 1.0
 */
public enum Action {

    ADD_BIRD ("-addbird"),
    ADD_SIGHTING ("-addsighting"),
    LIST_BIRDS ("-listbirds"),
    LIST_SIGHTING ("-listsightings"),
    REMOVE ("-remove"),
    QUIT ("-quit");

    /**
     * A command line value for an Action
     */
    public final String param;

    Action(String param) {
        this.param = param;
    };

}
