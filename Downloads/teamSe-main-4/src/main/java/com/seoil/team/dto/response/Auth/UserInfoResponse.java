package com.seoil.team.dto.response.Auth;

import com.seoil.team.domain.member.RoleType;

public record UserInfoResponse(String name, String email, RoleType role) {
}