package com.mo.mediaodyssey.controllersExtra;

import com.mo.mediaodyssey.models.Community;
import com.mo.mediaodyssey.models.DTO.CommunityDTO;
import com.mo.mediaodyssey.models.DTO.CommunityMemberDTO;
import com.mo.mediaodyssey.models.DTO.PostDTO;
import com.mo.mediaodyssey.services.CommunityService;

import com.mo.mediaodyssey.services.PostService;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/communities")
public class CommController {

    private final CommunityService communityService;
    private final PostService postService;

    public CommController(
            CommunityService communityService,
            PostService postService) {
        this.communityService = communityService;
        this.postService = postService;
    }


    // Show create community page
    @GetMapping("/create")
    public String showCreateCommunityPage() {
        return "communities/create-community";
    }

    // Create community
    @PostMapping("/create")
    public String createCommunity(
            @RequestParam String name,
            @RequestParam String description,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/users/login";
        }

        communityService.createCommunity(userId, name, description);
        redirectAttributes.addFlashAttribute("success", "Community Created Successfully");
        return "redirect:/communities";
    }

    // Show all communities
    @GetMapping
    public String listCommunities(Model model) {
        List<Community> communities = communityService.getAllCommunities();
        model.addAttribute("communities", communities);
        return "communities/join-community";
    }


    // View a community
    @GetMapping("/{id}")
    public String viewCommunity(@PathVariable Integer id, Model model){

        Community community = communityService.getCommunityById(id);
        Integer memberCount = communityService.getMemberCount(id);
        List<PostDTO> posts = postService.getPostsByCommunityId(id);


        model.addAttribute("community",community);
        model.addAttribute("memberCount",memberCount);
        model.addAttribute("posts",posts);

        return "communities/view-community";
    }




    @PostMapping("/{communityId:\\d+}/join")
    public String joinCommunity(
            @PathVariable Integer communityId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/users/login";
        }

        try {
            communityService.joinCommunity(userId, communityId);
            redirectAttributes.addFlashAttribute("success", "You joined the community!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/communities/" + communityId;
    }

    // Leave community
    @PostMapping("/{communityId}/leave")
    public String leaveCommunity(
            @PathVariable Integer communityId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/users/login";
        }

        try {
            communityService.leaveCommunity(userId, communityId);
            redirectAttributes.addFlashAttribute("success", "You left the community");
            return "redirect:/communities";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/communities/" + communityId;
        }
    }

    @GetMapping("/mod-community")
    public String modCommunity(
            @SessionAttribute("userId") Integer userId,
            @RequestParam(required = false) Integer communityId,
            Model model) {

        List<CommunityDTO> communities = communityService.getOwnedCommunities(userId);
        model.addAttribute("communities", communities);

        if (communityId != null) {
            // Fetch ALL members for this community
            List<CommunityMemberDTO> members = communityService.getCommunityMembers(communityId);
            model.addAttribute("members", members);
            model.addAttribute("communityId", communityId);
        }

        return "communities/mod-community";
    }

    @PostMapping("/{communityId}/promote")
    public String promoteMember(
            @PathVariable Integer communityId,
            @RequestParam Integer targetUserId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer actingUserId = (Integer) session.getAttribute("userId");

        try {
            communityService.promoteMember(actingUserId, targetUserId, communityId);
            redirectAttributes.addFlashAttribute("success", "Member promoted to moderator");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        // Pass communityId as query parameter
        return "redirect:/communities/mod-community?communityId=" + communityId;
    }

    @PostMapping("/{communityId}/demote")
    public String demoteModerator(
            @PathVariable Integer communityId,
            @RequestParam Integer targetUserId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer actingUserId = (Integer) session.getAttribute("userId");

        try {
            communityService.demoteModerator(actingUserId, targetUserId, communityId);
            redirectAttributes.addFlashAttribute("success", "Moderator demoted to member");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/communities/mod-community?communityId=" + communityId;
    }

    @PostMapping("/{communityId}/kick")
    public String kickMember(
            @PathVariable Integer communityId,
            @RequestParam Integer targetUserId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer actingUserId = (Integer) session.getAttribute("userId");

        try {
            communityService.kickMember(actingUserId, targetUserId, communityId);
            redirectAttributes.addFlashAttribute("success", "Member kicked from community");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/communities/mod-community?communityId=" + communityId;
    }

    @PostMapping("/{communityId}/transfer")
    public String transferOwnership(
            @PathVariable Integer communityId,
            @RequestParam Integer targetUserId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer actingUserId = (Integer) session.getAttribute("userId");

        try {
            communityService.transferOwnership(actingUserId, targetUserId, communityId);
            redirectAttributes.addFlashAttribute("success", "Ownership transferred");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/communities/mod-community?communityId=" + communityId;
    }

    @PostMapping("/{communityId}/edit")
    public String editCommunity(
            @PathVariable Integer communityId,
            @RequestParam String name,
            @RequestParam String description,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer actingUserId = (Integer) session.getAttribute("userId");

        try {
            communityService.editCommunity(actingUserId, communityId, name, description);
            redirectAttributes.addFlashAttribute("success", "Community updated");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/communities/" + communityId;
    }


    @PostMapping("/{communityId}/delete")
    public String deleteCommunity(
            @PathVariable Integer communityId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer actingUserId = (Integer) session.getAttribute("userId");

        try {
            communityService.deleteCommunity(actingUserId, communityId);
            redirectAttributes.addFlashAttribute("success", "Community deleted");
            return "redirect:/communities";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/communities/" + communityId;
        }
    }


}

