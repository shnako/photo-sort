package com.shnako.photosort.sorters;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.mov.QuickTimeDirectory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.shnako.photosort.Constants.EXT_SEPARATOR;
import static com.shnako.photosort.Constants.FILE_NAME_FORMAT;
import static com.shnako.photosort.Constants.SEPARATOR;
import static com.shnako.photosort.Constants.TAGS_QUICKTIME_DATETIME;
import static com.shnako.photosort.Constants.TAG_EXIF_DATETIME;
import static com.shnako.photosort.Constants.TAG_EXIF_SUB_DATETIME;

public class DateTimeSorter implements Sorter {
    private final Pattern datePattern = Pattern.compile("20[0-9]{6}");

    /**
     * Determines new names for files based on their EXIF creation dates or file names.
     *
     * @param paths - The Path objects for the files to analyze.
     * @return A map of file path objects to the suggested new names.
     */
    public Map<Path, String> getNewFileNames(List<Path> paths, String prefix) {
        Map<String, Path> fileNames = new HashMap<>(paths.size());

        for (Path path : paths) {
            try {
                Metadata metadata = ImageMetadataReader.readMetadata(path.toFile());

                DateTime dateTime = getDateTimeFromSubExif(metadata);
                if (dateTime == null) {
                    dateTime = getDateTimeFromExif(metadata);
                }
                if (dateTime == null) {
                    dateTime = getDateTimeFromQuicktime(metadata);
                }

                String newPath;
                if (dateTime != null) {
                    newPath = dateTime.withZone(DateTimeZone.UTC).toString(FILE_NAME_FORMAT);
                } else {
                    newPath = getNewFileNameFromExistingFileName(path);
                }

                if (StringUtils.isNotBlank(newPath)) {
                    if (StringUtils.isNotBlank(prefix)) {
                        newPath = prefix + SEPARATOR + newPath;
                    }
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

    private DateTime getDateTimeFromQuicktime(Metadata metadata) {
        try {
            Collection<QuickTimeDirectory> quicktimeTags = metadata.getDirectoriesOfType(QuickTimeDirectory.class);

            if (quicktimeTags.isEmpty()) {
                return null;
            }

            for (int tagId : TAGS_QUICKTIME_DATETIME) {
                Optional<Date> dateTime = quicktimeTags
                        .stream()
                        .filter(tag -> tag.containsTag(tagId))
                        .map(x -> x.getDate(tagId))
                        .findAny();
                if (dateTime.isPresent()) {
                    return new DateTime(dateTime.get());
                }
            }

            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    private String getNewFileNameFromExistingFileName(Path path) {
        String fileName = path.getFileName().toString();
        Matcher regexMatcher = datePattern.matcher(fileName);
        if (regexMatcher.find()) {
            int dateStringStart = regexMatcher.start();
            if (dateStringStart > 0 && Character.isDigit(fileName.charAt(regexMatcher.start() - 1))) {
                // It's just part of a longer number.
                return null;
            }
            fileName = fileName.substring(regexMatcher.start());
            if (Character.isDigit(fileName.charAt(8))) {
                fileName = fileName.substring(0, 8) + "_" + fileName.substring(8);
            }
            fileName = FilenameUtils.removeExtension(fileName);
            return fileName;
        }
        return null;
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
