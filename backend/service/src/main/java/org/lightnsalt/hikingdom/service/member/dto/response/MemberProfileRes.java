package org.lightnsalt.hikingdom.service.member.dto.response;

import java.util.List;

import org.lightnsalt.hikingdom.domain.entity.member.Member;
import org.lightnsalt.hikingdom.domain.entity.member.MemberHikingStatistic;
import org.lightnsalt.hikingdom.service.hiking.dto.response.HikingRecordRes;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfileRes {
	private String email;
	private String nickname;
	private Long level;
	private String profileUrl;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer unreadNotificationCount;
	private Long totalHikingCount;
	private Long totalMountainCount;
	private Long totalDuration;
	private double totalDistance;
	private double totalAlt;
	private List<HikingRecordRes> hikingRecords;

	public MemberProfileRes(Member member, MemberHikingStatistic hikingStatistic,
		List<HikingRecordRes> recordResList, Integer unreadAlarmCount) {
		this.email = member.getEmail();
		this.nickname = member.getNickname();
		this.level = Long.valueOf(member.getLevel().getId());
		this.profileUrl = member.getProfileUrl();
		this.unreadNotificationCount = unreadAlarmCount;
		this.totalHikingCount = hikingStatistic.getTotalHikingCount();
		this.totalMountainCount = hikingStatistic.getTotalMountainCount();
		this.totalDuration = hikingStatistic.getTotalDuration();
		this.totalDistance = hikingStatistic.getTotalDistance();
		this.totalAlt = hikingStatistic.getTotalAlt();
		this.hikingRecords = recordResList;
	}
}