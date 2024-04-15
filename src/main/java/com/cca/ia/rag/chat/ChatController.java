package com.cca.ia.rag.chat;

import com.cca.ia.rag.chat.model.MessageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/{collectionId}")
    public MessageDto sendQuestion(@PathVariable Long collectionId, @RequestParam(value = "question") String question) {
        MessageDto answer = this.chatService.sendQuestion(collectionId, question);
        return answer;
    }
}
