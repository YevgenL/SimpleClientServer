package com.dataart.exercise.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.dataart.exercise.entity.Bird;
import com.dataart.exercise.entity.Sighting;
import com.dataart.exercise.service.FileService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service to work with a file system.
 *
 * @author Eugene Lapin
 * @version 1.0
 */
public class FileServiceImpl implements FileService {

    /**
     * Create directory/files if no exists, check files permissions
     *
     * @param folderPath path to directory
     * @param fileNames fileNames array
     * @throws IOException if no permissions to create a directory or r/w access to files
     */
    public void checkFilesOrCreate(String folderPath, String... fileNames) throws IOException {
        File directory = new File(folderPath);
        if (!directory.exists()) {
            directory.mkdir();
        }
        for (String fileName : fileNames) {
            File file = new File(folderPath + File.separator + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            if (!file.canRead()) {
                throw new IOException("No Read permission to file " + file.getAbsolutePath());
            }
            if (!file.canWrite()) {
                throw new IOException("No Write permission to file " + file.getAbsolutePath());
            }
        }
    }

    /**
     * Write data from Map to File
     * @param map map of objects to save
     * @param filePath path to a file where data should be saved
     * @throws IOException
     */
    public void writeToFile(Map<String, ?> map, String filePath) throws IOException {
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            JSONArray birdArray = new JSONArray(map.values().stream().map(JSON::toJSONString).collect(Collectors.toList()));
            birdArray.writeJSONString(fileWriter);
            fileWriter.flush();
        }
    }

    /**
     * Read birds from a file
     *
     * @param filePath path to a file to read data from
     * @return Map of birds
     * @throws IOException
     * @see com.dataart.exercise.entity.Bird
     */
    public Map<String, Bird> readBirdsFromFile(String filePath) throws IOException {
        try (FileInputStream is = new FileInputStream(new File(filePath))) {
            Map<String, Bird> birds = new ConcurrentHashMap<>();
            if (is.available() <= 0) {
                return birds;
            }
            JSONArray jsonArray = JSON.parseObject(is, JSONArray.class);
            jsonArray.stream().map(o -> JSON.parseObject(o.toString(), Bird.class)).forEach(o -> birds.putIfAbsent(((Bird)o).getName(), (Bird)o));
            return birds;
        }
    }

    /**
     * Read sightings from a file
     *
     * @param filePath path to a file to read data from
     * @return Map of sightings
     * @throws IOException
     */
    public Map<String, List<Sighting>> readSightingsFromFile(String filePath) throws IOException {
        try (FileInputStream is = new FileInputStream(new File(filePath))) {
            Map<String, List<Sighting>> sightings = new ConcurrentHashMap<>();
            if (is.available() <= 0) {
                return sightings;
            }
            JSONArray jsonArray = JSON.parseObject(is, JSONArray.class);
            for (Object jsonObject : jsonArray) {
                Sighting[] sightingArray = JSON.parseObject(jsonObject.toString(), Sighting[].class);
                for (Sighting sighting : sightingArray) {
                    List<Sighting> sightingList = sightings.getOrDefault(sighting.getBirdName(), new ArrayList());
                    sightingList.add(sighting);
                    sightings.put(sighting.getBirdName(), sightingList);
                }
            }
            return sightings;
        }
    }
}
