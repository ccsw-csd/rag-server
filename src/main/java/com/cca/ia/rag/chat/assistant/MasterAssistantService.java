package com.cca.ia.rag.chat.assistant;

import com.cca.ia.rag.chat.assistant.model.RequestContextDto;
import com.cca.ia.rag.chat.assistant.model.ResponseContextDto;
import com.cca.ia.rag.collection.CollectionService;
import com.cca.ia.rag.collection.model.CollectionPropertyDto;
import com.cca.ia.rag.collection.model.CollectionPropertyEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MasterAssistantService implements AssistantService {

    @Autowired
    private CollectionService collectionService;

    @Autowired
    @Qualifier("sqlAssistantService")
    private AssistantService sqlAssistantService;

    @Autowired
    @Qualifier("chatAssistantService")
    private AssistantService chatAssistantService;

    @Override
    public ResponseContextDto process(RequestContextDto request) {

        long start = System.currentTimeMillis();

        List<CollectionPropertyDto> properties = collectionService.findProperties(request.getCollection().getId());
        List<CollectionPropertyEntity> prompts = collectionService.findPrompts(request.getCollection().getId());

        request.setCollectionProperties(properties);
        request.setCollectionPrompts(prompts);
        request.parseQuery();

        ResponseContextDto response = null;

        if (request.isQuery()) {
            response = sqlAssistantService.process(request);
        } else {
            response = chatAssistantService.process(request);
        }

        response.setRequest(request.getQuestion());
        response.setDocuments(request.getDocuments());

        long end = System.currentTimeMillis();

        response.setSpentTime(end - start);

        return response;
    }
}
