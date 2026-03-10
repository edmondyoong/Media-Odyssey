package com.mo.mediaodyssey.controllersExtra;

import com.mo.mediaodyssey.models.Community;
import com.mo.mediaodyssey.models.DTO.FriendRequestDTO;
import com.mo.mediaodyssey.models.User;
import com.mo.mediaodyssey.services.CommService;
import com.mo.mediaodyssey.services.FriendshipService;
import com.mo.mediaodyssey.services.PermissionService;
import com.mo.mediaodyssey.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final CommService commService;
    private final FriendshipService friendshipService;
    private final PermissionService permissionService;

    public UserController(UserService userService,
                          CommService commService,
                          FriendshipService friendshipService, PermissionService permissionService){
        this.userService = userService;
        this.commService = commService;
        this.friendshipService = friendshipService;
        this.permissionService = permissionService;
    }

    // Show login page
    @GetMapping("/login")
    public String showLoginPage() {
        return "users/login";
    }

    // Show register page
    @GetMapping("/register")
    public String showRegisterPage() {
        return "users/register";
    }

    // Register user
    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String password,
                               RedirectAttributes redirectAttributes) {
        try {
            userService.registerUser(username, email, password);
            redirectAttributes.addFlashAttribute("successMessage", "Account created successfully");
            return "redirect:/users/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/users/register";
        }
    }

    // Login
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        try {
            User user = userService.loginUser(username, password);
            session.setAttribute("userId", user.getId());
            return "redirect:/users/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/users/login";
        }
    }

    // Dashboard
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/users/login";
        }

        // Communities
        List<Community> communities = commService.getUserCommunities(userId);
        model.addAttribute("myCommunities", communities);


        Map<Integer, Boolean> moderationMap = new HashMap<>();

        for (Community community : communities) {
            boolean canModerate =
                    permissionService.canModerateCommunity(userId, community.getId());

            moderationMap.put(community.getId(), canModerate);
        }

        model.addAttribute("moderationMap", moderationMap);
        // Friends
        List<User> friends = friendshipService.getFriends(userId);
        model.addAttribute("friends", friends);

        // Incoming friend requests
        List<FriendRequestDTO> requests = friendshipService.getIncomingRequests(userId);
        model.addAttribute("requests", requests);

        // Suggested friends
        List<User> suggested = friendshipService.getSuggestedFriends(userId);
        model.addAttribute("suggested", suggested);

        return "users/dashboard";
    }

    @PostMapping("/friends/request")
    public String sendFriendRequest(@RequestParam Integer friendId, HttpSession session, RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/users/login";

        friendshipService.sendFriendRequest(userId, friendId);
        redirectAttributes.addFlashAttribute("success",
                "Friend request sent");
        return "redirect:/users/dashboard";
    }

    @PostMapping("/friends/accept")
    public String acceptFriendRequest(@RequestParam Integer requestId, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/users/login";

        friendshipService.acceptFriendRequest(requestId);
        return "redirect:/users/dashboard";
    }
}