package com.mayer.championsleaguedraw.Service;

import com.mayer.championsleaguedraw.exception.TeamNotFoundException;
import com.mayer.championsleaguedraw.model.DrawOpponent;
import com.mayer.championsleaguedraw.model.DrawResult;
import com.mayer.championsleaguedraw.model.Team;
import com.mayer.championsleaguedraw.repository.TeamRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@Service
public class DrawService {

    private final TeamRepository teamRepository;

    // Team ID -> its opponents
    private final Map<Long, List<DrawOpponent>> currentDraw =
            new HashMap<>();

    private final Random random = new Random();

    public DrawService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    // =========================================================
    // TEAMS
    // =========================================================

    // Get all teams
    public List<Team> getTeams() {
        return teamRepository.findAll();
    }

    // Get one team
    public Team getTeamById(Long id) {
        return teamRepository.findById(id).orElse(null);
    }

    // Get teams from one pot
    public List<Team> getTeamsByPot(int pot) {
        return teamRepository.findByPot(pot);
    }

    // =========================================================
    // GENERATE DRAW
    // =========================================================

    // Generate the complete draw
    public List<DrawResult> generateFullDraw() {

        currentDraw.clear();

        List<Team> teams = teamRepository.findAll();

        // Create an empty list for every team
        for (Team team : teams) {

            currentDraw.put(
                    team.getId(),
                    new ArrayList<>()
            );
        }

        // Create matches inside each pot
        for (int pot = 1; pot <= 4; pot++) {

            List<Team> teamsInPot =
                    new ArrayList<>(
                            teamRepository.findByPot(pot)
                    );

            createSamePotPairings(teamsInPot);
        }

        // Create matches between different pots
        for (int pot1 = 1; pot1 <= 4; pot1++) {

            for (int pot2 = pot1 + 1;
                 pot2 <= 4;
                 pot2++) {

                List<Team> firstPot =
                        new ArrayList<>(
                                teamRepository.findByPot(pot1)
                        );

                List<Team> secondPot =
                        new ArrayList<>(
                                teamRepository.findByPot(pot2)
                        );

                createCrossPotPairings(
                        firstPot,
                        secondPot
                );
            }
        }

        /*
         * At this point every team should have
         * exactly 8 opponents.
         */
        for (Team team : teams) {

            if (currentDraw.get(team.getId()).size() != 8) {

                throw new IllegalStateException(
                        team.getName()
                                + " has "
                                + currentDraw.get(team.getId()).size()
                                + " opponents instead of 8."
                );
            }
        }

        // Assign exactly 4 home and 4 away
        assignHomeAway();

        // Make sure everything is correct
        if (!validateDraw()) {

            throw new IllegalStateException(
                    "Generated draw is invalid."
            );
        }

        List<DrawResult> results =
                new ArrayList<>();

        for (Team team : teams) {

            results.add(
                    createDrawResult(team)
            );
        }

        return results;
    }

    // =========================================================
    // SAME POT PAIRINGS
    // =========================================================

    // Create 2 opponents from the same pot
    private void createSamePotPairings(
            List<Team> teams) {

        boolean valid = false;

        while (!valid) {

            Collections.shuffle(
                    teams,
                    random
            );

            valid = true;

            for (int i = 0;
                 i < teams.size();
                 i++) {

                Team current =
                        teams.get(i);

                Team next =
                        teams.get(
                                (i + 1) % teams.size()
                        );

                if (current.getCountry()
                        .equals(next.getCountry())) {

                    valid = false;
                    break;
                }
            }
        }

        // Connect every team to the next team
        for (int i = 0;
             i < teams.size();
             i++) {

            Team current =
                    teams.get(i);

            Team next =
                    teams.get(
                            (i + 1) % teams.size()
                    );

            addPairing(
                    current,
                    next
            );
        }
    }

    // =========================================================
    // CROSS POT PAIRINGS
    // =========================================================

