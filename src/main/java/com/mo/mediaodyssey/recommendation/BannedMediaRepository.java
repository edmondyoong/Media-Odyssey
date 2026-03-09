package com.mo.mediaodyssey.recommendation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BannedMediaRepository extends JpaRepository<BannedMedia, Long> {
    boolean existsByMediaApiId(String mediaApiId);
    void deleteByMediaApiId(String mediaApiId);
}