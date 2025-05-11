package icu.yeguo.cloudnest.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletResponse;

public class FileHandlerUtils {

    public static void previewFile(Path filePath, String fileName, HttpServletResponse response) throws IOException {
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "inline; filename=\""
                + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"");
        response.setContentLengthLong(Files.size(filePath));
        transferFileContent(filePath, response);
    }

    public static void downloadFile(Path filePath, String fileName, HttpServletResponse response) throws IOException {
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replace("+", "%20");

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + encodedFileName + "\";" +
                        "filename*=UTF-8''" + encodedFileName);
        response.setContentLengthLong(Files.size(filePath));
        transferFileContent(filePath, response);
    }

    private static void transferFileContent(Path filePath, HttpServletResponse response) throws IOException {
        try (InputStream inputStream = Files.newInputStream(filePath);
             OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[16384];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        }
    }
}
