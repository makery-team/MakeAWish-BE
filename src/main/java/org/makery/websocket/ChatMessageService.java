package org.makery.websocket;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public List<ChatMessageResponseDto> findMessages(Long roomNumber) {
        return chatMessageRepository.findByRoomNumber(roomNumber)
                .stream()
                .map(ChatMessageResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void save(final ChatMessageRequestDto requestDto) {
        ChatMessage chatMessage = requestDto.toEntity();
        chatMessageRepository.save(chatMessage);
    }

    @Transactional
    public void deleteByRoomNumber(Long roomNumber) {
        chatMessageRepository.deleteByRoomNumber(roomNumber);
    }
}