package com.mo.mediaodyssey.layout.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mo.mediaodyssey.auth.model.User;
import com.mo.mediaodyssey.layout.models.Boards;
import com.mo.mediaodyssey.layout.repositories.BoardsRepository;

@Service
public class BoardsService {
    
    private final BoardsRepository boardsRepository; 

    public BoardsService (BoardsRepository boardsRepository) {
        this.boardsRepository = boardsRepository; 
    }

    public void createBoard(Boards board) {
        boardsRepository.save(board); 
    }

    public List<Boards> findAllBoards () {
        return boardsRepository.findAll();
    }

    public Optional<Boards> findBoardById(Long id) {
        return boardsRepository.findById(id); 
    }

    public List<Boards> findBoardsByUser (User user) {
        return boardsRepository.findByUser(user); 
    }
}
