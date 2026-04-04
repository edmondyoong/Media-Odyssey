package com.mo.mediaodyssey.controllersExtra;

import com.mo.mediaodyssey.models.Community;
import com.mo.mediaodyssey.models.DTO.CommunityDTO;
import com.mo.mediaodyssey.models.DTO.CommunityMemberDTO;
import com.mo.mediaodyssey.models.DTO.PostDTO;
import com.mo.mediaodyssey.services.CommuService;
import com.mo.mediaodyssey.services.PostService;
import com.mo.mediaodyssey.shared.model.User;

import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/communities")
public class CommController {

    private final CommuService commuService;
    private final PostService postService;

    public CommController(CommuService commuService, PostService postService) {
        this.commuService = commuService;
        this.postService = postService;
    }

    @GetMapping("/create")
    public String showCreateCommunityPage() {
        return "communities/create-community";
    }

    @PostMapping("/create")
    public String createCommunity(@RequestParam String name, @RequestParam String description,
            HttpSession session, RedirectAttributes redirectAttributes, Authentication authentication) {

        // TODO: changed to use id from /auth. Please clean up in future.
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();

        commuService.createCommunity(userId.intValue(), name, description);
        redirectAttributes.addFlashAttribute("success", "Community Created Successfully");
        return "redirect:/communities";
    }

    @GetMapping
    public String listCommunities(Model model) {
        model.addAttribute("communities", commuService.getAllCommunities());
        return "communities/join-community";
    }

    @GetMapping("/{id}")
    public String viewCommunity(@PathVariable Integer id, Model model) {
        model.addAttribute("community", commuService.getCommunityById(id));
        model.addAttribute("memberCount", commuService.getMemberCount(id));
        model.addAttribute("posts", postService.getPostsByCommunityId(id));
        return "communities/view-community";
    }

    @PostMapping("/{communityId:\\d+}/join")
    public String joinCommunity(@PathVariable Integer communityId, HttpSession session,
            RedirectAttributes redirectAttributes, Authentication authentication) {

        // TODO: changed to use id from /auth. Please clean up in future.
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();

        try {
            commuService.joinCommunity(userId.intValue(), communityId);
            redirectAttributes.addFlashAttribute("success", "You joined the community!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/communities/" + communityId;
    }

    @PostMapping("/{communityId}/leave")
    public String leaveCommunity(@PathVariable Integer communityId, HttpSession session,
            RedirectAttributes redirectAttributes, Authentication authentication) {

        // TODO: changed to use id from /auth. Please clean up in future.
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();

        try {
            commuService.leaveCommunity(userId.intValue(), communityId);
            redirectAttributes.addFlashAttribute("success", "You left the community");
            return "redirect:/communities";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/communities/" + communityId;
        }
    }

    @GetMapping("/mod-community")
    public String modCommunity(HttpSession session,
            @RequestParam(required = false) Integer communityId, Model model, Authentication authentication) {

        // TODO: changed to use id from /auth. Please clean up in future.
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();

        model.addAttribute("communities", commuService.getOwnedCommunities(userId.intValue()));
        if (communityId != null) {
            model.addAttribute("members", commuService.getCommunityMembers(communityId));
            model.addAttribute("communityId", communityId);
        }
        return "communities/mod-community";
    }

    @PostMapping("/{communityId}/promote")
    public String promoteMember(@PathVariable Integer communityId, @RequestParam Integer targetUserId,
            HttpSession session, RedirectAttributes redirectAttributes, Authentication authentication) {

        // TODO: changed to use id from /auth. Please clean up in future.
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();

        try {
            commuService.promoteMember(userId.intValue(), targetUserId, communityId);
            redirectAttributes.addFlashAttribute("success", "Member promoted to moderator");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/communities/mod-community?communityId=" + communityId;
    }

    @PostMapping("/{communityId}/demote")
    public String demoteModerator(@PathVariable Integer communityId, @RequestParam Integer targetUserId,
            HttpSession session, RedirectAttributes redirectAttributes, Authentication authentication) {

        // TODO: changed to use id from /auth. Please clean up in future.
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();

        try {
            commuService.demoteModerator(userId.intValue(), targetUserId, communityId);
            redirectAttributes.addFlashAttribute("success", "Moderator demoted to member");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/communities/mod-community?communityId=" + communityId;
    }

    @PostMapping("/{communityId}/kick")
    public String kickMember(@PathVariable Integer communityId, @RequestParam Integer targetUserId,
            HttpSession session, RedirectAttributes redirectAttributes, Authentication authentication) {

        // TODO: changed to use id from /auth. Please clean up in future.
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();

        try {
            commuService.kickMember(userId.intValue(), targetUserId, communityId);
            redirectAttributes.addFlashAttribute("success", "Member kicked from community");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/communities/mod-community?communityId=" + communityId;
    }

    @PostMapping("/{communityId}/transfer")
    public String transferOwnership(@PathVariable Integer communityId, @RequestParam Integer targetUserId,
            HttpSession session, RedirectAttributes redirectAttributes, Authentication authentication) {

        // TODO: changed to use id from /auth. Please clean up in future.
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();

        try {
            commuService.transferOwnership(userId.intValue(), targetUserId, communityId);
            redirectAttributes.addFlashAttribute("success", "Ownership transferred");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/communities/mod-community?communityId=" + communityId;
    }

    @PostMapping("/{communityId}/edit")
    public String editCommunity(@PathVariable Integer communityId, @RequestParam String name,
            @RequestParam String description, HttpSession session,
            RedirectAttributes redirectAttributes, Authentication authentication) {

        // TODO: changed to use id from /auth. Please clean up in future.
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();

        try {
            commuService.editCommunity(userId.intValue(), communityId, name, description);
            redirectAttributes.addFlashAttribute("success", "Community updated");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/communities/" + communityId;
    }

    @PostMapping("/{communityId}/delete")
    public String deleteCommunity(@PathVariable Integer communityId, HttpSession session,
            RedirectAttributes redirectAttributes, Authentication authentication) {

        // TODO: changed to use id from /auth. Please clean up in future.
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();

        try {
            commuService.deleteCommunity(userId.intValue(), communityId);
            redirectAttributes.addFlashAttribute("success", "Community deleted");
            return "redirect:/communities";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/communities/" + communityId;
        }
    }
}