package com.shnako.photosort.sorters;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface Sorter {
    Map<Path, String> getNewFileNames(List<Path> paths, String prefix);
}
