package com.msmsadegh.user.service;

import org.mop.account.ResponseTemplateDto;
import org.mop.account.apihandler.KeycloakApiHandler;
import org.mop.account.apihandler.RayganSmsPanelApiHandler;
import org.mop.account.apihandler.ShahkarApiHandler;
import org.mop.account.config.KeycloakUser;
import org.mop.account.exception.*;
import org.mop.account.keycloak.service.KeycloakService;
import org.mop.account.user.RoleType;
import org.mop.account.user.controller.requestDto.*;
import org.mop.account.user.model.Position;
import org.mop.account.user.model.Role;
import org.mop.account.user.model.User;
import org.mop.account.user.model.repository.PositionRepository;
import org.mop.account.user.model.repository.RoleRepository;
import org.mop.account.user.model.repository.UserRepository;
import org.mop.account.websocket.WebSocket;
import org.mop.account.websocket.WebSocketResponseMessage;
import org.mop.account.websocket.WebSocketResponseMessageType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Random;
import java.util.stream.IntStream;

import static org.mop.account.messages.Error.*;
import static org.mop.account.messages.Info.*;

@Service
public class UserService {
    private final KeycloakService keycloakService;
    private final PositionService positionService;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PositionRepository positionRepository;
    private ShahkarApiHandler shahkarApiHandler;
    private final KeycloakApiHandler keycloakApiHandler;
    private final RayganSmsPanelApiHandler rayganSmsPanelApiHandler;
    WebSocket webSocket;

    public UserService(KeycloakService keycloakService, PositionService positionService, RoleRepository roleRepository,
                       UserRepository userRepository, PositionRepository positionRepository,
                       ShahkarApiHandler shahkarApiHandler, RayganSmsPanelApiHandler rayganSmsPanelApiHandler,
                       KeycloakApiHandler keycloakApiHandler, WebSocket webSocket) {
        this.keycloakService = keycloakService;
        this.positionService = positionService;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.positionRepository = positionRepository;
        this.shahkarApiHandler = shahkarApiHandler;
        this.keycloakApiHandler = keycloakApiHandler;
        this.rayganSmsPanelApiHandler = rayganSmsPanelApiHandler;
        this.webSocket = webSocket;
    }

    @Transactional
    public User create(String nationalCode, String phoneNumber) {
        var user = User.builder()
                .nationalCode(nationalCode)
                .phoneNumber(phoneNumber)
                .isConfirm(false)
                .build();
        userRepository.save(user);
        return user;
    }
    @Transactional
    public Position createPosition(Instant startDate, Instant endDate, Role role, User user) {
        var position = Position.builder()
                .startDate(startDate)
                .endDate(endDate)
                .role(role)
                .user(user)
                .build();
        positionRepository.save(position);
        return position;
    }

    public ResponseTemplateDto<Void> login(LoginPostRequest loginPostRequest) {
        //todo: fixed the fake number in the response!
        //todo: check confirm user in token
        var user = userRepository.findByNationalCode(loginPostRequest.getNationalCode())
                .orElseThrow(() -> new NotFoundException(USER_NATIONAL_CODE_NOT_FOUND_ERROR));

        var password = getSixDigitRandomCode();

        rayganSmsPanelApiHandler.sendCustomSms(user.getPhoneNumber(), password);
        keycloakService.setPassword(loginPostRequest.getNationalCode(), password);

        return ResponseTemplateDto.<Void>builder()
                .message(String.format(SEND_SMS_SUCCESSFULLY, user.getPhoneNumber().substring(8,11)))
                .build();
    }

