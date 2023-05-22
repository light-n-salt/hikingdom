package org.lightnsalt.hikingdom.service.info.dto.response;

import org.lightnsalt.hikingdom.domain.entity.club.ClubAsset;
import org.lightnsalt.hikingdom.domain.entity.info.AssetInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetInfoRes {
	private String assetUrl;
	private double row;
	private double column;

	public AssetInfoRes(ClubAsset clubAsset) {
		this.assetUrl = clubAsset.getAsset().getAssetUrl();
		this.row = clubAsset.getRowIndex();
		this.column = clubAsset.getColIndex();
	}

	public AssetInfoRes(AssetInfo assetInfo) {
		this.assetUrl = assetInfo.getAssetUrl();
		this.row = 0;
		this.column = 0;
	}
}
