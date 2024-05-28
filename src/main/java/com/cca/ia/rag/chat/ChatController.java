package com.cca.ia.rag.chat;

import com.cca.ia.rag.chat.model.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CHAT')")
    @GetMapping("/{chatId}/question")
    public MessageDto sendQuestion(@PathVariable Long chatId, @RequestParam(value = "question") String question) {
        ConversationEntity answer = this.chatService.sendQuestion(chatId, question);
        return mapper.map(answer, MessageDto.class);
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CHAT')")
    @PutMapping("/create-by-collection/{collectionId}")
    public ChatDto createChatByCollectionId(@PathVariable Long collectionId, @RequestBody ChatDto data) {
        ChatEntity chat = this.chatService.createChatByCollectionId(collectionId, data);
        return mapper.map(chat, ChatDto.class);
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CHAT')")
    @GetMapping("/{chatId}")
    public List<MessageDto> getMessagesFromChat(@PathVariable Long chatId) {
        List<ConversationEntity> messages = this.chatService.findByChatId(chatId);

        return messages.stream().map(e -> mapper.map(e, MessageDto.class)).collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CHAT')")
    @DeleteMapping("/{chatId}")
    public void deleteChat(@PathVariable Long chatId) {
        this.chatService.deleteChat(chatId);
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CHAT')")
    @PostMapping("/{chatId}/rename")
    public void renameChat(@PathVariable Long chatId, @RequestBody ChatDto data) {
        this.chatService.renameChat(chatId, data.getTitle());
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CHAT')")
    @GetMapping("/embeddings/{messageId}")
    public List<ConversationContextDto> getEmbeddingsFromMessageId(@PathVariable Long messageId) {
        List<ConversationPropertiesEntity> context = this.chatService.getEmbeddingsFromMessageId(messageId);

        return context.stream().map(e -> mapper.map(e, ConversationContextDto.class)).collect(Collectors.toList());

    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CHAT')")
    @GetMapping("/list-by-collection/{collectionId}")
    public List<ChatDto> findChatsByCollectionId(@PathVariable Long collectionId) {
        List<ChatEntity> chats = this.chatService.findChatsByCollectionId(collectionId);

        return chats.stream().map(e -> mapper.map(e, ChatDto.class)).collect(Collectors.toList());
    }

}