    @Transactional
    public ResponseTemplateDto<Void> signUp(SignUpPostRequest signUpPostRequest) {
        isValidNationalCodeOrThrow(signUpPostRequest.getNationalCode());

        //todo: continue after get access code
//        isValidPhoneNumberThrow(signUpPostRequest.getPhoneNumber(), signUpPostRequest.getNationalCode());

        var user = create(signUpPostRequest.getNationalCode(), signUpPostRequest.getPhoneNumber());
        var role = roleRepository.findByEnumName(RoleType.ORGANIZATION_MANAGER)
                .orElseThrow(() -> new NotFoundException(ROLE_ENUM_NOT_FOUND_ERROR));
        positionService.create(Instant.now(), null, role, user);
        //todo alireza: call common user register
//        commonApiHandler.register();
        var password = getSixDigitRandomCode();

        //todo: might get first name and last name from gsb
        var keycloakUser = KeycloakUser.builder()
                .role(role.getEnName())
                .username(signUpPostRequest.getNationalCode())
                .phoneNumber(signUpPostRequest.getPhoneNumber())
                .firstName("first name")
                .lastName("last name")
                .password(password)
                .build();
        keycloakService.createUser(keycloakUser);
        rayganSmsPanelApiHandler.sendCustomSms(user.getPhoneNumber(), password);
        user.setVerificationCode(password);
        user.setMessageSentAt(Instant.now());
        userRepository.save(user);

        return ResponseTemplateDto.<Void>builder()
                .message(String.format(SEND_SMS_SUCCESSFULLY, user.getPhoneNumber().substring(8,11)))
                .build();
    }
    @Transactional
    public ResponseTemplateDto<Void> confirmVerificationCode(VerificationPutRequest verificationPutRequest) {
        var user = userRepository.findByNationalCodeAndRemovedAt(verificationPutRequest.getNationalCode(), null)
                .orElseThrow(() -> new NotFoundException(USER_NATIONAL_CODE_NOT_FOUND_ERROR));

        if(!user.getVerificationCode().equals(verificationPutRequest.getVerificationCode()))
            throw new VerificationCodeException();

        if(Instant.now().getEpochSecond() - user.getMessageSentAt().getEpochSecond() > 120)
            throw new ValidationCodeExpiredException();

        confirmUser(user);

        return ResponseTemplateDto.<Void>builder()
                .message(SIGN_UP_SUCCESSFULLY)
                .build();
    }

    @Transactional
    public ResponseTemplateDto<Void> resendVerificationCode(ResendVerificationPutRequest resendVerificationPutRequest) {
        var user = userRepository.findByNationalCodeAndRemovedAt(resendVerificationPutRequest.getNationalCode(), null)
                .orElseThrow(() -> new NotFoundException(USER_NATIONAL_CODE_NOT_FOUND_ERROR));

        if(user.getIsConfirm())
            throw new UserConfirmationException();

        if(Instant.now().getEpochSecond() - user.getMessageSentAt().getEpochSecond() < 120)
            return ResponseTemplateDto.<Void>builder()
                    .message(SEND_SMS_SUCCESSFULLY)
                    .build();

        var password = getSixDigitRandomCode();
        keycloakService.setPassword(user.getNationalCode(), password);
        rayganSmsPanelApiHandler.sendCustomSms(user.getPhoneNumber(), password);

        user.setVerificationCode(password);
        user.setMessageSentAt(Instant.now());
        userRepository.save(user);

        return ResponseTemplateDto.<Void>builder()
                .message(SEND_SMS_SUCCESSFULLY)
                .build();
    }

    @Transactional
    public void isValidNationalCodeOrThrow(String nationalCode) {
        if (!nationalCode.matches("^\\d{10}$"))
            throw new ValidationNationalCodeException();

        var check = Integer.parseInt(nationalCode.substring(9, 10));

        var sum = IntStream.range(0, 9)
                .map(x -> Integer.parseInt(nationalCode.substring(x, x + 1)) * (10 - x))
                .sum() % 11;

        if (sum < 2) {
            if (check != sum)
                throw new ValidationNationalCodeException();
        } else if (check + sum != 11)
            throw new ValidationNationalCodeException();
    }

    @Transactional
    public void isValidPhoneNumberThrow(String phoneNumber, String nationalCode) {
        var result = shahkarApiHandler.phoneNumberIsValid(phoneNumber, nationalCode);

        if(!result.getStatus().equals("success"))
            throw new ValidationPhoneNumberException();
    }

    @Transactional
    public void confirmUser(User user) {
        user.setIsConfirm(true);
        userRepository.save(user);
        keycloakService.confirmUser(user.getNationalCode());
    }

    private String getSixDigitRandomCode() {
        Random random = new Random();
        // this will convert any number sequence into 6 character.
        return String.format("%06d", random.nextInt(999999));
    }


    public void qrCodeLogin(InitializeUserGetRequest initializeUserGetRequest, String username) {
        webSocket.sendMessage(initializeUserGetRequest.getSid(),
                new WebSocketResponseMessage(WebSocketResponseMessageType.PROCESSING, WEB_SOCKET_PROCESSING));

        var user = userRepository.findByNationalCode(username)
                .orElseThrow(() -> new NotFoundException(USER_NATIONAL_CODE_NOT_FOUND_ERROR));
        Random random = new Random();
        var password =  String.format("%06d", random.nextInt(999999));

        keycloakService.setPassword(username, password);
        keycloakApiHandler.getTokenOrThrow(username, password);
        webSocket.sendMessage(initializeUserGetRequest.getSid(),
                new WebSocketResponseMessage(WebSocketResponseMessageType.PROCESSING, WEB_SOCKET_PROCESSING));
        ResponseTemplateDto.<Void>builder()
                .message(String.format(SEND_SMS_SUCCESSFULLY, user.getPhoneNumber().substring(8, 11)))
                .build();
    }
}