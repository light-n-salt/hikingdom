import React, { useEffect } from 'react'

import styles from './MemberModal.module.scss'
import MemberList from 'components/club/MemberList'
import { ClubMember } from 'types/club.interface'

//Todo: 참여멤버 API 수정 필요

const clubMemberList: ClubMember[] = [
  {
    memberId: 1,
    nickname: '정예지렁이비가오네?',
    profileUrl:
      'https://i.namu.wiki/i/ZeGGZBUYHmppb5RTym8vJmnQulv-UhJygvrUMH_qXurQ08oZzRshu2sToDMet1NFeq4iwnkqY69Y_pKH4C2lmg.webp',
    level: 1,
    totalHikingCount: 3,
    totalDuration: 800,
    totalDistance: 730,
  },
  {
    memberId: 2,
    nickname: '이병호루라기',
    profileUrl:
      'https://i.namu.wiki/i/ZeGGZBUYHmppb5RTym8vJmnQulv-UhJygvrUMH_qXurQ08oZzRshu2sToDMet1NFeq4iwnkqY69Y_pKH4C2lmg.webp',
    level: 2,
    totalHikingCount: 3,
    totalDuration: 800,
    totalDistance: 730,
  },
  {
    memberId: 3,
    nickname: '조혜진',
    profileUrl:
      'https://i.namu.wiki/i/ZeGGZBUYHmppb5RTym8vJmnQulv-UhJygvrUMH_qXurQ08oZzRshu2sToDMet1NFeq4iwnkqY69Y_pKH4C2lmg.webp',
    level: 3,
    totalHikingCount: 3,
    totalDuration: 800,
    totalDistance: 730,
  },
  {
    memberId: 1,
    nickname: '정예지렁이비가오네?',
    profileUrl:
      'https://i.namu.wiki/i/ZeGGZBUYHmppb5RTym8vJmnQulv-UhJygvrUMH_qXurQ08oZzRshu2sToDMet1NFeq4iwnkqY69Y_pKH4C2lmg.webp',
    level: 1,
    totalHikingCount: 3,
    totalDuration: 800,
    totalDistance: 730,
  },
  {
    memberId: 2,
    nickname: '이병호루라기',
    profileUrl:
      'https://i.namu.wiki/i/ZeGGZBUYHmppb5RTym8vJmnQulv-UhJygvrUMH_qXurQ08oZzRshu2sToDMet1NFeq4iwnkqY69Y_pKH4C2lmg.webp',
    level: 2,
    totalHikingCount: 3,
    totalDuration: 800,
    totalDistance: 730,
  },
  {
    memberId: 3,
    nickname: '조혜진',
    profileUrl:
      'https://i.namu.wiki/i/ZeGGZBUYHmppb5RTym8vJmnQulv-UhJygvrUMH_qXurQ08oZzRshu2sToDMet1NFeq4iwnkqY69Y_pKH4C2lmg.webp',
    level: 3,
    totalHikingCount: 3,
    totalDuration: 800,
    totalDistance: 730,
  },
]

function MemberModal() {
  return (
    <div className={styles.modal}>
      <MemberList
        title="참여 멤버"
        length={clubMemberList.length}
        memberList={clubMemberList}
      />
    </div>
  )
}

export default MemberModal
