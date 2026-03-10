package com.mo.mediaodyssey.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import com.mo.mediaodyssey.models.Boards;
import com.mo.mediaodyssey.services.BoardsService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;




@Controller
@RequestMapping("/boards")
public class BoardsController {

    private final BoardsService boardsService; 

    public BoardsController (BoardsService boardsService) {
        this.boardsService = boardsService; 
    }

    @GetMapping("/create")
    public String createBoardPage(Model model) {
        model.addAttribute("board", new Boards());

        return "createBoard"; 
    }

    @PostMapping("/create")
    public String createBoard (@ModelAttribute("board") Boards board) {

        boardsService.createBoard(board);
        
        return "redirect:/";
    }
}
