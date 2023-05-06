import apiRequest from 'apis/axios'

// GET Request
export function checkNickname(nickname: string) {
  return apiRequest.get(`/members/auth/nickname-check/${nickname}`)
}

// POST Request
export function validEmail(email: string) {
  return apiRequest.post(`/members/auth/email-valid`, {
    email,
  })
}

export function signup(
  email: string,
  nickname: string,
  password: string,
  checkPassword: string
) {
  return apiRequest.post(`/members/auth/signup`, {
    email,
    nickname,
    password,
    checkPassword,
  })
}

export function login(email: string, password: string) {
  return apiRequest
    .post(`/members/auth/login`, {
      email,
      password,
    })
    .then((res) => {
      localStorage.setItem('accessToken', res.data.result.accessToken)
      localStorage.setItem('refreshToken', res.data.result.accessToken)
    })
}

export function logout() {
  return apiRequest.post(`/members/logout`).then(() => {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  })
}

// PUT request
export function findPw(email: string) {
  return apiRequest.put(`/members/auth/password-find`, {
    email,
  })
}

export function updateNickname(nickname: string) {
  return apiRequest.put(`/members/nickname-change`, {
    nickname,
  })
}

export function updatePw(
  password: string,
  newPassword: string,
  checkPassword: string
) {
  return apiRequest.put(`/members/password-change`, {
    password,
    newPassword,
    checkPassword,
  })
}

// DELETE Request
export function confirmEmail(email: string, authCode: string) {
  return apiRequest.delete(`/members/auth/email-valid`, {
    data: {
      email,
      authCode,
    },
  })
}
