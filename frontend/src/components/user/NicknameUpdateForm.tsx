import React, { useContext } from 'react'
import { useNavigate } from 'react-router-dom'
import { ThemeContext } from 'styles/ThemeProvider'
import styles from './NicknameUpdateForm.module.scss'

import LabelInput from 'components/common/LabelInput'
import Button from 'components/common/Button'

import useAuthInput from 'hooks/useAuthInput'

import { updateNickname } from 'apis/services/users'
import toast from 'components/common/Toast'

function NicknameUpdateForm() {
  const { theme } = useContext(ThemeContext)
  const navigate = useNavigate()

  // 닉네임 수정
  const {
    value: nickname,
    onChange: changeNickname,
    isPass: isNicknamePass,
    condition: nicknameCond,
  } = useAuthInput({ type: 'nickname' })

  // 닉네임 수정 함수
  const onClickUpdateNickname = () => {
    updateNickname(nickname)
      .then(() => {
        toast.addMessage('success', '닉네임이 변경되었습니다.')
        navigate('/profile')
        // Todo: Query invalidate 처리
      })
      .catch((err) => {
        if (err.status === 400) {
          toast.addMessage('error', err.data.message)
        }
      })
  }

  return (
    <div className={`content ${theme} ${styles.nickname}`}>
      <Button
        text="취소"
        color="secondary"
        size="sm"
        onClick={() => navigate('/profile')}
      />

      <LabelInput
        label="닉네임"
        value={nickname}
        onChange={changeNickname}
        placeholder={nicknameCond}
      />
      <Button
        text="닉네임 수정"
        color={isNicknamePass ? 'primary' : 'gray'}
        onClick={onClickUpdateNickname}
      />
    </div>
  )
}

export default NicknameUpdateForm
