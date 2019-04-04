package io.audd.example.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FileUploader {
    private static final int BufferSizeBytes = 2048;
    private final String boundary;
    private final File file;
    private final String valueName;

    FileUploader(File file, String boundary, String valueName) {
        this.boundary = boundary;
        this.valueName = valueName;
        this.file = file;
    }

    long getContentLength() {
        return (this.file.length() + ((long) getFileDescription().length())) + ((long) getBoundaryEnd().length());
    }

    String getFileDescription() {
        return "\r\n--" + this.boundary + "\r\nContent-Disposition: form-data; name=\"" + this.valueName + "\"; filename=\"" + this.file.getName() + "\"\r\nContent-Type: %s\r\n\r\n";
    }

    private String getBoundaryEnd() {
        return String.format("\r\n--%s--\r\n", new Object[]{this.boundary});
    }

    void writeTo(OutputStream outputStream) throws IOException {
        outputStream.write(getFileDescription().getBytes("UTF-8"));
        FileInputStream reader = new FileInputStream(this.file);
        byte[] fileBuffer = new byte[2048];
        while (true) {
            int bytesRead = reader.read(fileBuffer);
            if (bytesRead != -1) {
                outputStream.write(fileBuffer, 0, bytesRead);
            } else {
                reader.close();
                outputStream.write(getBoundaryEnd().getBytes("UTF-8"));
                return;
            }
        }
    }
}
