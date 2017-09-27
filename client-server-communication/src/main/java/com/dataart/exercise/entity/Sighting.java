package com.dataart.exercise.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity stores an information about a sighting
 *
 * @author Eugene Lapin
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sighting {

    private String birdName;

    private String location;

    private LocalDateTime sightingDateTime;
}