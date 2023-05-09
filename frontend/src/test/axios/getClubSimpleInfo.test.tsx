import apiRequest from 'apis/axios'
import { getClubSimpleInfo } from 'apis/services/clubs'

jest.mock('apis/axios', () => ({
  get: jest.fn(),
}))

describe('getClubSimpleInfo', () => {
  afterEach(() => {
    jest.clearAllMocks() // mock 함수 초기화
  })

  it('getClubSimpleInfo test', async () => {
    const clubId = 1
    const responseData = { hostId: 1, clubId: 1, clubName: '산타마리오' }
    const expectedUrl = `clubs/${clubId}`

    ;(apiRequest.get as jest.Mock).mockResolvedValue({
      data: { result: responseData },
    })

    const response = await getClubSimpleInfo(clubId)
    expect(apiRequest.get).toHaveBeenCalledWith(expectedUrl)
    expect(response.data.result).toEqual(responseData) // 타입검사
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
