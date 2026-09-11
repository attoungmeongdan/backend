package com.atmd.backend.domain.group.service;

import com.atmd.backend.domain.group.dto.request.GroupCreateRequestDTO;
import com.atmd.backend.domain.group.dto.response.GroupInviteLinkResponseDTO;
import com.atmd.backend.domain.group.dto.response.GroupJoinResponseDTO;
import com.atmd.backend.domain.group.dto.response.GroupMemberResponseDTO;
import com.atmd.backend.domain.group.dto.response.GroupResponseDTO;
import com.atmd.backend.domain.group.entity.Group;
import com.atmd.backend.domain.group.entity.GroupUser;
import com.atmd.backend.domain.group.exception.GroupErrorCode;
import com.atmd.backend.domain.group.repository.GroupRepository;
import com.atmd.backend.domain.group.repository.GroupUserRepository;
import com.atmd.backend.domain.user.entity.User;
import com.atmd.backend.domain.user.exception.UserErrorCode;
import com.atmd.backend.domain.user.repository.UserRepository;
import com.atmd.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final int CODE_LENGTH = 12;
    private static final int MAX_GENERATION_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final GroupRepository groupRepository;
    private final GroupUserRepository groupUserRepository;
    private final UserRepository userRepository;

    @Value("${app.frontend-base-url:https://www.atmd.cloud}")
    private String frontendBaseUrl;

    @Transactional
    public GroupResponseDTO create(Long userId, GroupCreateRequestDTO request) {
        User owner = findUser(userId);

        if (groupUserRepository.countByUserId(userId) >= Group.MAX_GROUPS_PER_USER) {
            throw new GeneralException(GroupErrorCode.MAX_GROUPS_EXCEEDED);
        }

        Integer maxMemberCount = request.getMaxMemberCount();
        if (maxMemberCount == null || !Group.isValidMemberCount(maxMemberCount)) {
            throw new GeneralException(GroupErrorCode.INVALID_MEMBER_COUNT);
        }

        String inviteCode = generateUniqueInviteCode();
        Group group = Group.create(owner, request.getName(), inviteCode, maxMemberCount);

        groupRepository.save(group);
        groupUserRepository.save(GroupUser.of(group, owner));

        return GroupResponseDTO.of(group, 1L, userId);
    }

    @Transactional(readOnly = true)
    public List<GroupResponseDTO> getMyGroups(Long userId) {
        return groupUserRepository.findAllByUserIdWithGroup(userId).stream()
                .map(gu -> {
                    Group group = gu.getGroup();
                    long count = groupUserRepository.countByGroupId(group.getId());
                    return GroupResponseDTO.of(group, count, userId);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public GroupResponseDTO getGroupDetail(Long groupId, Long userId) {
        Group group = findGroup(groupId);
        checkMembership(groupId, userId);
        long count = groupUserRepository.countByGroupId(groupId);
        return GroupResponseDTO.of(group, count, userId);
    }

    @Transactional(readOnly = true)
    public List<GroupMemberResponseDTO> getGroupMembers(Long groupId, Long userId) {
        Group group = findGroup(groupId);
        checkMembership(groupId, userId);
        Long ownerId = group.getOwner().getId();
        return groupUserRepository.findAllByGroupIdWithUser(groupId).stream()
                .map(gu -> GroupMemberResponseDTO.of(gu, ownerId))
                .toList();
    }

    @Transactional(readOnly = true)
    public GroupInviteLinkResponseDTO getInviteLink(Long groupId, Long userId) {
        Group group = findGroup(groupId);
        if (!group.isOwner(userId)) {
            throw new GeneralException(GroupErrorCode.ACCESS_DENIED);
        }
        String link = UriComponentsBuilder.fromUriString(frontendBaseUrl)
                .pathSegment("groups", "join")
                .queryParam("inviteCode", group.getInviteCode())
                .build().toUriString();
        return GroupInviteLinkResponseDTO.of(group.getInviteCode(), link);
    }

    @Transactional
    public GroupJoinResponseDTO joinByInviteCode(String inviteCode, Long userId) {
        User user = findUser(userId);
        return joinInternal(inviteCode, user);
    }

    @Transactional
    public void joinAfterSignup(String inviteCode, User user) {
        if (inviteCode == null || inviteCode.isBlank()) return;
        try {
            joinInternal(inviteCode, user);
        } catch (GeneralException ignored) {
            // 초대 실패는 회원가입 자체를 막지 않음 (정원 초과, 이미 멤버 등)
        }
    }

    @Transactional
    public void deleteGroup(Long groupId, Long userId) {
        Group group = findGroup(groupId);
        if (!group.isOwner(userId)) {
            throw new GeneralException(GroupErrorCode.ACCESS_DENIED);
        }
        groupUserRepository.deleteAllByGroupId(groupId);
        groupRepository.delete(group);
    }

    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        Group group = findGroup(groupId);
        if (group.isOwner(userId)) {
            throw new GeneralException(GroupErrorCode.OWNER_MUST_DELETE_GROUP);
        }
        GroupUser groupUser = groupUserRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new GeneralException(GroupErrorCode.NOT_MEMBER));
        groupUserRepository.delete(groupUser);
    }

    private GroupJoinResponseDTO joinInternal(String inviteCode, User user) {
        if (inviteCode == null || inviteCode.isBlank()) {
            throw new GeneralException(GroupErrorCode.INVALID_INVITE_CODE);
        }
        Group group = groupRepository.findByInviteCodeAndIsDeletedFalse(inviteCode)
                .orElseThrow(() -> new GeneralException(GroupErrorCode.INVALID_INVITE_CODE));

        if (groupUserRepository.existsByGroupIdAndUserId(group.getId(), user.getId())) {
            throw new GeneralException(GroupErrorCode.ALREADY_MEMBER);
        }
        if (groupUserRepository.countByUserId(user.getId()) >= Group.MAX_GROUPS_PER_USER) {
            throw new GeneralException(GroupErrorCode.MAX_GROUPS_EXCEEDED);
        }
        long currentCount = groupUserRepository.countByGroupId(group.getId());
        if (!group.canAddMember(currentCount)) {
            throw new GeneralException(GroupErrorCode.GROUP_FULL);
        }

        groupUserRepository.save(GroupUser.of(group, user));
        return GroupJoinResponseDTO.of(group.getId(), group.getName(), currentCount + 1);
    }

    private String generateUniqueInviteCode() {
        for (int i = 0; i < MAX_GENERATION_ATTEMPTS; i++) {
            String code = randomCode();
            if (!groupRepository.existsByInviteCode(code)) {
                try {
                    return code;
                } catch (DataIntegrityViolationException ignored) {
                    // race condition -> 재시도
                }
            }
        }
        throw new GeneralException(GroupErrorCode.INVITE_CODE_GENERATION_FAILED);
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    private Group findGroup(Long groupId) {
        return groupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new GeneralException(GroupErrorCode.GROUP_NOT_FOUND));
    }

    private User findUser(Long userId) {
        return userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new GeneralException(UserErrorCode.USER_NOT_FOUND));
    }

    private void checkMembership(Long groupId, Long userId) {
        if (!groupUserRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new GeneralException(GroupErrorCode.ACCESS_DENIED);
        }
    }
}
