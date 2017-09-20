package com.shnako.photosort;

import com.shnako.photosort.sorters.DateTimeSorter;
import com.shnako.photosort.sorters.Sorter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class App {
    public static void main(String[] args) throws IOException {
        Scanner scanInput = new Scanner(System.in);
        Sorter sorter = new DateTimeSorter();
        Path folderToScanPath;

        System.out.println("I'll be using the " + sorter.getClass() + ".");
        while(true) {
            System.out.println("Please specify the path to the folder containing the images to sort: ");
            String folderToScan = scanInput.nextLine().trim();
            folderToScanPath = Paths.get(folderToScan);
            if (folderToScanPath.toFile().isDirectory()) {
                break;
            }
            System.out.println("Invalid path. Please try again.");
        }

        List<Path> images = FileUtils.getImagesInDirectory(folderToScanPath);
        Map<Path, String> newImageNames = sorter.getNewFileNames(images);
        for (Map.Entry<Path, String> newImageName : newImageNames.entrySet()) {
            System.out.println(newImageName.getKey().getFileName().toString() + " -> " + newImageName.getValue());
        }

        System.out.println("\r\n\r\nDo you want to execute the following renames? yes/no");
        String shouldRename = scanInput.nextLine();
        shouldRename = shouldRename.trim().substring(0, 1).toUpperCase();
        if ("Y".equals(shouldRename)) {
            FileUtils.renameFiles(newImageNames);
            System.out.println("The files have been successfully renamed!");
        }
    }
}
