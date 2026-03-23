package com.mo.mediaodyssey.layout.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mo.mediaodyssey.layout.models.BoardMedia;
import com.mo.mediaodyssey.layout.models.Boards;
import com.mo.mediaodyssey.layout.repositories.BoardMediaRepository;
import com.mo.mediaodyssey.layout.repositories.BoardsRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/boards")
public class boardMediaController {
    
    private final BoardMediaRepository boardMediaRepository; 
    private final BoardsRepository boardsRepository; 

    public boardMediaController (BoardMediaRepository boardMediaRepository, BoardsRepository boardsRepository) {
        this.boardMediaRepository = boardMediaRepository; 
        this.boardsRepository = boardsRepository;
    }

    /*
    * *** This function is used to Post request of adding media into a board. 
    * ** Notice: we most likely don't want users to duplicate any movie/music/game in their board.
    * ** Therefore, this function will check if the selected media has existed in the chosen board or not.
    * 
    * *** This request will only be sent from {media}Display/html. Which means mediaApiId is already there.
    * ** Auth ensures only logged users can access to boards and movies. 
    * ** Therefore, there is no need to doublecheck mediApiId or user_id. 
    */
    @PostMapping("/{board_id}/media")
    public ResponseEntity<?> addMediaToBoard( @PathVariable Long board_id, @RequestBody BoardMedia boardMedia,
                                            Authentication authentication){

        // Find the board by its id
        Boards board = boardsRepository.findById(board_id)
            .orElseThrow(() -> new RuntimeException("Cannot find your board. Please try again."));

        if (boardMediaRepository.existsByBoardIdAndMediaApiId(board_id, boardMedia.getMediaApiId())){
            return ResponseEntity.badRequest().body("You've already added this in the board.");
        }

        boardMedia.setBoard(board);
        boardMediaRepository.save(boardMedia); 

        return ResponseEntity.ok("Added Successfully!");
    }
    
}
