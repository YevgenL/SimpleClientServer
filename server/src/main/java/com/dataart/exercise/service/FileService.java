package com.dataart.exercise.service;

import com.dataart.exercise.entity.Bird;
import com.dataart.exercise.entity.Sighting;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Interface for a service to work with a file system.
 *
 * @author Eugene Lapin
 * @version 1.0
 */
public interface FileService {

    /**
     * Create directory/files if no exists, check files permissions
     *
     * @param folderPath path to directory
     * @param fileNames fileNames array
     * @throws IOException if no permissions to create a directory or r/w access to files
     */
    void checkFilesOrCreate(String folderPath, String... fileNames) throws IOException;
    /**
     * Write data from Map to File
     * @param map map of objects to save
     * @param filePath path to a file where data should be saved
     * @throws IOException
     */
    void writeToFile(Map<String, ?> map, String filePath) throws IOException;
    /**
     * Read birds from a file
     *
     * @param filePath path to a file to read data from
     * @return Map of birds
     * @throws IOException
     * @see com.dataart.exercise.entity.Bird
     */
    Map<String, Bird> readBirdsFromFile(String filePath) throws IOException;
    /**
     * Read sightings from a file
     *
     * @param filePath path to a file to read data from
     * @return Map of sightings
     * @throws IOException
     */
    Map<String, List<Sighting>> readSightingsFromFile(String filePath) throws IOException;
}
