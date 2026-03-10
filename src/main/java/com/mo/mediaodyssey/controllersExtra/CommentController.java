package com.mo.mediaodyssey.controllersExtra;

import com.mo.mediaodyssey.services.CommentService;
import com.mo.mediaodyssey.services.CommuService;
import jakarta.servlet.http.HttpSession;
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
                              HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) { redirectAttributes.addFlashAttribute("error", "Please login to comment");
            return "redirect:/users/login"; }
        try { commuService.createComment(userId.intValue(), postId, content);
            redirectAttributes.addFlashAttribute("success", "Comment added!");
        } catch (RuntimeException e) { redirectAttributes.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/posts/" + postId;
    }

    @PostMapping("/reply/{commentId}")
    public String replyComment(@PathVariable Integer commentId, @RequestParam String content,
                                HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) { redirectAttributes.addFlashAttribute("error", "Please login to reply");
            return "redirect:/users/login"; }
        try { commuService.replyToComment(userId.intValue(), commentId, content);
            redirectAttributes.addFlashAttribute("success", "Reply added!");
        } catch (RuntimeException e) { redirectAttributes.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/posts/" + commentService.getParentPostId(commentId);
    }

    @PostMapping("/{commentId}/edit")
    public String editComment(@PathVariable Integer commentId, @RequestParam String newContent,
                               HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) { redirectAttributes.addFlashAttribute("error", "Please login to edit comments");
            return "redirect:/users/login"; }
        try { commuService.editComment(userId.intValue(), commentId, newContent);
            redirectAttributes.addFlashAttribute("success", "Comment edited successfully");
        } catch (RuntimeException e) { redirectAttributes.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/posts/" + commentService.getParentPostId(commentId);
    }

    @PostMapping("/{commentId}/delete")
    public String deleteComment(@PathVariable Integer commentId, HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) { redirectAttributes.addFlashAttribute("error", "Please login to delete comments");
            return "redirect:/users/login"; }
        try { commuService.deleteComment(userId.intValue(), commentId);
            redirectAttributes.addFlashAttribute("success", "Comment deleted successfully");
        } catch (RuntimeException e) { redirectAttributes.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/posts/" + commentService.getParentPostId(commentId);
    }
}