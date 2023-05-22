package org.lightnsalt.hikingdom.service.notification.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.lightnsalt.hikingdom.domain.common.enumType.NotificationType;
import org.lightnsalt.hikingdom.domain.entity.club.ClubMember;
import org.lightnsalt.hikingdom.domain.entity.member.Member;
import org.lightnsalt.hikingdom.service.notification.dto.event.CreateMeetupNotificationEvent;
import org.lightnsalt.hikingdom.service.notification.dto.request.NotificationAddReq;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@Async("createMeetupNotification")
@Transactional
@RequiredArgsConstructor
public class CreateMeetupNotificationEventListener {
    private final NotificationService notificationService;

    @EventListener
    public void handleCreateMeetupNotificationEvent(CreateMeetupNotificationEvent event) {
        final List<ClubMember> clubMemberList = event.getClubMemberList();
        final Member host = event.getHost();
        String meetupDate = event.getStartAt().format(DateTimeFormatter.ofPattern("MM월 dd일"));
        String url = "/club/meetup/" + event.getMeetupId() + "/detail";

        createMeetupHostNotification(host, event.getClubId(), event.getMeetupId(), meetupDate, url);     // 일정을 생성한 호스트에게 생성이 완료되었다는 것을 알림 (알림 DB insert, fcm 푸시알림 전송)
        createMeetupClubMembersNotification(clubMemberList, host, event.getClubId(), event.getMeetupId(), meetupDate, url);  // 일정을 생성한 호스트가 속한 클럽의 멤버들에게 생성이 완료되었다는 것을 알림 (알림 DB insert, fcm 푸시알림 전송)
    }

    private void createMeetupHostNotification(Member host, Long clubId, Long meetupId, String meetupDate, String url){    // 일정을 생성한 호스트에게 생성이 완료되었다는 것을 알림 (알림 DB insert, fcm 푸시알림 전송)
        NotificationAddReq notificationAddReq = NotificationAddReq.builder()
                .member(host)
                .category(NotificationType.NEW_MEETUP)
                .title(meetupDate + " 일정이 생성되었습니다.")
                .body("새로운 일정이 생성되었습니다.")
                .sendAt(LocalDateTime.now())
                .url(url)
                .clubId(clubId)
                .meetupId(meetupId)
                .build();
        notificationService.addNotification(notificationAddReq);
    }

    private void createMeetupClubMembersNotification(List<ClubMember> clubMemberList, Member host, Long clubId, Long meetupId,
        String meetupDate, String url){ // 일정을 생성한 호스트가 속한 클럽의 멤버들에게 생성이 완료되었다는 것을 알림 (알림 DB insert, fcm 푸시알림 전송)
        NotificationAddReq notificationAddReq = NotificationAddReq.builder()
                .category(NotificationType.NEW_MEETUP)
                .title("일정이 추가되었습니다. 참여해보세요!")
                .body(host.getNickname() + "님이 "+ meetupDate +" 새로운 일정을 추가하셨습니다.")
                .sendAt(LocalDateTime.now())
                .url(url)
                .clubId(clubId)
                .meetupId(meetupId)
                .build();

        clubMemberList
                .stream()
                .filter(cm -> !cm.getMember().getId().equals(host.getId())) // 일정 생성자 제외
                .forEach(cm -> {
                    notificationAddReq.setMember(cm.getMember());
                    notificationService.addNotification(notificationAddReq);
                });
    }
}
