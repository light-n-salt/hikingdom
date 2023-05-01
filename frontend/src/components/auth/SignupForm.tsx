import React from 'react'
import LabelInput from 'components/common/LabelInput'
import useAuthInput from 'hooks/useAuthInput'
import useCheckPw from 'hooks/useCheckPw'
import styles from './SignupForm.module.scss'
import Button from 'components/common/Button'

function SignupForm() {
    const {
        value: email,
        onChange: changeEmail,
        isPass: isEmailPass,
    } = useAuthInput({ type: 'email' })
    const {
        value: nickname,
        onChange: changeNickname,
        isPass: isNicknamePass,
    } = useAuthInput({ type: 'nickname' })
    const {
        value: password,
        onChange: changePw,
        isPass: isPwPass,
    } = useAuthInput({ type: 'password' })
    const {
        value: checkPassword,
        onChange: changeCheckPw,
        isPass: isCheckPwPass,
    } = useCheckPw({ password })

    return (
        <div className={styles['signup-form']}>
            <LabelInput
                label="이메일"
                value={email}
                onChange={changeEmail}
                isPass={isEmailPass}
                placeholder="이메일을 입력해주세요"
            />
            <LabelInput
                label="닉네임"
                value={nickname}
                onChange={changeNickname}
                isPass={isNicknamePass}
                placeholder="닉네임을 입력해주세요"
            />
            <LabelInput
                label="비밀번호"
                value={password}
                onChange={changePw}
                isPass={isPwPass}
                placeholder="비밀번호을 입력해주세요"
                type="password"
            />
            <LabelInput
                label="비밀번호 확인"
                value={checkPassword}
                onChange={changeCheckPw}
                isPass={isCheckPwPass}
                placeholder="비밀번호을 확인해주세요"
                type="password"
            />
            <Button text="회원가입" color="primary" onClick={() => {}} />
        </div>
    )
}

export default SignupForm
