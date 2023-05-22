package org.lightnsalt.hikingdom.service.notification.repository;

import static org.lightnsalt.hikingdom.domain.entity.notification.QNotification.*;

import java.util.List;

import org.lightnsalt.hikingdom.domain.entity.notification.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public Slice<Notification> findNotificationsByMemberId(Long memberId, Long notificationId, Pageable pageable) {
        List<Notification> result = queryFactory.selectFrom(notification)
                .where(
                        isLast(notificationId),
                        notification.member.id.eq(memberId))
                .orderBy(notification.sendAt.desc())
                .limit(pageable.getPageSize() + 1L)
                .fetch();
        return checkLastPage(pageable, result);
    }

    // no-offset 방식 처리하는 메서드
    private BooleanExpression isLast(Long notificationId) {
        if (notificationId == null) {
            return null;
        }
        return notification.id.lt(notificationId);
    }

    private Slice<Notification> checkLastPage(Pageable pageable, List<Notification> result) {
        boolean hasNext = result.size() > pageable.getPageSize();

        // 조회한 결과 개수가 요청한 페이지 사이즈보다 크면 뒤에 더 있는 것, next = true
        if (hasNext) {
            result.remove(pageable.getPageSize());
        }
        return new SliceImpl<>(result, pageable, hasNext);
    }
}
