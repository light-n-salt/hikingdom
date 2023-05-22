package org.lightnsalt.hikingdom.domain.entity.notification;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.lightnsalt.hikingdom.domain.common.BaseTimeEntity;
import org.lightnsalt.hikingdom.domain.common.enumType.NotificationType;
import org.lightnsalt.hikingdom.domain.entity.member.Member;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "notification")
@Getter
@ToString
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "BIGINT UNSIGNED")
	private Long id;

	@ToString.Exclude
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private Member member;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private NotificationType category;

	@Column(nullable = false, length = 50)
	private String title;

	@Column(nullable = false, length = 100)
	private String body;

	@Column(name = "send_at")
	private LocalDateTime sendAt;

	@Column(name = "is_read", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
	private boolean isRead;

	@Column(length = 512)
	private String url;

	@Column(name = "club_id", columnDefinition = "BIGINT UNSIGNED DEFAULT NULL")
	private Long clubId;

	@Column(name = "meetup_id", columnDefinition = "BIGINT UNSIGNED DEFAULT NULL")
	private Long meetupId;

	@Builder
	public Notification(Member member, NotificationType category, String title, String body, LocalDateTime sendAt,
		boolean isRead, String url, Long clubId, Long meetupId) {
		this.member = member;
		this.category = category;
		this.title = title;
		this.body = body;
		this.sendAt = sendAt;
		this.isRead = isRead;
		this.url = url;
		this.clubId = clubId;
		this.meetupId = meetupId;
	}
}
