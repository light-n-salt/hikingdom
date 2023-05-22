package org.lightnsalt.hikingdom.service.club.dto.response;

import org.lightnsalt.hikingdom.domain.entity.club.ClubAsset;

import lombok.Data;

@Data
public class ClubAssetRes {
	private String mountainName;
	private Long meetupId;
	private String assetUrl;
	private double row;
	private double column;

	public ClubAssetRes(ClubAsset clubAsset) {
		if (clubAsset.getMeetup() == null) { // 기본 에셋 변환할 때 Null Check
			this.mountainName = null;
			this.meetupId = null;
		} else {
			this.mountainName = clubAsset.getMeetup().getMountain().getName();
			this.meetupId = clubAsset.getMeetup().getId();
		}

		this.assetUrl = clubAsset.getAsset().getAssetUrl();
		this.row = clubAsset.getRowIndex();
		this.column = clubAsset.getColIndex();
	}
}
