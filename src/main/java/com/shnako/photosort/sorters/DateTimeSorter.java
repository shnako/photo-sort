package com.shnako.photosort.sorters;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.shnako.photosort.Constants.EXT_SEPARATOR;
import static com.shnako.photosort.Constants.FILE_NAME_FORMAT;
import static com.shnako.photosort.Constants.SEPARATOR;
import static com.shnako.photosort.Constants.TAG_EXIF_SUB_DATETIME;
import static com.shnako.photosort.Constants.TAG_EXIF_DATETIME;

public class DateTimeSorter implements Sorter {
    /**
     * Determines new names for files based on their EXIF creation dates.
     *
     * @param paths - The Path objects for the files to analyze.
     * @return A map of file path objects to the suggested new names.
     */
    public Map<Path, String> getNewFileNames(List<Path> paths) {
        Map<String, Path> fileNames = new HashMap<>(paths.size());

        for (Path path : paths) {
            try {
                Metadata metadata = ImageMetadataReader.readMetadata(path.toFile());

                DateTime dateTime = getDateTimeFromSubExif(metadata);
                if (dateTime == null) {
                    dateTime = getDateTimeFromExif(metadata);
                }

                if (dateTime != null) {
                    String newPath = dateTime.toString(FILE_NAME_FORMAT);
                    addPathToMap(path, newPath, fileNames);
                }
            } catch (Exception e) {
                System.out.println("Could not determine time for " + path + ". This will not be renamed.");
            }
        }

        return flipAndAddExtensions(fileNames);
    }

    private DateTime getDateTimeFromSubExif(Metadata metadata) {
        try {
            Directory exifSubDirectory = metadata.getDirectoriesOfType(ExifSubIFDDirectory.class).iterator().next();
            return new DateTime(exifSubDirectory.getDate(TAG_EXIF_SUB_DATETIME));
        } catch (Exception ex) {
            return null;
        }
    }

    private DateTime getDateTimeFromExif(Metadata metadata) {
        try {
            Directory exifDirectory = metadata.getDirectoriesOfType(ExifIFD0Directory.class).iterator().next();
            return new DateTime(exifDirectory.getDate(TAG_EXIF_DATETIME));
        } catch (Exception ex) {
            return null;
        }
    }

    private void addPathToMap(Path path, String newPath, Map<String, Path> dateFileNames) {
        String updatedPath = newPath;

        // If we already have a file like this, add an increment to it.
        if (dateFileNames.containsKey(newPath)) {
            dateFileNames.put(newPath + SEPARATOR + "1", dateFileNames.remove(newPath));
        }

        // Update the new path to contain an increment if needed.
        if (dateFileNames.containsKey(newPath + SEPARATOR + "1")) {
            int increment = 2;
            while (dateFileNames.containsKey(newPath + SEPARATOR + increment)) {
                increment++;
            }
            updatedPath += SEPARATOR + increment;
        }

        dateFileNames.put(updatedPath, path);
    }

    private Map<Path, String> flipAndAddExtensions(Map<String, Path> dateFileNames) {
        return dateFileNames.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getValue,
                o -> o.getKey() + EXT_SEPARATOR + FilenameUtils.getExtension(o.getValue().toString())));
    }
}
