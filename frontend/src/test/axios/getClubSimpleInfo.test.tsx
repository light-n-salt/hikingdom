import apiRequest from 'apis/axios'
import { getClubSimpleInfo } from 'apis/services/clubs'

jest.mock('apis/axios', () => ({
  get: jest.fn(),   // Mock function 생성
}))

// 테스트 코드를 그룹화하고, 묶어주는 역할
describe('getClubSimpleInfo', () => {
  // 각 실행들이 끝날 때마다
  afterEach(() => {
    jest.clearAllMocks() // mock 함수 초기화
  })

  // test === it
  it('getClubSimpleInfo test', async () => {
    const clubId = 1
    const responseData = { hostId: 1, clubId: 1, clubName: '산타마리오' }
    const expectedUrl = `clubs/${clubId}`

    // 예상한 값으로 변경
    ;(apiRequest.get as jest.Mock).mockResolvedValue({
      data: { result: responseData },
    })

    // 실행
    const response = await getClubSimpleInfo(clubId)
    
    // toHaveBeenCalledWith: 특정 매개변수와 호출되는지 확인
    expect(apiRequest.get).toHaveBeenCalledWith(expectedUrl)
    expect(response.data.result).toStrictEqual(responseData) // 타입검사
    expect(response.data.result.hostId).toEqual(responseData.hostId)
    expect(response.data.result.clubId).toEqual(responseData.clubId)
    expect(response.data.result.clubName).toEqual(responseData.clubName)
  })

  // it('should throw an error if the request fails', async () => {
  //   const clubId = 1;
  //   const expectedError = new Error('Network Error');
  //   const expectedUrl = `clubs/${clubId}`;
  //   (apiRequest.get as jest.Mock).mockRejectedValue(expectedError);

  //   await expect(getClubSimpleInfo(clubId)).rejects.toThrow(expectedError);
  //   expect(apiRequest.get).toHaveBeenCalledWith(expectedUrl);
  // });
})
