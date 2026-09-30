package com.mayer.championsleaguedraw.exception;

public class TeamNotFoundException extends RuntimeException {

    public TeamNotFoundException(Long id) {
        super("Team with ID " + id + " was not found.");
    }
}