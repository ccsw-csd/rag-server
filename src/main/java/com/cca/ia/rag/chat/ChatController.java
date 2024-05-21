package com.cca.ia.rag.chat;

import com.cca.ia.rag.chat.model.*;
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

    @GetMapping("/{chatId}/question")
    public MessageDto sendQuestion(@PathVariable Long chatId, @RequestParam(value = "question") String question) {
        ConversationEntity answer = this.chatService.sendQuestion(chatId, question);
        return mapper.map(answer, MessageDto.class);
    }

    @PutMapping("/create-by-collection/{collectionId}")
    public ChatDto createChatByCollectionId(@PathVariable Long collectionId, @RequestBody ChatDto data) {
        ChatEntity chat = this.chatService.createChatByCollectionId(collectionId, data);
        return mapper.map(chat, ChatDto.class);
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

    @GetMapping("/list-by-collection/{collectionId}")
    public List<ChatDto> findChatsByCollectionId(@PathVariable Long collectionId) {
        List<ChatEntity> chats = this.chatService.findChatsByCollectionId(collectionId);

        return chats.stream().map(e -> mapper.map(e, ChatDto.class)).collect(Collectors.toList());
    }

}
