package com.mayer.championsleaguedraw;

import com.mayer.championsleaguedraw.model.Team;
import com.mayer.championsleaguedraw.repository.TeamRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class DataLoader implements CommandLineRunner {

    private final TeamRepository teamRepository;

    public DataLoader(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        // Don't load the teams again if they already exist
        if (teamRepository.count() > 0) {
            return;
        }

        InputStream inputStream =
                getClass()
                        .getClassLoader()
                        .getResourceAsStream("teams.csv");

        if (inputStream == null) {
            throw new RuntimeException("teams.csv not found!");
        }

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                inputStream,
                                StandardCharsets.UTF_8
                        )
                );

        // Skip CSV header
        reader.readLine();

        String line;

        while ((line = reader.readLine()) != null) {

            String[] data = line.split(",");

            String country = data[0];
            String name = data[1];
            int pot = Integer.parseInt(data[2]);

            Team team = new Team(name, country, pot);

            teamRepository.save(team);
        }

        reader.close();

        System.out.println("Teams loaded successfully!");
    }
}