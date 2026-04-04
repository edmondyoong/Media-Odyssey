package com.mo.mediaodyssey.controllersExtra;

import com.mo.mediaodyssey.services.CommentService;
import com.mo.mediaodyssey.services.CommuService;
import com.mo.mediaodyssey.shared.model.User;

import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;
    private final CommuService commuService;

    public CommentController(CommentService commentService, CommuService commuService) {
        this.commentService = commentService;
        this.commuService = commuService;
    }

    @PostMapping("/post/{postId}")
    public String addComment(@PathVariable Integer postId, @RequestParam String content,
            HttpSession session, RedirectAttributes redirectAttributes, Authentication authentication) {

        // TODO: changed to use id from /auth. Please clean up in future.
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();

        try {
            commuService.createComment(userId.intValue(), postId, content);
            redirectAttributes.addFlashAttribute("success", "Comment added!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/posts/" + postId;
    }

    @PostMapping("/reply/{commentId}")
    public String replyComment(@PathVariable Integer commentId, @RequestParam String content,
            HttpSession session, RedirectAttributes redirectAttributes, Authentication authentication) {

        // TODO: changed to use id from /auth. Please clean up in future.
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();

        try {
            commuService.replyToComment(userId.intValue(), commentId, content);
            redirectAttributes.addFlashAttribute("success", "Reply added!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/posts/" + commentService.getParentPostId(commentId);
    }

    @PostMapping("/{commentId}/edit")
    public String editComment(@PathVariable Integer commentId, @RequestParam String newContent,
            HttpSession session, RedirectAttributes redirectAttributes, Authentication authentication) {

        // TODO: changed to use id from /auth. Please clean up in future.
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();

        try {
            commuService.editComment(userId.intValue(), commentId, newContent);
            redirectAttributes.addFlashAttribute("success", "Comment edited successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/posts/" + commentService.getParentPostId(commentId);
    }

    @PostMapping("/{commentId}/delete")
    public String deleteComment(@PathVariable Integer commentId, HttpSession session,
            RedirectAttributes redirectAttributes, Authentication authentication) {

        // TODO: changed to use id from /auth. Please clean up in future.
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();

        try {
            commuService.deleteComment(userId.intValue(), commentId);
            redirectAttributes.addFlashAttribute("success", "Comment deleted successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/posts/" + commentService.getParentPostId(commentId);
    }
}