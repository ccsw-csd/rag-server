package com.cca.ia.rag.chat;

import com.cca.ia.rag.chat.model.MessageDto;

public interface ChatService {

    MessageDto sendQuestion(Long collectionId, String question);

}
