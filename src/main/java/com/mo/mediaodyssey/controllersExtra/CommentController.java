package com.mo.mediaodyssey.controllersExtra;

import com.mo.mediaodyssey.services.CommentService;
import com.mo.mediaodyssey.services.CommService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;
    private final CommService commService;

    public CommentController(CommentService commentService, CommService commService){
        this.commentService = commentService;
        this.commService = commService;
    }

    // Add a top-level comment
    @PostMapping("/post/{postId}")
    public String addComment(@PathVariable Integer postId,
                             @RequestParam String content,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        Integer userId = (Integer) session.getAttribute("userId");
        if(userId == null){
            redirectAttributes.addFlashAttribute("error","Please login to comment");
            return "redirect:/users/login";
        }
        try {
            commService.createComment(userId, postId, content); // uses permission check
            redirectAttributes.addFlashAttribute("success", "Comment added!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }


        // Redirect back to post page
        return "redirect:/posts/" + postId;
    }

    // Reply to an existing comment
    @PostMapping("/reply/{commentId}")
    public String replyComment(@PathVariable Integer commentId,
                               @RequestParam String content,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        Integer userId = (Integer) session.getAttribute("userId");
        if(userId == null){
            redirectAttributes.addFlashAttribute("error","Please login to reply");
            return "redirect:/users/login";
        }

        try {
            commService.replyToComment(userId, commentId, content); // permission-aware
            redirectAttributes.addFlashAttribute("success", "Reply added!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        // Find the parent post id
        Integer postId = commentService.getParentPostId(commentId);

        // Redirect back to the post view
        return "redirect:/posts/" + postId;
    }

    @PostMapping("/{commentId}/edit")
    public String editComment(@PathVariable Integer commentId,
                              @RequestParam String newContent,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        Integer userId = (Integer) session.getAttribute("userId");
        if(userId == null){
            redirectAttributes.addFlashAttribute("error","Please login to edit comments");
            return "redirect:/users/login";
        }

        try {
            commService.editComment(userId, commentId, newContent);
            redirectAttributes.addFlashAttribute("success", "Comment edited successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        Integer postId = commentService.getParentPostId(commentId);
        return "redirect:/posts/" + postId;
    }

    @PostMapping("/{commentId}/delete")
    public String deleteComment(@PathVariable Integer commentId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        Integer userId = (Integer) session.getAttribute("userId");
        if(userId == null){
            redirectAttributes.addFlashAttribute("error","Please login to delete comments");
            return "redirect:/users/login";
        }

        try {
            commService.deleteComment(userId, commentId);
            redirectAttributes.addFlashAttribute("success", "Comment deleted successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        Integer postId = commentService.getParentPostId(commentId);
        return "redirect:/posts/" + postId;
    }


}