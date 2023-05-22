package org.lightnsalt.hikingdom.service.notification.dto.response;

import java.time.format.DateTimeFormatter;

import org.lightnsalt.hikingdom.domain.common.enumType.NotificationType;
import org.lightnsalt.hikingdom.domain.entity.notification.Notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRes {
	@JsonProperty("notificationId")
	private Long id;
	private NotificationType category;
	private String title;
	private String body;
	private String sendAt;
	@JsonProperty("isRead")
	private boolean isRead;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Long clubId;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Long meetupId;

	public NotificationRes(Notification notification) {
		this.id = notification.getId();
		this.category = notification.getCategory();
		this.title = notification.getTitle();
		this.body = notification.getBody();
		this.sendAt = notification.getSendAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		this.isRead = notification.isRead();
		this.clubId = notification.getClubId();
		this.meetupId = notification.getMeetupId();
	}
}
