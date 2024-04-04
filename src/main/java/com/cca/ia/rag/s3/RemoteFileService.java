package com.cca.ia.rag.s3;

import java.io.InputStream;

public interface RemoteFileService {

    void putObject(InputStream data, String pathName, String objectName) throws Exception;

    InputStream getObject(String pathName, String objectName) throws Exception;

    void deleteObject(String pathName, String objectName) throws Exception;

}


