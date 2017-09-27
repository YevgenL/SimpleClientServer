package com.dataart.exercise.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity stores an information about a bird
 *
 * @author Eugene Lapin
 * @version 1.0
 */
@NoArgsConstructor
@AllArgsConstructor
public class Bird {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String color;

    @Getter
    @Setter
    private String weight;

    @Getter
    @Setter
    private String height;
}