    // Create 2 opponents from another pot
    private void createCrossPotPairings(
            List<Team> firstPot,
            List<Team> secondPot) {

        boolean valid = false;

        while (!valid) {

            valid = true;

            List<Team> shuffledFirst =
                    new ArrayList<>(secondPot);

            List<Team> shuffledSecond =
                    new ArrayList<>(secondPot);

            Collections.shuffle(
                    shuffledFirst,
                    random
            );

            Collections.shuffle(
                    shuffledSecond,
                    random
            );

            // Check teams from the first pot
            for (int i = 0;
                 i < firstPot.size();
                 i++) {

                Team team =
                        firstPot.get(i);

                Team opponent1 =
                        shuffledFirst.get(i);

                Team opponent2 =
                        shuffledSecond.get(i);

                // Cannot get the same opponent twice
                if (opponent1.getId()
                        .equals(opponent2.getId())) {

                    valid = false;
                    break;
                }

                // Same country is not allowed
                if (team.getCountry()
                        .equals(opponent1.getCountry())
                        ||
                    team.getCountry()
                        .equals(opponent2.getCountry())) {

                    valid = false;
                    break;
                }

                int count1 =
                        countCountryOpponents(
                                team,
                                opponent1.getCountry()
                        );

                int count2 =
                        countCountryOpponents(
                                team,
                                opponent2.getCountry()
                        );

                if (count1 >= 2) {

                    valid = false;
                    break;
                }

                if (count2 >= 2) {

                    valid = false;
                    break;
                }

                if (opponent1.getCountry()
                        .equals(opponent2.getCountry())) {

                    if (count1 + 2 > 2) {

                        valid = false;
                        break;
                    }
                }
            }

            // Check teams from the second pot
            if (valid) {

                for (Team team : secondPot) {

                    // Check first shuffled list
                    for (int j = 0;
                         j < firstPot.size();
                         j++) {

                        if (shuffledFirst.get(j)
                                .getId()
                                .equals(team.getId())) {

                            Team opponent =
                                    firstPot.get(j);

                            if (team.getCountry()
                                    .equals(
                                            opponent.getCountry()
                                    )) {

                                valid = false;
                                break;
                            }

                            int count =
                                    countCountryOpponents(
                                            team,
                                            opponent.getCountry()
                                    );

                            if (count >= 2) {

                                valid = false;
                                break;
                            }
                        }
                    }

                    if (!valid) {
                        break;
                    }

                    // Check second shuffled list
                    for (int j = 0;
                         j < firstPot.size();
                         j++) {

                        if (shuffledSecond.get(j)
                                .getId()
                                .equals(team.getId())) {

                            Team opponent =
                                    firstPot.get(j);

                            if (team.getCountry()
                                    .equals(
                                            opponent.getCountry()
                                    )) {

                                valid = false;
                                break;
                            }

                            int count =
                                    countCountryOpponents(
                                            team,
                                            opponent.getCountry()
                                    );

                            if (count >= 2) {

                                valid = false;
                                break;
                            }
                        }
                    }

                    if (!valid) {
                        break;
                    }
                }
            }

            // Save the pairings
            if (valid) {

                for (int i = 0;
                     i < firstPot.size();
                     i++) {

                    addPairing(
                            firstPot.get(i),
                            shuffledFirst.get(i)
                    );

                    addPairing(
                            firstPot.get(i),
                            shuffledSecond.get(i)
                    );
                }
            }
        }
    }

    // =========================================================
    // COUNTRY CHECK
    // =========================================================

    // Count opponents from a specific country
    private int countCountryOpponents(
            Team team,
            String country) {

        int count = 0;

        List<DrawOpponent> opponents =
                currentDraw.get(
                        team.getId()
                );

        if (opponents == null) {
            return 0;
        }

        for (DrawOpponent opponent :
                opponents) {

            if (opponent.getTeam()
                    .getCountry()
                    .equals(country)) {

                count++;
            }
        }

        return count;
    }

