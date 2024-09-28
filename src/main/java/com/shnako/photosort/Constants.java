package com.shnako.photosort;

import com.drew.metadata.mov.QuickTimeDirectory;
import com.drew.metadata.mov.media.QuickTimeVideoDirectory;

public class Constants {
    public static final String SEPARATOR = "_";
    public static final String EXT_SEPARATOR = ".";
    public static final String FILE_NAME_FORMAT = "yyyyMMdd" + SEPARATOR + "HHmmss";
    public static final int TAG_EXIF_SUB_DATETIME = 36868;
    public static final int TAG_EXIF_DATETIME = 306;
    public static final int[] TAGS_QUICKTIME_DATETIME = new int[]{QuickTimeDirectory.TAG_CREATION_TIME, QuickTimeVideoDirectory.TAG_CREATION_TIME, QuickTimeDirectory.TAG_MODIFICATION_TIME, QuickTimeVideoDirectory.TAG_MODIFICATION_TIME};
}
