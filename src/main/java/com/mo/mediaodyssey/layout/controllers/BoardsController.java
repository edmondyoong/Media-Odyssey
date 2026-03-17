package com.mo.mediaodyssey.layout.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.mo.mediaodyssey.layout.models.Boards;
import com.mo.mediaodyssey.layout.services.BoardsService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;


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

        return "pages/createBoard"; 
    }

    /* After user finished created a board, they should be brought back to the homePage */
    @PostMapping("/create")
    public String createBoard (@ModelAttribute("board") Boards board) {

        boardsService.createBoard(board);
        
        return "boardsLayout/homePage";
    }
}
