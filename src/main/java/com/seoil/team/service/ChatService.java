package com.seoil.team.service;

import com.seoil.team.domain.member.Member;
import com.seoil.team.dto.chatroom.ChatRoom;
import com.seoil.team.dto.chatroom.ChatRoomType;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class ChatService {

    private Map<String, ChatRoom> chatRoomMap;

    @PostConstruct
    private void init() {
        chatRoomMap = new LinkedHashMap<>();
    }
    //전체 채팅 방 조회
    public List<ChatRoom> findAllRoom() {

        List<ChatRoom> chatRooms = new ArrayList<>(chatRoomMap.values());
        Collections.reverse(chatRooms);

        return chatRooms;
    }

    public ChatRoom findRoomById(String id) {
        return chatRoomMap.get(id);
    }

    public ChatRoom createTextRoom(String roomName){
        ChatRoom chatRoom = new ChatRoom().create(roomName, ChatRoomType.TEXT);

        chatRoomMap.put(roomName, chatRoom);
        return chatRoom;
    }

    //채팅방 인원 +1
    public void plusUserCnt(String roomId) {
        ChatRoom chatRoom = chatRoomMap.get(roomId);
        chatRoom.setUserCount(chatRoom.getUserCount() + 1);
    }

    public void minusUserCnt(String roomId) {
        ChatRoom chatRoom = chatRoomMap.get(roomId);
        chatRoom.setUserCount(chatRoom.getUserCount() - 1);
    }

    public String addUser(String roomId, String userName) {
        ChatRoom chatRoom = chatRoomMap.get(roomId);
        String userId = UUID.randomUUID().toString();

        chatRoom.getUserList().put(userId, userName);

        return userName;
    }

    public String isDuplicateName(String roomId, String username){
        ChatRoom room = chatRoomMap.get(roomId);
        String tmp = username;

        while(room.getUserList().containsValue(tmp)){
            int ranNum = (int) (Math.random()*100)+1;

            tmp = username+ranNum;
        }

        return tmp;
    }

    // 채팅방 유저 리스트 삭제
    public void delUser(String roomId, String userUUID){
        ChatRoom room = chatRoomMap.get(roomId);
        room.getUserList().remove(userUUID);
    }

    // 채팅방 userName 조회
    public String getUserName(String roomId, String userUUID){
        ChatRoom room = chatRoomMap.get(roomId);
        return room.getUserList().get(userUUID);
    }

    // 채팅방 전체 userlist 조회
    public ArrayList<String> getUserList(String roomId){
        ArrayList<String> list = new ArrayList<>();

        ChatRoom room = chatRoomMap.get(roomId);

        room.getUserList().forEach((key, value) -> list.add(value));
        return list;
    }
}
