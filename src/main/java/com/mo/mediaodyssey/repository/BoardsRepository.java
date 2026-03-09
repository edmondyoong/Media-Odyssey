package com.mo.mediaodyssey.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mo.mediaodyssey.models.Boards;

public interface BoardsRepository extends JpaRepository<Boards, Long> {

}
