import React, { useContext } from 'react'
import { ThemeContext } from 'styles/ThemeProvider'
import styles from './PwUpdateForm.module.scss'
import { useNavigate } from 'react-router-dom'

import LabelInput from 'components/common/LabelInput'
import Button from 'components/common/Button'

import useAuthInput from 'hooks/useAuthInput'
import useCheckPw from 'hooks/useCheckPw'

import { updatePw } from 'apis/services/users'
import toast from 'components/common/Toast'

function PwUpdateForm() {
  const { theme } = useContext(ThemeContext)
  const navigate = useNavigate()

  // 비밀번호 변경
  const {
    value: password,
    onChange: changePw,
    isPass: isPwPass,
    condition: pwCond,
  } = useAuthInput({ type: 'password' })

  const {
    value: newPassword,
    onChange: changeNewPw,
    isPass: isNewPwPass,
    condition: newPwCond,
  } = useAuthInput({ type: 'password' })

  const {
    value: checkPassword,
    onChange: changeCheckPw,
    isPass: isCheckPwPass,
  } = useCheckPw({ password })

  // 비밀번호 변경 함수
  const onClickUpdatePw = () => {
    updatePw(password, newPassword, checkPassword)
      .then(() => {
        toast.addMessage('success', '비밀번호가 변경되었습니다.')
        navigate('/profile')
      })
      .catch((err) => {
        if (err.status === 401) {
          toast.addMessage('error', '비밀번호가 일치하지 않습니다.')
        }
      })
  }

  return (
    <div className={`content ${theme} ${styles.password}`}>
      <LabelInput
        label="현재 비밀번호"
        value={password}
        onChange={changePw}
        isPass={isPwPass}
        placeholder={pwCond}
        type="password"
      />
      <LabelInput
        label="새 비밀번호"
        value={newPassword}
        onChange={changeNewPw}
        isPass={isNewPwPass}
        placeholder={newPwCond}
        type="password"
      />
      <LabelInput
        label="새 비밀번호 확인"
        value={checkPassword}
        onChange={changeCheckPw}
        isPass={isCheckPwPass}
        isError={newPassword !== checkPassword}
        placeholder="새 비밀번호를 확인해주세요"
        type="password"
      />
      <Button
        text="비밀번호 수정"
        color={isPwPass && isNewPwPass && isCheckPwPass ? 'primary' : 'gray'}
        onClick={onClickUpdatePw}
      />
    </div>
  )
}

export default PwUpdateForm
