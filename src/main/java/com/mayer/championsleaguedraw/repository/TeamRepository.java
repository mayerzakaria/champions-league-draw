package com.mayer.championsleaguedraw.repository;

import com.mayer.championsleaguedraw.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface TeamRepository extends JpaRepository<Team, Long> 

{
    List<Team> findByPot(int pot);

}