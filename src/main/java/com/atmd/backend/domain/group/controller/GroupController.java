package com.atmd.backend.domain.group.controller;

import com.atmd.backend.domain.group.dto.request.GroupCreateRequestDTO;
import com.atmd.backend.domain.group.dto.response.GroupInviteLinkResponseDTO;
import com.atmd.backend.domain.group.dto.response.GroupJoinResponseDTO;
import com.atmd.backend.domain.group.dto.response.GroupMemberResponseDTO;
import com.atmd.backend.domain.group.dto.response.GroupResponseDTO;
import com.atmd.backend.domain.group.service.GroupService;
import com.atmd.backend.global.auth.util.SecurityUtil;
import com.atmd.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Group", description = "그룹 API")
@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @Operation(
            summary = "그룹 생성",
            description = """
                    첫 그룹은 GENERAL(2인 고정, 무료)로 자동 생성됩니다.
                    이후 그룹은 SUBSCRIBED로 maxMemberCount(2~5)를 지정해야 하며, 인원수 × 500원의 price가 그룹에 기록됩니다.
                    사용자당 최대 5개 그룹까지 가입 가능합니다.
                    """
    )
    @PostMapping
    public ResponseEntity<ApiResponse<GroupResponseDTO>> create(
            @Valid @RequestBody GroupCreateRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(
                "그룹이 생성되었습니다.",
                groupService.create(SecurityUtil.getCurrentUserId(), request)));
    }

    @Operation(summary = "내 그룹 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupResponseDTO>>> getMyGroups() {
        return ResponseEntity.ok(ApiResponse.success(
                groupService.getMyGroups(SecurityUtil.getCurrentUserId())));
    }

    @Operation(summary = "그룹 상세 조회")
    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupResponseDTO>> getGroupDetail(
            @PathVariable Long groupId) {
        return ResponseEntity.ok(ApiResponse.success(
                groupService.getGroupDetail(groupId, SecurityUtil.getCurrentUserId())));
    }

    @Operation(summary = "그룹 멤버 목록 조회")
    @GetMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse<List<GroupMemberResponseDTO>>> getGroupMembers(
            @PathVariable Long groupId) {
        return ResponseEntity.ok(ApiResponse.success(
                groupService.getGroupMembers(groupId, SecurityUtil.getCurrentUserId())));
    }

    @Operation(
            summary = "그룹 초대 링크 조회 (방장 전용)",
            description = "그룹 생성 시 발급된 초대 코드와 링크를 반환합니다. 방장만 조회 가능합니다."
    )
    @GetMapping("/{groupId}/invite-link")
    public ResponseEntity<ApiResponse<GroupInviteLinkResponseDTO>> getInviteLink(
            @PathVariable Long groupId) {
        return ResponseEntity.ok(ApiResponse.success(
                groupService.getInviteLink(groupId, SecurityUtil.getCurrentUserId())));
    }

    @Operation(
            summary = "초대 코드로 그룹 참가 (로그인 사용자 전용)",
            description = "이미 로그인된 사용자가 초대 코드로 그룹에 참가합니다. 미로그인 사용자는 소셜 로그인 플로우에서 자동 참가됩니다."
    )
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<GroupJoinResponseDTO>> join(
            @Parameter(description = "그룹 초대 코드", required = true)
            @RequestParam String inviteCode) {
        return ResponseEntity.ok(ApiResponse.success(
                "그룹에 참가했습니다.",
                groupService.joinByInviteCode(inviteCode, SecurityUtil.getCurrentUserId())));
    }

    @Operation(
            summary = "그룹 삭제 (방장 전용)",
            description = "방장이 그룹을 삭제합니다. 그룹의 모든 멤버가 함께 그룹에서 제거됩니다."
    )
    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(@PathVariable Long groupId) {
        groupService.deleteGroup(groupId, SecurityUtil.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(
            summary = "그룹 탈퇴",
            description = "방장은 탈퇴할 수 없습니다. 그룹을 삭제해 주세요."
    )
    @DeleteMapping("/{groupId}/members/me")
    public ResponseEntity<ApiResponse<Void>> leaveGroup(@PathVariable Long groupId) {
        groupService.leaveGroup(groupId, SecurityUtil.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
