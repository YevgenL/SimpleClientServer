package com.dataart.exercise.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Request to get a sighting list
 *
 * @author Eugene Lapin
 * @version 1.0
 * @see com.dataart.exercise.entity.Sighting
 */
@NoArgsConstructor
@AllArgsConstructor
public class SightingsRequest {

    @Getter
    @Setter
    private String birdNamePattern;

    @Getter
    @Setter
    private LocalDate sightingDateStart;

    @Getter
    @Setter
    private LocalDate sightingDateEnd;
}
