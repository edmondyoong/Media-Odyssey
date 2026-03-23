package com.mo.mediaodyssey.layout.controllers;

import org.springframework.security.core.Authentication;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import com.mo.mediaodyssey.auth.model.User;
import com.mo.mediaodyssey.layout.models.Boards;
import com.mo.mediaodyssey.layout.services.BoardsService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



@Controller
@RequestMapping("/boards")
public class BoardsController {
    /* boards controller is a mapping controller for board related htmls */

    private final BoardsService boardsService; 

    public BoardsController (BoardsService boardsService) {
        this.boardsService = boardsService; 
    }


    /* Bring user to the page to create a board */
    @GetMapping("/create")
    public String createBoardPage(Model model) {
        model.addAttribute("board", new Boards());

        return "boardsLayout/themeBoard/createBoard"; 
    }

    /* After user finished created a board, they should be brought back to the homePage 
    *
    ** Notice: the return is set to redirect for the page to automatically reload, in order for the newly
        created boards to appear. If return is setted to actual path of returning to homePage.html then
        no boards will show and error will happen.
    */
    @PostMapping("/create")
    public String createBoard (@ModelAttribute("board") Boards board, Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        board.setUser(user);
        boardsService.createBoard(board);
        
        return "redirect:/";
    }

    /* Theme boards that are displayed on the homePage are clickable. 
    After clicking on those boards, users will be able to see the details of that boards. 
    Which would be showing the media, description, post, .... */
    @GetMapping("/display/{id}")
    public String navToboardDisplay(@PathVariable("id") Long id, 
                                    Model model, RedirectAttributes redirectAtrrAttributes,
                                    Authentication authentication) {

        // Find whether the board exists or not. 
        Optional<Boards> boardOpt = boardsService.findBoardById(id); 

        // If board exists
        if (boardOpt.isPresent()) {
            Boards board = boardOpt.get(); //store board's data
                
            model.addAttribute("board", board);
            return "boardsLayout/themeBoard/boardDisplay";
        } else {
            redirectAtrrAttributes.addFlashAttribute("errorMessage", "Board not found or have been deleted.");
            return "redirect:/";
        }
    }
    
}
