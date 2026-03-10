package com.mo.mediaodyssey.controls;

import com.mo.mediaodyssey.enums.RoleType;
import com.mo.mediaodyssey.models.CommunityRole;
import com.mo.mediaodyssey.models.DTO.CommentDTO;
import com.mo.mediaodyssey.models.Post;
import com.mo.mediaodyssey.models.User;
import com.mo.mediaodyssey.repositories.CommunityRoleRepository;
import com.mo.mediaodyssey.repositories.UserRepository;
import com.mo.mediaodyssey.services.CommentService;
import com.mo.mediaodyssey.services.PostService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final CommentService commentService;
    private final CommunityRoleRepository communityRoleRepo;
    private final UserRepository userRepo;
    public PostController(PostService postService, CommentService commentService, CommunityRoleRepository communityRoleRepo, UserRepository userRepo){
        this.postService = postService;
        this.commentService = commentService;
        this.communityRoleRepo = communityRoleRepo;
        this.userRepo = userRepo;
    }

    // Show the Create Post Form
    @GetMapping("/create/{communityId}")
    public String showCreatePostForm(@PathVariable Integer communityId, Model model){
        model.addAttribute("communityId", communityId);
        return "posts/create-post"; //
    }

    // Handle Post Submission
    @PostMapping("/create/{communityId}")
    public String createPost(@PathVariable Integer communityId,
                             @RequestParam String title,
                             @RequestParam String content,
                             HttpSession session,
                             RedirectAttributes redirectAttributes){

        Integer userId = (Integer) session.getAttribute("userId");
        if(userId == null){
            redirectAttributes.addFlashAttribute("error","Please login to create a post");
            return "redirect:/users/login";
        }

        postService.createPost(userId, communityId, title, content);
        redirectAttributes.addFlashAttribute("success","Post created successfully");

        // Redirect back to the community page
        return "redirect:/communities/" + communityId;
    }

    @GetMapping("/{postId}/edit")
    public String showEditPostForm(@PathVariable Integer postId,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes redirectAttributes){

        Integer userId = (Integer) session.getAttribute("userId");
        if(userId == null){
            redirectAttributes.addFlashAttribute("error","Please login to edit posts");
            return "redirect:/users/login";
        }

        Post post = postService.getPostById(postId);

        if(!post.getAuthorId().equals(userId)){
            redirectAttributes.addFlashAttribute("error","You can only edit your own posts");
            return "redirect:/posts/" + postId;
        }

        model.addAttribute("post", post);

        return "posts/view-post";
    }


    // Delete a post
    @PostMapping("/{postId}/delete")
    public String deletePost(@PathVariable Integer postId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes){

        Integer userId = (Integer) session.getAttribute("userId");
        if(userId == null){
            redirectAttributes.addFlashAttribute("error","Please login to delete posts");
            return "redirect:/users/login";
        }

        try {
            postService.deletePost(userId, postId);
            redirectAttributes.addFlashAttribute("success","Post deleted successfully");

            // Redirect back to the community page
            Post post = postService.getPostById(postId);
            return "redirect:/communities/" + post.getCommunityId();
        } catch (RuntimeException e){
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            Post post = postService.getPostById(postId);
            return "redirect:/communities/" + post.getCommunityId();
        }
    }



    //view post
    @GetMapping("/{postId}")
    public String viewPost(@PathVariable Integer postId, Model model, HttpSession session){
        Post post = postService.getPostById(postId);
        String username = userRepo.findById(post.getAuthorId())
                .map(User::getUsername)
                .orElse("Unknown");


        List<CommentDTO> comments = commentService.getCommentsWithDepth(postId);
        Integer currentUserId = (Integer) session.getAttribute("userId");
        RoleType currentUserRole = null;

        if(currentUserId != null){
            Optional<CommunityRole> roleOpt = communityRoleRepo
                    .findByUserIdAndCommunityId(currentUserId, post.getCommunityId());
            if(roleOpt.isPresent()){
                currentUserRole = roleOpt.get().getRoleType();
            }
        }

        model.addAttribute("post", post);
        model.addAttribute("postUsername", username);
        model.addAttribute("comments", comments);
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("currentUserRole", currentUserRole);

        return "posts/view-post";
    }

}