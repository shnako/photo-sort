package com.shnako.photosort;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class FileUtils {
    static List<Path> getImagesInDirectory(Path folder) throws IOException {
        try (Stream<Path> paths = Files.walk(folder)) {
            return paths.filter(Files::isRegularFile).collect(Collectors.toList());
        }
    }

    static void renameFiles(Map<Path, String> newFileNames) throws IOException {
        for (Map.Entry<Path, String> newFileName : newFileNames.entrySet()) {
            Path targetDirectory = newFileName.getKey().getParent();
            Files.move(newFileName.getKey(), targetDirectory.resolve(newFileName.getValue()));
        }
    }
}
