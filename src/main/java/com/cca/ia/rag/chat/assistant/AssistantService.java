package com.cca.ia.rag.chat.assistant;

import com.cca.ia.rag.chat.assistant.model.RequestContextDto;
import com.cca.ia.rag.chat.assistant.model.ResponseContextDto;

public interface AssistantService {

    ResponseContextDto process(RequestContextDto request);

}
