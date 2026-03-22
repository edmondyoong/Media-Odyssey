package com.mo.mediaodyssey.layout.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mo.mediaodyssey.layout.models.Boards;

@Repository
public interface BoardsRepository extends JpaRepository<Boards, Long> {
}
