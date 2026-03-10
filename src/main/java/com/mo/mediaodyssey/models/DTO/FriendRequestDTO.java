package com.mo.mediaodyssey.models.DTO;

public class FriendRequestDTO {

    private Integer requestId;
    private Integer senderId;
    private String senderUsername;

    public FriendRequestDTO(Integer requestId,
                            Integer senderId,
                            String senderUsername) {
        this.requestId = requestId;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }
}