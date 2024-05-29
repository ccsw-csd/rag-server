package com.cca.ia.rag.chat.assistant.database;

public interface DynamicRepository {
    String launchQuery(String query, String url, String username, String password) throws Exception;
}
