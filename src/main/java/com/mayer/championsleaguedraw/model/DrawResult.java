package com.mayer.championsleaguedraw.model;

import java.util.List;

public class DrawResult {

    private Team team;

    private List<DrawOpponent> pot1Opponents;
    private List<DrawOpponent> pot2Opponents;
    private List<DrawOpponent> pot3Opponents;
    private List<DrawOpponent> pot4Opponents;

    public DrawResult(
            Team team,
            List<DrawOpponent> pot1Opponents,
            List<DrawOpponent> pot2Opponents,
            List<DrawOpponent> pot3Opponents,
            List<DrawOpponent> pot4Opponents) {

        this.team = team;
        this.pot1Opponents = pot1Opponents;
        this.pot2Opponents = pot2Opponents;
        this.pot3Opponents = pot3Opponents;
        this.pot4Opponents = pot4Opponents;
    }

    public Team getTeam() {
        return team;
    }

    public List<DrawOpponent> getPot1Opponents() {
        return pot1Opponents;
    }

    public List<DrawOpponent> getPot2Opponents() {
        return pot2Opponents;
    }

    public List<DrawOpponent> getPot3Opponents() {
        return pot3Opponents;
    }

    public List<DrawOpponent> getPot4Opponents() {
        return pot4Opponents;
    }
}