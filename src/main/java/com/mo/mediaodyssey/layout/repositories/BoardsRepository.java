package com.mo.mediaodyssey.layout.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mo.mediaodyssey.layout.models.Boards;
import java.util.List;
import com.mo.mediaodyssey.auth.model.User;


@Repository
public interface BoardsRepository extends JpaRepository<Boards, Long> {

    // find boards by user
    List<Boards> findByUser(User user);
}
