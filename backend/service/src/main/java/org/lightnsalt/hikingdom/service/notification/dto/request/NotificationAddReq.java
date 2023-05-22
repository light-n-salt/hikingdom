package org.lightnsalt.hikingdom.service.notification.dto.request;

import java.time.LocalDateTime;

import org.lightnsalt.hikingdom.domain.common.enumType.NotificationType;
import org.lightnsalt.hikingdom.domain.entity.member.Member;
import org.lightnsalt.hikingdom.domain.entity.notification.Notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationAddReq {
	private Member member;
	private NotificationType category;
	private String title;
	private String body;
	private String url;
	private LocalDateTime sendAt;
	private Long clubId;
	private Long meetupId;

	public Notification toEntity() {
		return Notification.builder()
			.member(member)
			.category(category)
			.title(title)
			.body(body)
			.url(url)
			.sendAt(sendAt)
			.clubId(clubId)
			.meetupId(meetupId)
			.isRead(false)
			.build();
	}
}
