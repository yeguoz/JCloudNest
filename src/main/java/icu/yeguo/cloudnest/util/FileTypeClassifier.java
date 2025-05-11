package icu.yeguo.cloudnest.util;

import java.util.*;

public class FileTypeClassifier {

    private static final Set<String> VIDEO_EXTENDS = Set.of("mp4", "mkv", "avi", "mov", "flv", "wmv", "webm");
    private static final Set<String> IMAGE_EXTENDS = Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "tiff");
    private static final Set<String> AUDIO_EXTENDS = Set.of("mp3", "wav", "aac", "flac", "ogg", "wma", "m4a");
    private static final Set<String> DOCUMENT_EXTENDS = Set.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md", "csv");
    private static final Set<String> ARCHIVE_EXTENDS = Set.of("zip", "rar", "7z", "tar", "gz", "bz2", "xz");
    private static final Set<String> EXECUTABLE_EXTENDS = Set.of("exe", "msi", "apk", "bat", "sh", "jar");

    public static String classifyFile(String ext) {
        if (ext == null) return "other";

        String extLower = ext.toLowerCase();

        if (IMAGE_EXTENDS.contains(extLower)) return "image";
        if (VIDEO_EXTENDS.contains(extLower)) return "video";
        if (AUDIO_EXTENDS.contains(extLower)) return "audio";
        if (DOCUMENT_EXTENDS.contains(extLower)) return "document";
        if (ARCHIVE_EXTENDS.contains(extLower)) return "archive";
        if (EXECUTABLE_EXTENDS.contains(extLower)) return "executable";

        return "other";
    }
}