    // =========================================================
    // ADD MATCH
    // =========================================================

    // Add the same match to both teams
    private void addPairing(
            Team team1,
            Team team2) {

        currentDraw
                .get(team1.getId())
                .add(
                        new DrawOpponent(
                                team2,
                                false
                        )
                );

        currentDraw
                .get(team2.getId())
                .add(
                        new DrawOpponent(
                                team1,
                                false
                        )
                );
    }

    // =========================================================
    // HOME / AWAY
    // =========================================================

    /*
     * Give every team exactly:
     *
     * 4 HOME
     * 4 AWAY
     *
     * We use an Euler circuit.
     */
    private void assignHomeAway() {

        List<Team> teams =
                teamRepository.findAll();

        /*
         * Create a graph.
         *
         * Team = vertex
         * Match = edge
         */
        Map<Long, List<MatchPair>> graph =
                new HashMap<>();

        for (Team team : teams) {

            graph.put(
                    team.getId(),
                    new ArrayList<>()
            );
        }

        /*
         * Create every match only once.
         */
        for (Team team : teams) {

            for (DrawOpponent opponent :
                    currentDraw.get(team.getId())) {

                Team other =
                        opponent.getTeam();

                if (team.getId() < other.getId()) {

                    MatchPair match =
                            new MatchPair(
                                    team,
                                    other
                            );

                    graph.get(team.getId())
                            .add(match);

                    graph.get(other.getId())
                            .add(match);
                }
            }
        }

        /*
         * Randomize the graph.
         *
         * This makes the Home/Away result
         * different each time.
         */
        for (List<MatchPair> matches :
                graph.values()) {

            Collections.shuffle(
                    matches,
                    random
            );
        }

        Set<MatchPair> used =
                new HashSet<>();

        /*
         * The graph can theoretically contain
         * more than one connected component.
         *
         * Therefore we process every component.
         */
        for (Team start : teams) {

            if (hasUnusedMatch(
                    start,
                    graph,
                    used)) {

                List<Traversal> circuit =
                        new ArrayList<>();

                buildEulerCircuit(
                        start,
                        graph,
                        used,
                        circuit
                );

                /*
                 * The recursive algorithm creates
                 * the circuit backwards.
                 *
                 * Reverse it to get the real
                 * direction of travel.
                 */
                Collections.reverse(
                        circuit
                );

                /*
                 * If the Euler traversal goes:
                 *
                 * A -> B
                 *
                 * A is HOME
                 * B is AWAY
                 */
                for (Traversal traversal :
                        circuit) {

                    setMatchHome(
                            traversal.from,
                            traversal.to,
                            true
                    );
                }
            }
        }

        /*
         * Final check.
         */
        for (Team team : teams) {

            int home = 0;
            int away = 0;

            for (DrawOpponent opponent :
                    currentDraw.get(
                            team.getId()
                    )) {

                if (opponent.isHome()) {
                    home++;
                } else {
                    away++;
                }
            }

            if (home != 4 || away != 4) {

                throw new IllegalStateException(
                        "Could not create 4 home / 4 away for "
                                + team.getName()
                                + " (Home: "
                                + home
                                + ", Away: "
                                + away
                                + ")"
                );
            }
        }
    }

    /*
     * Check whether this team still has
     * an unused match.
     */
    private boolean hasUnusedMatch(
            Team team,
            Map<Long, List<MatchPair>> graph,
            Set<MatchPair> used) {

        for (MatchPair match :
                graph.get(team.getId())) {

            if (!used.contains(match)) {
                return true;
            }
        }

        return false;
    }

