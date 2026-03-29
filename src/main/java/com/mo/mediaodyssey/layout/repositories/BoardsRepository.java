package com.mo.mediaodyssey.layout.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mo.mediaodyssey.layout.models.Boards;
import com.mo.mediaodyssey.shared.model.User;

import java.util.List;

@Repository
public interface BoardsRepository extends JpaRepository<Boards, Long> {

    // find boards by user
    List<Boards> findByUser(User user);
}
