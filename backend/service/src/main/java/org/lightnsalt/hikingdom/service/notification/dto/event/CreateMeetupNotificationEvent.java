package org.lightnsalt.hikingdom.service.notification.dto.event;

import java.time.LocalDateTime;
import java.util.List;

import org.lightnsalt.hikingdom.domain.entity.club.ClubMember;
import org.lightnsalt.hikingdom.domain.entity.member.Member;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateMeetupNotificationEvent {
	private final List<ClubMember> clubMemberList;
	private final Member host;
	private final Long clubId;
	private final Long meetupId;
	private final LocalDateTime startAt;
}
