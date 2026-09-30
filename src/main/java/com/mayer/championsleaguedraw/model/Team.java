package com.mayer.championsleaguedraw.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity 
public class Team {

    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String country;
    private int pot;

public Team() {

}

public Team(Long id, String name, String country, int pot) {
    this.id = id;
    this.name = name;
    this.country = country;
    this.pot = pot;
}

public Team(String name, String country, int pot) {
    this.name = name;
    this.country = country;
    this.pot = pot;
}
    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getCountry() {
        return country;
    }
    public int getPot() {
        return pot;
    }

}
