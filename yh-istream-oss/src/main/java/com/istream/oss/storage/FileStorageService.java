package com.istream.oss.storage;

import java.io.InputStream;

public interface FileStorageService {

    String store(InputStream inputStream, String fileName, String mimeType);

    InputStream load(String filePath);

    void delete(String filePath);

    String getAccessUrl(String filePath);
}