package org.lightnsalt.hikingdom.service.club.repository.record;

import java.util.List;

import org.lightnsalt.hikingdom.domain.entity.club.record.ClubMeetupHikingStatistic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubMeetupHikingStatisticRepository extends JpaRepository<ClubMeetupHikingStatistic, Long> {
	List<ClubMeetupHikingStatistic> findByMeetupId(Long meetupId);
}
