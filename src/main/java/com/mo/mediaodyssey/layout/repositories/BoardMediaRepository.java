package com.mo.mediaodyssey.layout.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mo.mediaodyssey.layout.models.BoardMedia;

@Repository
public interface BoardMediaRepository extends JpaRepository<BoardMedia, Long>{

    List<BoardMedia> findByBoardId (Long id);

    boolean existByBoardIdAndMediaApiId (Long boardId, Long mediaApiId);
    
}