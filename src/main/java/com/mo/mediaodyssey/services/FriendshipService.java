package com.mo.mediaodyssey.services;

import com.mo.mediaodyssey.models.Community;
import com.mo.mediaodyssey.models.DTO.FriendRequestDTO;
import com.mo.mediaodyssey.models.Friendship;
import com.mo.mediaodyssey.models.User;
import com.mo.mediaodyssey.repositories.FriendshipRepository;
import com.mo.mediaodyssey.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class FriendshipService {

    private final FriendshipRepository friendshipRepo;
    private final CommuService commuService;
    private final UserRepository userRepo;

    public FriendshipService(FriendshipRepository friendshipRepo,
                             CommuService commuService,
                             UserRepository userRepo) {
        this.friendshipRepo = friendshipRepo;
        this.commuService = commuService;
        this.userRepo = userRepo;
    }

    /** Send a friend request */
    public void sendFriendRequest(Integer fromUserId, Integer toUserId) {
        if(friendshipRepo.existsByUserIdAndFriendId(fromUserId, toUserId) ||
                friendshipRepo.existsByFriendIdAndUserId(fromUserId, toUserId)) {
            throw new IllegalStateException("Friend request already exists");
        }
        Friendship request = new Friendship(fromUserId, toUserId);
        friendshipRepo.save(request);
    }

    /** Accept a friend request */
    public void acceptFriendRequest(Integer requestId) {
        Friendship request = friendshipRepo.findById(requestId)
                .orElseThrow(() -> new IllegalStateException("Friend request not found"));
        request.setAccepted(true);
        friendshipRepo.save(request);
    }

    /** Get friends as User objects */
    public List<User> getFriends(Integer userId) {
        List<Friendship> friendships = friendshipRepo.findByUserIdOrFriendIdAndAcceptedTrue(userId, userId);

        return friendships.stream()
                .map(f -> {
                    Integer otherId = f.getUserId().equals(userId) ? f.getFriendId() : f.getUserId();
                    return userRepo.findById(otherId).orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    public List<FriendRequestDTO> getIncomingRequests(Integer userId) {
        return friendshipRepo.findIncomingRequests(userId);
    }


    public List<User> getSuggestedFriends(Integer userId) {
        List<Integer> communityIds = commuService.getUserCommunities(userId).stream()
                .map(Community::getId)
                .collect(Collectors.toList());

        List<User> candidates =
                userRepo.findUsersInCommunitiesExcludingFriends(userId, communityIds);

        return candidates.stream()
                .filter(u ->
                        !friendshipRepo.existsByUserIdAndFriendId(userId, u.getId()) &&
                                !friendshipRepo.existsByFriendIdAndUserId(userId, u.getId())
                )
                .toList();
    }
}