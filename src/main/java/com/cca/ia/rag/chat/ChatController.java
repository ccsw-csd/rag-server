package com.cca.ia.rag.chat;

import com.cca.ia.rag.chat.model.ConversationEntity;
import com.cca.ia.rag.chat.model.EmbeddingMessage;
import com.cca.ia.rag.chat.model.MessageDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ModelMapper mapper;

    @GetMapping("/{collectionId}/question")
    public MessageDto sendQuestion(@PathVariable Long collectionId, @RequestParam(value = "question") String question) {
        ConversationEntity answer = this.chatService.sendQuestion(collectionId, question);
        return mapper.map(answer, MessageDto.class);
    }

    @GetMapping("/{chatId}")
    public List<MessageDto> getMessagesFromChat(@PathVariable Long chatId) {
        List<ConversationEntity> messages = this.chatService.findByChatId(chatId);

        return messages.stream().map(e -> mapper.map(e, MessageDto.class)).collect(Collectors.toList());
    }

    @GetMapping("/embeddings/{messageId}")
    public List<EmbeddingMessage> getEmbeddingsFromMessageId(@PathVariable Long messageId) {
        return this.chatService.getEmbeddingsFromMessageId(messageId);
    }

}
