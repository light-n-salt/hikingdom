import React, { useContext, useEffect, useState } from 'react'
import { ThemeContext } from 'styles/ThemeProvider'
import styles from './MeetupDetailPage.module.scss'

import Button from 'components/common/Button'
import PageHeader from 'components/common/PageHeader'
import MeetupDetail from 'components/meetup/MeetupDetail'
import MeetupIntroduction from 'components/meetup/MeetupIntroduction'
import MeetupMembers from 'components/meetup/MeetupMembers'
import MeetupAlbum from 'components/meetup/MeetupAlbum'
import MeetupReviewList from 'components/meetup/MeetupReviewList'
import TextSendBar from 'components/common/TextSendBar'

import { meetupInfoDetail } from 'types/meetup.interface'

import { getMeetupDetail } from 'apis/services/meetup'

function MeetupDetailPage() {
  const { theme } = useContext(ThemeContext)
  const [meetup, setMeetup] = useState<meetupInfoDetail>()

  useEffect(() => {
    getMeetupDetail(1, 1)
      .then((res) => {
        console.log(res.data.result)
        setMeetup(res.data.result)
      })
      .catch((err) => console.log(err))
  }, [])

  return meetup ? (
    <div className={`page p-sm ${theme} ${styles.page}`}>
      <Button text="수정" color="secondary" size="xs" />
      <PageHeader
        title={meetup?.meetupName}
        url="/club/meetup"
        color="primary"
      />
      <MeetupDetail
        mountain={meetup?.mountainName}
        date={meetup?.startAt.split('T')[0]}
        time={meetup?.startAt?.split('T')[1]}
      />
      <div className={`page ${theme} ${styles.content}`}>
        <div className={styles.intro}>
          <MeetupIntroduction content={meetup?.description} />
        </div>
        <MeetupMembers memberInfo={meetup?.memberInfo} />
        <MeetupAlbum photoInfo={meetup?.photoInfo} />
        <MeetupReviewList reviewInfo={meetup?.reviewInfo} />
      </div>
      <TextSendBar placeholder="후기를 입력해주세요" />
    </div>
  ) : null
}

export default MeetupDetailPage
