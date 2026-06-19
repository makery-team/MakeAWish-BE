package org.makery.controller;

import lombok.RequiredArgsConstructor;
import org.makery.domain.PrincipalDetails;
import org.makery.dto.AiAgentRequest;
import org.makery.dto.AiAgentResponse;
import org.makery.service.AiAgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai-agent")
@RequiredArgsConstructor
public class AiChatController {

    private final AiAgentService aiAgentService;

    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AiAgentResponse> chat(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody AiAgentRequest request) {

        AiAgentResponse response = aiAgentService.handleUserChat(
                principalDetails.user(),
                request.message(),
                request.productId()
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> clearChatHistory(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        aiAgentService.clearChatHistory(principalDetails.user());
        return ResponseEntity.ok().build();
    }
}
