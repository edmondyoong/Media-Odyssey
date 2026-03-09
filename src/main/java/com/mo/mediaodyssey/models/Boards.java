package com.mo.mediaodyssey.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Boards {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    private String board_name; 
    private String board_description; 
    private String board_type;
    private String board_avatar; 

    public Boards(){}

    public Boards (Long id, String board_name, String board_description, String board_avatar) {
        this.id = id; 
        this.board_name = board_name;
        this.board_description = board_description; 
        this.board_avatar = board_avatar; 
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBoard_name() {
        return board_name;
    }

    public void setBoard_name(String board_name) {
        this.board_name = board_name;
    }

    public String getBoard_description() {
        return board_description;
    }

    public void setBoard_description(String board_description) {
        this.board_description = board_description;
    }

    public String getBoard_type() {
        return board_type;
    }

    public void setBoard_type(String board_type) {
        this.board_type = board_type;
    }

    public String getBoard_avatar() {
        return board_avatar;
    }

    public void setBoard_avatar(String board_avatar) {
        this.board_avatar = board_avatar;
    }
}
