import React, { useContext, useState } from 'react'
import styles from './UserProfile.module.scss'
import { ThemeContext } from 'styles/ThemeProvider'
import { useNavigate } from 'react-router-dom'

import Button from 'components/common/Button'
import Image from 'components/common/Image'
import IconButton from 'components/common/IconButton'
import UserInfo from 'components/user/UserInfo'

import LevelModal from 'components/user/LevelModal'
import Modal from 'components/common/Modal'

import mountain from 'assets/images/mountain.png'

import { BiEdit } from 'react-icons/bi'

import { logout } from 'apis/services/users'

// import { UserRecord } from 'types/user.interface'

// type UserProfileProps = {
//     imgUrl: string
//     nickname: string
//     email: string
//     userRecord: UserRecord
// }

export default function UserProfile() {
  const { theme } = useContext(ThemeContext)
  const navigate = useNavigate()
  const [isOpen, setIsOpen] = useState(false)

  // Todo: user API 연동
  const userRecord = {
    totalHikingCount: 10,
    totalMountainCount: 30,
    totalDuration: '30:23',
    totalDistance: 34,
    totalAlt: 34,
  }

  const imgUrl =
    'https://images.unsplash.com/photo-1575936123452-b67c3203c357?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8Mnx8aW1hZ2V8ZW58MHx8MHx8&w=1000&q=80'
  const nickname = '이병호리병'
  const email = '조혜진진자라@gamil.com'

  const onClickLogout = () => {
    logout()
    navigate('/')
  }

  return (
    <div className={styles.profile}>
      {isOpen && (
        <Modal onClick={() => setIsOpen(false)}>
          <LevelModal />
        </Modal>
      )}
      <div className={`content ${theme} ${styles.img}`}>
        <Image size="lg" imgUrl={imgUrl} />
      </div>
      <div className={`content ${theme} ${styles.record}`}>
        <div className={styles.btns}>
          <Button
            text={'로그아웃'}
            size={'sm'}
            color={'secondary'}
            onClick={onClickLogout}
          />

          <IconButton
            icon={<BiEdit />}
            size="sm"
            color="gray"
            onClick={() => navigate('/profile/update')}
          />
        </div>
        <div className={styles.username}>
          {nickname}
          <IconButton
            imgSrc={mountain}
            size={'sm'}
            onClick={() => setIsOpen(true)}
          />
        </div>
        <span className={styles.email}>{email}</span>
        <UserInfo userRecord={userRecord} />
      </div>
    </div>
  )
}
