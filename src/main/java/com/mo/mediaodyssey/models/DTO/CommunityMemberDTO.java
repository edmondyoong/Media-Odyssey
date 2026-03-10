package com.mo.mediaodyssey.models.DTO;

import com.mo.mediaodyssey.enums.RoleType;

public class CommunityMemberDTO {

    private final Integer userId;
    private final String username;
    private final RoleType roleType;
    private final String communityName;

    public CommunityMemberDTO(Integer userId, String username, RoleType roleType, String communityName) {
        this.userId = userId;
        this.username = username;
        this.roleType = roleType;
        this.communityName = communityName;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public String getCommunityName() {
        return communityName;
    }
}