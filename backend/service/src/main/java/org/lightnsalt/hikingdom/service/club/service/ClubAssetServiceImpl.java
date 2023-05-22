package org.lightnsalt.hikingdom.service.club.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.lightnsalt.hikingdom.common.error.ErrorCode;
import org.lightnsalt.hikingdom.common.error.GlobalException;
import org.lightnsalt.hikingdom.domain.entity.club.Club;
import org.lightnsalt.hikingdom.domain.entity.club.ClubAsset;
import org.lightnsalt.hikingdom.domain.entity.info.AssetInfo;
import org.lightnsalt.hikingdom.service.club.dto.response.ClubAssetRes;
import org.lightnsalt.hikingdom.service.club.repository.ClubAssetRepository;
import org.lightnsalt.hikingdom.service.club.repository.ClubRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClubAssetServiceImpl implements ClubAssetService {
	private final ClubRepository clubRepository;
	private final ClubAssetRepository clubAssetRepository;
	@Value("${values.asset.defaultAsset}")
	private String defaultAssetUrl;
	private AssetInfo defaultAsset;

	@PostConstruct
	private void postConstruct() {
		defaultAsset = new AssetInfo(null, "기본 에셋", defaultAssetUrl, "기본 에셋", 0);
	}

	@Override
	public List<ClubAssetRes> findClubMountainList(Long clubId) {
		final Club club = clubRepository.findById(clubId)
			.orElseThrow(() -> new GlobalException(ErrorCode.CLUB_NOT_FOUND));
		List<ClubAsset> clubAssetList = clubAssetRepository.findAllByClubId(club.getId());

		// 기본 땅 에셋 추가하기
		ClubAsset defaultClubAsset = ClubAsset.builder()
			.club(club)
			.asset(defaultAsset)
			.meetup(null)
			.rowIndex(0)
			.colIndex(0)
			.build();
		clubAssetList.add(0, defaultClubAsset);

		return clubAssetList.stream()
			.map(ClubAssetRes::new).collect(Collectors.toList());
	}
}