    /*
     * Build an Euler circuit.
     *
     * We travel through every match exactly once.
     */
    private void buildEulerCircuit(
            Team current,
            Map<Long, List<MatchPair>> graph,
            Set<MatchPair> used,
            List<Traversal> circuit) {

        for (MatchPair match :
                graph.get(current.getId())) {

            if (used.contains(match)) {
                continue;
            }

            used.add(match);

            Team next;

            if (match.team1.getId()
                    .equals(current.getId())) {

                next = match.team2;

            } else {

                next = match.team1;
            }

            /*
             * Continue walking through the graph.
             */
            buildEulerCircuit(
                    next,
                    graph,
                    used,
                    circuit
            );

            /*
             * Remember the direction:
             *
             * current -> next
             */
            circuit.add(
                    new Traversal(
                            current,
                            next
                    )
            );
        }
    }

    /*
     * Represents one step in the Euler circuit.
     */
    private static class Traversal {

        private final Team from;
        private final Team to;

        private Traversal(
                Team from,
                Team to) {

            this.from = from;
            this.to = to;
        }
    }

    /*
     * Represents one match.
     */
    private static class MatchPair {

        private final Team team1;
        private final Team team2;

        private MatchPair(
                Team team1,
                Team team2) {

            this.team1 = team1;
            this.team2 = team2;
        }
    }

    // =========================================================
    // SET HOME / AWAY
    // =========================================================

    /*
     * Set one match.
     *
     * If team1 is HOME:
     *
     * team1 = true
     * team2 = false
     *
     * This guarantees that both teams
     * see the same match correctly.
     */
    private void setMatchHome(
            Team team1,
            Team team2,
            boolean team1Home) {

        setOpponentHomeValue(
                team1,
                team2,
                team1Home
        );

        setOpponentHomeValue(
                team2,
                team1,
                !team1Home
        );
    }

    // Change the Home/Away value of one opponent
    private void setOpponentHomeValue(
            Team team,
            Team opponent,
            boolean home) {

        List<DrawOpponent> opponents =
                currentDraw.get(
                        team.getId()
                );

        for (DrawOpponent drawOpponent :
                opponents) {

            if (drawOpponent.getTeam()
                    .getId()
                    .equals(opponent.getId())) {

                drawOpponent.setHome(home);
                return;
            }
        }
    }

    // =========================================================
    // RESULT
    // =========================================================

    // Put opponents into their correct pots
    private DrawResult createDrawResult(
            Team team) {

        List<DrawOpponent> opponents =
                currentDraw.get(
                        team.getId()
                );

        List<DrawOpponent> pot1 =
                new ArrayList<>();

        List<DrawOpponent> pot2 =
                new ArrayList<>();

        List<DrawOpponent> pot3 =
                new ArrayList<>();

        List<DrawOpponent> pot4 =
                new ArrayList<>();

        for (DrawOpponent opponent :
                opponents) {

            switch (opponent.getTeam().getPot()) {

                case 1:
                    pot1.add(opponent);
                    break;

                case 2:
                    pot2.add(opponent);
                    break;

                case 3:
                    pot3.add(opponent);
                    break;

                case 4:
                    pot4.add(opponent);
                    break;
            }
        }

        return new DrawResult(
                team,
                pot1,
                pot2,
                pot3,
                pot4
        );
    }

    // =========================================================
    // VALIDATION
    // =========================================================

