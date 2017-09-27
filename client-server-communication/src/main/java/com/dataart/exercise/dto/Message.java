package com.dataart.exercise.dto;

import com.dataart.exercise.Action;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO used for communication between the client and the server
 *
 * @author Eugene Lapin
 * @version 1.0
 */
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Getter
    @Setter
    Action action;

    @Getter
    @Setter
    Object object;

    @Getter
    @Setter
    String comment;

    /**
     * Constructor for Message
     *
     * @param action current action
     * @param object transferred object
     */
    public Message (Action action, Object object) {
        this.action = action;
        this.object = object;
    }

    /**
     * Constructor for Message
     *
     * @param comment comment from the server
     */
    public Message (String comment) {
        this.comment = comment;
    }
}
