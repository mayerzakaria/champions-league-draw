package com.mayer.championsleaguedraw.model;

public class DrawOpponent {

    private Team team;
    private boolean home;

    public DrawOpponent(Team team, boolean home) {
        this.team = team;
        this.home = home;
    }

    public Team getTeam() {
        return team;
    }

    public boolean isHome() {
        return home;
    }
    public void setHome(boolean home) {
    this.home = home;
}
}