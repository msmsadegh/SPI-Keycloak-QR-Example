package com.msmsadegh.user.service;

import org.mop.account.ResponseTemplateDto;
import org.mop.account.exception.NotFoundException;
import org.mop.account.user.controller.requestDto.RolePostRequest;
import org.mop.account.user.controller.requestDto.RolePutRequest;
import org.mop.account.user.controller.responseDto.RoleResponse;
import org.mop.account.user.mapper.RoleMapper;
import org.mop.account.user.model.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.mop.account.messages.Error.ROLE_ID_NOT_FOUND_ERROR;
import static org.mop.account.messages.Info.*;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    public RoleService(RoleRepository roleRepository, RoleMapper roleMapper) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
    }

    @Transactional
    public ResponseTemplateDto<RoleResponse> create(RolePostRequest rolePostRequest) {
        var role = roleMapper.toRole(rolePostRequest);
        roleRepository.save(role);
         return ResponseTemplateDto.<RoleResponse>builder()
                 .message(CREATED_ROLE_SUCCESSFULLY)
                 .build();
    }

    @Transactional
    public ResponseTemplateDto<RoleResponse> update(Long id, RolePutRequest rolePutRequest) {
        var role = roleRepository.findById(id).orElseThrow(
                () -> new NotFoundException(ROLE_ID_NOT_FOUND_ERROR));

        roleMapper.updateByRolePutRequest(rolePutRequest, role);
        roleRepository.save(role);

        return ResponseTemplateDto.<RoleResponse>builder()
                .message(UPDATED_ROLE_SUCCESSFULLY)
                .build();
    }

    @Transactional
    public ResponseTemplateDto<RoleResponse> remove(Long id) {
        var role = roleRepository.findById(id).orElseThrow(
                () -> new NotFoundException(ROLE_ID_NOT_FOUND_ERROR));

        roleRepository.delete(role);

        return ResponseTemplateDto.<RoleResponse>builder()
                .message(DELETED_ROLE_SUCCESSFULLY)
                .build();
    }
}