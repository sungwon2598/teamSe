package com.seoil.team.controller;

import com.seoil.team.dto.chatroom.ChatRoom;
import com.seoil.team.service.ChatService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/ws/chat")
public class TextChatRoomController {

    private final ChatService chatService;

    @GetMapping("/")
    public ResponseEntity<List<ChatRoom>> getAllRooms() {
        List<ChatRoom> rooms = chatService.findAllRoom();
        return ResponseEntity.ok(rooms);
    }

    @PostMapping("/create")
    public ResponseEntity<ChatRoom> createRoom(@RequestBody String roomName) {
        ChatRoom chatRoom = chatService.createTextRoom(roomName);

        return ResponseEntity.status(HttpStatus.CREATED).body(chatRoom);
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<ChatRoom> getRoom(@PathVariable String roomId) {
        try {
            ChatRoom chatRoom = chatService.findRoomById(roomId);
            return ResponseEntity.ok(chatRoom);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * 채팅방에 사용자 추가
     * @param roomId 방 ID
     * @param userName 추가할 사용자 이름
     * @return 성공 메시지
     */
    @PostMapping("/room/{roomId}/participants")
    public ResponseEntity<String> addUserToRoom(@PathVariable String roomId, @RequestParam String userName) {
        String uniqueUserName = chatService.isDuplicateName(roomId, userName);
        chatService.addUser(roomId, uniqueUserName);
        chatService.plusUserCnt(roomId);
        return ResponseEntity.ok(uniqueUserName + "님이 방에 참여하셨습니다.");
    }

    /**
     * 채팅방에서 사용자 제거
     * @param roomId 방 ID
     * @param userUUID 제거할 사용자의 UUID
     * @return 성공 메시지
     */
    @DeleteMapping("/room/{roomId}/participants")
    public ResponseEntity<String> removeUserFromRoom(@PathVariable String roomId, @RequestParam String userUUID) {
        chatService.delUser(roomId, userUUID);
        chatService.minusUserCnt(roomId);
        return ResponseEntity.ok("사용자가 방에서 제거되었습니다.");
    }

}
