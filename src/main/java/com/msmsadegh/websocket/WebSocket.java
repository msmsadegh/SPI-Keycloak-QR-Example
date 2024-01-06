package com.msmsadegh.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.msmsadegh.exception.ExpiredException;
import com.msmsadegh.exception.NotFoundException;
import lombok.*;
import org.springframework.stereotype.Component;
import org.springframework.util.PropertyPlaceholderHelper;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import javax.ws.rs.BadRequestException;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.msmsadegh.messages.Error.*;


//todo later, session expiration with scheduler or on some actions of others!


@ServerEndpoint(value = "/websocket/connect/{sid}", encoders = {DataEncoder.class}, decoders = {DataDecoder.class})
@Component
public class WebSocket {

    private static final String serverKey;
    private static final String rawSidFormat;
    private static final Integer TIMEOUT;

    /* after authorization a new sessionId for the user would be added */
    private static final Map<String, SessionDetail> authorizedSessionIds;
    /* concurrent Thread safety of each client LoginWebSocket Object. */
    private static final Map<String, WebSocket> openSessions;

    protected static final ObjectMapper mapper;

    private static final PropertyPlaceholderHelper helper;

    static {
        serverKey = "fd8fd473-a5ce-4687-8574-f051d7734fbe";
        rawSidFormat = "${email}-${actionType}-${businessKey}-${serverKey}-${timeout}";
        TIMEOUT = 100;
        authorizedSessionIds = Collections.synchronizedMap(new HashMap<String, SessionDetail>());
        openSessions = Collections.synchronizedMap(new HashMap<String, WebSocket>());
        helper = new PropertyPlaceholderHelper("${", "}", null, false);
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    private Session session;
    private String sid = "";
    private SessionDetail sessionDetail;

    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        if(!authorizedSessionIds.containsKey(sid)) {
            //todo, add log
            dropCurrentSession(session);
            throw new BadRequestException(WEBSOCKET_SID_NOT_INITIALISED_ERROR);
        }

        sessionDetail = authorizedSessionIds.get(sid);

        /* remove the sid from authorization sids not to be reused */
        authorizedSessionIds.remove(sid);

        this.session = session;
        this.sid = sid;

        openSessions.put(sid, this);

        //todo, add log
    }

    private static void closeSession(String sid) {
        try {
            openSessions.get(sid).session.close();
            openSessions.remove(sid);
            //todo, add log
        } catch (Exception ignored) {
            throw new NotFoundException(WEBSOCKET_SID_NOT_FOUND_ERROR);
        }
    }

    public void closeSession(String sid, String callerUserEmail, WebSocketActionType actionType,
                             String businessKey, Instant timeout) {
        isValidSessionOrThrow(sid, callerUserEmail, actionType, businessKey, timeout);
        closeSession(sid);
    }

    private void dropCurrentSession(Session session) {
        try {
            session.close();
        } catch (IOException ignored) {

        }
    }

    @OnClose
    public void onClose() {
        closeSession(sid);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        ;
        /* we don't receive message from clients in this, if receive we uncomment this part to handle it
            for (QrLoginWebSocket item : webSocketSet) {
                try {
                    item.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        */
    }

    @OnError
    public void onError(Session session, Throwable error) {
        //todo, add log and exception log
    }


    public static void sendBroadcastMessage(String message) {
        openSessions.keySet().forEach(sid -> {
            try {
                openSessions.get(sid).sendMessage(message);
                //todo, add log (sender is server)
            } catch (IOException e) {
                //todo, add log and exception log
            }
        });
    }

    public WebSocketDto getAuthorizedSessionId(String callerUserEmail, WebSocketActionType actionType,
                                               String businessKey, String comment) {
        SessionDetail sessionDetail = SessionDetail.builder()
                .actionType(actionType)
                .businessKey(businessKey)
                .timeout(Instant.now().plusSeconds(TIMEOUT))
                .email(callerUserEmail)
                .comment(comment)
                .build();
        Map<String, String> placeHolders = mapper.convertValue(sessionDetail, Map.class);
        placeHolders.put("serverKey", serverKey);

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            String sessionId = DatatypeConverter
                    .printHexBinary(md.digest(helper.replacePlaceholders(rawSidFormat, placeHolders::get).getBytes()));

            authorizedSessionIds.put(sessionId, sessionDetail);
            return new WebSocketDto(sessionId, sessionDetail.getTimeout());
        } catch (NoSuchAlgorithmException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    public void isValidSessionOrThrow(String sid, String callerUserEmail, WebSocketActionType actionType,
                                      String businessKey, Instant timeout) {

        if(timeout.isBefore(Instant.now()))
        {
            closeSession(sid);
            throw new ExpiredException(WEBSOCKET_EXPIRATION_ERROR);
        }
        SessionDetail givenSessionDetail = SessionDetail.builder()
                .actionType(actionType)
                .businessKey(businessKey)
                .timeout(openSessions.get(sid).sessionDetail.timeout)
                .email(callerUserEmail)
                .build();
        Map<String, String> placeHolders = mapper.convertValue(givenSessionDetail, Map.class);
        placeHolders.put("serverKey", serverKey);

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            String computedSid = DatatypeConverter
                    .printHexBinary(md.digest(helper.replacePlaceholders(rawSidFormat, placeHolders::get).getBytes()));
            if(!sid.equals(computedSid))
                throw new BadRequestException(WEBSOCKET_SID_INVALID_ERROR);
        } catch (NoSuchAlgorithmException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    public void sendMessage(String message) throws IOException {
        try {
            this.session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            //todo, add log and exception log
            throw new ExpiredException(WEBSOCKET_EXPIRATION_ERROR);
        }
        //todo, add success log

    }

    private void sendMessage(WebSocketResponseMessage webSocketResponseMessage) {
        try {
            this.session.getBasicRemote().sendObject(webSocketResponseMessage);
            //todo, add log
        } catch (IOException e) {
            //todo, add log and exception log
            throw new ExpiredException(WEBSOCKET_EXPIRATION_ERROR);
        } catch (EncodeException e) {
            //todo, add log and exception log
            throw new ExpiredException(WEBSOCKET_EXPIRATION_ERROR);
//            e.printStackTrace();
        }
        //todo, add success log
    }

    /* HINT: for null sid we can push to all, not in this case */
    public void sendMessage(@PathParam("sid") String sid,
                            WebSocketResponseMessage webSocketResponseMessage) {
        if(!openSessions.containsKey(sid))
            throw new NotFoundException(WEBSOCKET_SID_NOT_FOUND_ERROR);

        openSessions.get(sid).sendMessage(webSocketResponseMessage);
    }

    public void sendMessageAndClose(@PathParam("sid") String sid,
                                    WebSocketResponseMessage webSocketResponseMessage) {
        sendMessage(sid, webSocketResponseMessage);

        if(!openSessions.containsKey(sid))
            throw new NotFoundException(WEBSOCKET_SID_NOT_FOUND_ERROR);

        openSessions.get(sid).sendMessage(webSocketResponseMessage);

        closeSession(sid);
    }

    public SessionDetail getSessionDetail(String sid) {
        if(!openSessions.containsKey(sid))
            throw new NotFoundException(WEBSOCKET_SID_NOT_FOUND_ERROR);

        return openSessions.get(sid).sessionDetail;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessionDetail {
        WebSocketActionType actionType;
        String businessKey;
        Instant timeout;
        String email;
        String comment;
    }
}
