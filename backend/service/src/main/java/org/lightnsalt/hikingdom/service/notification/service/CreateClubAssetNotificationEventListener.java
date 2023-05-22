package org.lightnsalt.hikingdom.service.notification.service;

import java.time.LocalDateTime;
import java.util.List;

import org.lightnsalt.hikingdom.domain.common.enumType.NotificationType;
import org.lightnsalt.hikingdom.domain.entity.club.ClubMember;
import org.lightnsalt.hikingdom.domain.entity.info.AssetInfo;
import org.lightnsalt.hikingdom.service.notification.dto.event.CreateClubAssetNotificationEvent;
import org.lightnsalt.hikingdom.service.notification.dto.request.NotificationAddReq;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@Async("createClubAssetNotification")
@Transactional
@RequiredArgsConstructor
public class CreateClubAssetNotificationEventListener {
    private final NotificationService notificationService;

    @EventListener
    public void handleCreateClubAssetNotificationEvent(CreateClubAssetNotificationEvent event) {
        final List<ClubMember> clubMemberList = event.getClubMemberList();
        final AssetInfo assetInfo = event.getAssetInfo();
        String url = "/club/main";

        createClubAssetNotification(clubMemberList, assetInfo, event.getClubId(), url);  // 일정을 생성한 호스트가 속한 클럽의 멤버들에게 생성이 완료되었다는 것을 알림 (알림 DB insert, fcm 푸시알림 전송)
    }

    private void createClubAssetNotification(List<ClubMember> clubMemberList, AssetInfo assetInfo, Long clubId, String url){ // 소모임 일정 중 한명이라도 산을 완등하여 소모임 에셋 생성이 완료되었다는 것을 알림 (알림 DB insert, fcm 푸시알림 전송)
        NotificationAddReq notificationAddReq = NotificationAddReq.builder()
            .category(NotificationType.NEW_ASSET)
            .title("에셋을 획득하셨습니다!")
            .body("소속 소모임이 ["+assetInfo.getName() + "]을 획득했습니다! 확인해보세요")
            .sendAt(LocalDateTime.now())
            .clubId(clubId)
            .url(url)
            .build();

        clubMemberList
            .stream()
            .forEach(cm -> {
                notificationAddReq.setMember(cm.getMember());
                notificationService.addNotification(notificationAddReq);
            });
    }
}
