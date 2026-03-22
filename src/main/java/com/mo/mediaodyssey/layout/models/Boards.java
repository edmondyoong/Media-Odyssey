package com.mo.mediaodyssey.layout.models;

import com.mo.mediaodyssey.auth.model.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="boards")
public class Boards {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    private String board_name; 
    private String board_description; 
    private String board_type;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    public Boards(){}
    public Boards (Long id, String board_name, String board_description) {
        this.id = id; 
        this.board_name = board_name;
        this.board_description = board_description; 
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

    public User getUser(){
        return user;
    }

    public void setUser (User user){
        this.user = user; 
    }
}
