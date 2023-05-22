package org.lightnsalt.hikingdom.service.notification.dto.event;

import java.util.List;

import org.lightnsalt.hikingdom.domain.entity.club.ClubMember;
import org.lightnsalt.hikingdom.domain.entity.info.AssetInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateClubAssetNotificationEvent {
	private final List<ClubMember> clubMemberList;
	private final AssetInfo assetInfo;
	private Long clubId;
}
