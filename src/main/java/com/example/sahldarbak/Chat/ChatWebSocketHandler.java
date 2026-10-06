package com.example.sahldarbak.Chat;

import com.example.sahldarbak.Repository.TravelMatchRepository;
import com.example.sahldarbak.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final TravelMatchRepository travelMatchRepository;
    private final UserRepository userRepository;

    // connected users: userId -> their open connection (memory only, nothing saved)
    private final Map<Integer, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // a user connects: ws://localhost:8080/chat?userId=1
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Integer userId = getUserId(session);
        if (userId == null || !userRepository.existsById(userId)) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        session.getAttributes().put("userId", userId);
        sessions.put(userId, session);
    }

    // a message arrives, format: toUserId:text   (example  2:hi Mohammed)
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        Integer fromId = (Integer) session.getAttributes().get("userId");
        String payload = message.getPayload();

        int separator = payload.indexOf(":");
        if (separator < 1) {
            send(session, "error: format must be toUserId:text");
            return;
        }

        Integer toId;
        try {
            toId = Integer.parseInt(payload.substring(0, separator).trim());
        } catch (NumberFormatException e) {
            send(session, "error: toUserId must be a number");
            return;
        }

        String text = payload.substring(separator + 1).trim();
        if (text.isEmpty()) {
            send(session, "error: message is empty");
            return;
        }

        // chat is allowed only between users with an accepted invite, any direction
        boolean accepted = travelMatchRepository.existsBySenderIdAndReceiverIdAndStatus(fromId, toId, "accepted")
                || travelMatchRepository.existsBySenderIdAndReceiverIdAndStatus(toId, fromId, "accepted");
        if (!accepted) {
            send(session, "error: no accepted invite with this user");
            return;
        }

        WebSocketSession target = sessions.get(toId);
        if (target == null || !target.isOpen()) {
            send(session, "error: user is offline");
            return;
        }

        send(target, fromId + ": " + text);
    }

    // a user disconnects
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Integer userId = (Integer) session.getAttributes().get("userId");
        if (userId != null)
            sessions.remove(userId);
    }

    private Integer getUserId(WebSocketSession session) {
        try {
            String query = session.getUri().getQuery();   // userId=1
            if (query == null || !query.startsWith("userId="))
                return null;
            return Integer.parseInt(query.substring(7));
        } catch (Exception e) {
            return null;
        }
    }

    private void send(WebSocketSession session, String text) {
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(text));
            }
        } catch (Exception e) {
            System.out.println("chat send failed: " + e.getMessage());
        }
    }
}