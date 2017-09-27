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
public class MessageDto {

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
     * Constructor for MessageDto
     *
     * @param action current action
     * @param object transferred object
     */
    public MessageDto(Action action, Object object) {
        this.action = action;
        this.object = object;
    }

    /**
     * Constructor for MessageDto
     *
     * @param comment comment from the server
     */
    public MessageDto(String comment) {
        this.comment = comment;
    }
}
