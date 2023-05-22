package org.lightnsalt.hikingdom.service.club.repository.record;

import java.time.LocalDate;

import org.lightnsalt.hikingdom.domain.entity.club.record.ClubRanking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubRankingRepository extends JpaRepository<ClubRanking, Long> {
	ClubRanking findByClubIdAndSetDate(Long clubId, LocalDate setDate);
}
