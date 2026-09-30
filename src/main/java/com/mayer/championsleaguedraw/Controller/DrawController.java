package com.mayer.championsleaguedraw.Controller;

import com.mayer.championsleaguedraw.Service.DrawService;
import com.mayer.championsleaguedraw.model.DrawResult;
import com.mayer.championsleaguedraw.model.Team;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/draw")
public class DrawController {

    private final DrawService drawService;

    public DrawController(DrawService drawService) {
        this.drawService = drawService;
    }

    // Get all 36 teams
    @GetMapping("/teams")
    public List<Team> getTeams() {
        return drawService.getTeams();
    }

    // Get teams from a specific pot
    @GetMapping("/teams/pot/{pot}")
    public List<Team> getTeamsByPot(@PathVariable int pot) {
        return drawService.getTeamsByPot(pot);
    }

    // Get one team by ID
    @GetMapping("/teams/{id}")
    public ResponseEntity<Team> getTeamById(
            @PathVariable Long id) {

        Team team = drawService.getTeamById(id);

        if (team == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(team);
    }

    // Generate a completely new draw
    @PostMapping("/generate")
    public ResponseEntity<List<DrawResult>> generateFullDraw() {

        List<DrawResult> draw =
                drawService.generateFullDraw();

        return ResponseEntity.ok(draw);
    }

    // Get the draw for one selected team
    @GetMapping("/team/{id}")
    public ResponseEntity<DrawResult> getDrawForTeam(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                drawService.getDrawForTeam(id)
        );
    }
}