import apiRequest from 'apis/axios'

// 일정 상세 조회
export function getMeetupDetail(clubId: number, meetupId: number) {
  return apiRequest.get(`clubs/${clubId}/meetups/${meetupId}/detail`)
}
