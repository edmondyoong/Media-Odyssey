package com.mo.mediaodyssey.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class MediaLike {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