   private boolean validateDraw() {

    List<Team> teams = teamRepository.findAll();

    for (Team team : teams) {

        List<DrawOpponent> opponents =
                currentDraw.get(team.getId());

        if (opponents == null) {

            System.out.println(
                    "INVALID: " + team.getName()
                            + " has no opponent list"
            );

            return false;
        }

        // 1. Exactly 8 opponents
        if (opponents.size() != 8) {

            System.out.println(
                    "INVALID: " + team.getName()
                            + " has "
                            + opponents.size()
                            + " opponents"
            );

            return false;
        }

        int homeCount = 0;
        int awayCount = 0;

        // Check every opponent
        for (DrawOpponent drawOpponent :
                opponents) {

            Team opponent =
                    drawOpponent.getTeam();

            // Cannot play yourself
            if (team.getId()
                    .equals(opponent.getId())) {

                System.out.println(
                        "INVALID: " + team.getName()
                                + " plays itself"
                );

                return false;
            }

            // Same country
            if (team.getCountry()
                    .equals(opponent.getCountry())) {

                System.out.println(
                        "INVALID: "
                                + team.getName()
                                + " plays "
                                + opponent.getName()
                                + " from the same country"
                );

                return false;
            }

            if (drawOpponent.isHome()) {
                homeCount++;
            } else {
                awayCount++;
            }

            // Check reverse match
            DrawOpponent reverse =
                    findOpponentEntry(
                            opponent,
                            team
                    );

            if (reverse == null) {

                System.out.println(
                        "INVALID: "
                                + team.getName()
                                + " vs "
                                + opponent.getName()
                                + " has no reverse match"
                );

                return false;
            }

            // Home/Away must be opposite
            if (drawOpponent.isHome()
                    == reverse.isHome()) {

                System.out.println(
                        "INVALID HOME/AWAY: "
                                + team.getName()
                                + " vs "
                                + opponent.getName()
                );

                return false;
            }
        }

        // 2. Four home
        if (homeCount != 4) {

            System.out.println(
                    "INVALID: "
                            + team.getName()
                            + " has "
                            + homeCount
                            + " home"
            );

            return false;
        }

        // 3. Four away
        if (awayCount != 4) {

            System.out.println(
                    "INVALID: "
                            + team.getName()
                            + " has "
                            + awayCount
                            + " away"
            );

            return false;
        }

        // 4. Maximum two from same country
        Map<String, Integer> countryCounts =
                new HashMap<>();

        for (DrawOpponent opponent :
                opponents) {

            String country =
                    opponent.getTeam()
                            .getCountry();

            countryCounts.put(
                    country,
                    countryCounts.getOrDefault(
                            country,
                            0
                    ) + 1
            );
        }

        for (Map.Entry<String, Integer> entry :
                countryCounts.entrySet()) {

            if (entry.getValue() > 2) {

                System.out.println(
                        "INVALID COUNTRY: "
                                + team.getName()
                                + " has "
                                + entry.getValue()
                                + " opponents from "
                                + entry.getKey()
                );

                return false;
            }
        }

        // 5. Exactly two from every pot
        for (int pot = 1; pot <= 4; pot++) {

            int count = 0;

            for (DrawOpponent opponent :
                    opponents) {

                if (opponent.getTeam().getPot()
                        == pot) {

                    count++;
                }
            }

            if (count != 2) {

                System.out.println(
                        "INVALID POT: "
                                + team.getName()
                                + " has "
                                + count
                                + " opponents from pot "
                                + pot
                );

                return false;
            }
        }
    }

    System.out.println(
            "DRAW VALIDATION PASSED"
    );

    return true;
}

    // =========================================================
    // FIND OPPONENT
    // =========================================================

    // Find one team's view of a match
    private DrawOpponent findOpponentEntry(
            Team team,
            Team opponent) {

        List<DrawOpponent> opponents =
                currentDraw.get(
                        team.getId()
                );

        if (opponents == null) {
            return null;
        }

        for (DrawOpponent drawOpponent :
                opponents) {

            if (drawOpponent.getTeam()
                    .getId()
                    .equals(opponent.getId())) {

                return drawOpponent;
            }
        }

        return null;
    }

    // =========================================================
    // GET DRAW FOR ONE TEAM
    // =========================================================

    // Get the draw for one team
    public DrawResult getDrawForTeam(
            Long teamId) {

        Team team =
                getTeamById(teamId);

        if (team == null) {

            throw new TeamNotFoundException(
                    teamId
            );
        }

        // Generate automatically if needed
        if (currentDraw.isEmpty()) {

            generateFullDraw();
        }

        return createDrawResult(team);
    }
}