package org.lightnsalt.hikingdom.service.info.dto.response;

import java.util.List;
import java.util.stream.Collectors;

import org.lightnsalt.hikingdom.domain.entity.info.MountainInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MountainDetailRes {
	private Long mountainId;
	private String name;
	private String description;
	private String address;
	private String imgUrl;
	private double maxAlt;
	private double topLat;
	private double topLng;
	private String peaks;
	private String checkPeak;
	private int totalDuration;
	private List<AssetMountainRes> assets;

	public MountainDetailRes(final MountainInfo mountainInfo) {
		this.mountainId = mountainInfo.getId();
		this.name = mountainInfo.getName();
		this.description = mountainInfo.getDescription();
		this.address = mountainInfo.getAddress();
		this.imgUrl = mountainInfo.getImgUrl();
		this.maxAlt = mountainInfo.getTopAlt();
		this.topLat = mountainInfo.getTopLat();
		this.topLng = mountainInfo.getTopLng();
		this.peaks = mountainInfo.getPeaks();
		this.checkPeak = mountainInfo.getCheckPeak();
		this.totalDuration = mountainInfo.getTotalDuration();
		this.assets = mountainInfo.getAsset().stream().map(AssetMountainRes::new).collect(Collectors.toList());
		this.totalDuration = mountainInfo.getTotalDuration();
	}
}
