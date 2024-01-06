package com.msmsadegh.user.service;

import org.mop.account.ResponseTemplateDto;
import org.mop.account.exception.NotFoundException;
import org.mop.account.user.controller.requestDto.PositionPostRequest;
import org.mop.account.user.controller.requestDto.PositionPutRequest;
import org.mop.account.user.controller.responseDto.PositionResponse;
import org.mop.account.user.mapper.PositionMapper;
import org.mop.account.user.model.Position;
import org.mop.account.user.model.Role;
import org.mop.account.user.model.User;
import org.mop.account.user.model.repository.PositionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.mop.account.messages.Error.POSITION_ID_NOT_FOUND_ERROR;
import static org.mop.account.messages.Info.*;

@Service
public class PositionService {

    private final PositionRepository positionRepository;
    private final PositionMapper positionMapper;

    public PositionService(PositionRepository positionRepository, PositionMapper positionMapper) {
        this.positionRepository = positionRepository;
        this.positionMapper = positionMapper;
    }


    @Transactional
    public ResponseTemplateDto<PositionResponse> create(PositionPostRequest positionPostRequest) {
        var position = positionMapper.toPosition(positionPostRequest);
        positionRepository.save(position);
         return ResponseTemplateDto.<PositionResponse>builder()
                 .message(CREATED_POSITION_SUCCESSFULLY)
                 .build();
    }

    @Transactional
    public ResponseTemplateDto<PositionResponse> update(Long id, PositionPutRequest positionPutRequest) {
        var position = positionRepository.findById(id).orElseThrow(
                () -> new NotFoundException(POSITION_ID_NOT_FOUND_ERROR));

        positionMapper.updateByPositionPutRequest(positionPutRequest, position);
        positionRepository.save(position);

        return ResponseTemplateDto.<PositionResponse>builder()
                .message(UPDATED_POSITION_SUCCESSFULLY)
                .build();
    }

    @Transactional
    public ResponseTemplateDto<PositionResponse> remove(Long id) {
        var position = positionRepository.findById(id).orElseThrow(
                () -> new NotFoundException(POSITION_ID_NOT_FOUND_ERROR));

        positionRepository.delete(position);

        return ResponseTemplateDto.<PositionResponse>builder()
                .message(DELETED_POSITION_SUCCESSFULLY)
                .build();
    }

    @Transactional
    public Position create(Instant startDate, Instant endDate, Role role, User user) {
        var position = Position.builder()
                .startDate(startDate)
                .endDate(endDate)
                .role(role)
                .user(user)
                .build();
        positionRepository.save(position);
        return position;
    }
}