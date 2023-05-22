package org.lightnsalt.hikingdom.domain.entity.info;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "mountain_info")
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MountainInfo {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "BIGINT UNSIGNED")
	private Long id;

	@Column(length = 20, nullable = false)
	private String name;

	@Column(length = 1024, nullable = false)
	private String description;

	@Column(length = 200, nullable = false)
	private String address;

	@Column(name = "top_alt", nullable = false)
	private double topAlt;

	@Column(name = "top_lat", nullable = false)
	private double topLat;

	@Column(name = "top_lng", nullable = false)
	private double topLng;

	@Column(name = "total_duration", columnDefinition = "int unsigned", nullable = false)
	private int totalDuration;

	@Column(name = "img_url", length = 512)
	private String imgUrl;

	@Column(name = "peaks", length = 128)
	private String peaks;

	@Column(name = "check_peak", length = 50)
	private String checkPeak;

	@OneToMany(mappedBy = "mountain")
	@ToString.Exclude
	private List<AssetInfo> asset;

	public void setAsset(List<AssetInfo> asset) {
		this.asset = asset;
	}

	@Builder
	public MountainInfo(String name, String description, String address, double topAlt, double topLat, double topLng,
		int totalDuration, String imgUrl, String peaks, List<AssetInfo> asset) {
		this.name = name;
		this.description = description;
		this.address = address;
		this.topAlt = topAlt;
		this.topLat = topLat;
		this.topLng = topLng;
		this.totalDuration = totalDuration;
		this.imgUrl = imgUrl;
		this.peaks = peaks;
		this.asset = asset;
	}
}
