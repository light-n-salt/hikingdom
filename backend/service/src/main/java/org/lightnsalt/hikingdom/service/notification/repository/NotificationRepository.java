package org.lightnsalt.hikingdom.service.notification.repository;

import org.lightnsalt.hikingdom.domain.entity.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("UPDATE Notification n SET n.isRead = true WHERE n.member.id = :memberId AND n.isRead = false")
	void markAsReadByMemberId(Long memberId);

	int countAllByMemberIdAndIsRead(Long memberId, boolean isRead);
}
