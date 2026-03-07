package com.mo.mediaodyssey.recommendation;

// TODO: replace with correct import once teammate creates UserInteraction database
// TODO: replace object names and rewrite the code based on teammates implementaion
// import com.mo.mediaodyssey.TEAMMATE_PACKAGE.UserInteraction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {

    // Get all interactions for a user
    List<UserInteraction> findByUserId(Long userId);

    // Get all interactions of a specific type for a user (VIEW, LIKE, RATE)
    List<UserInteraction> findByUserIdAndInteractionType(Long userId, String interactionType);
}
