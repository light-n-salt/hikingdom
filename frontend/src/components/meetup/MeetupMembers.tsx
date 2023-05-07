import React, { useState } from 'react'
import styles from './MeetupMembers.module.scss'

import Image from 'components/common/Image'
import Button from 'components/common/Button'
import Modal from 'components/common/Modal'
import MemberModal from './MemberModal'
import { MeetupMember } from 'types/meetup.interface'

type MeetupMembersProps = {
  memberInfo: MeetupMember[]
}

function MeetupMembers({ memberInfo }: MeetupMembersProps) {
  const [isOpen, setIsOpen] = useState(false)

  return (
    <>
      {isOpen && (
        <Modal onClick={() => setIsOpen(false)}>
          <MemberModal />
        </Modal>
      )}
      <div className={styles.meetup}>
        <div className={styles.titles}>
          <span className={styles.title}>
            참여 멤버 ({memberInfo.length}명)
          </span>
          <Button text="참여" color="primary" size="xs" />
        </div>
        <div className={styles.members}>
          {memberInfo.map((member) => (
            <Image
              key={member.memberId}
              imgUrl={member.profileUrl}
              size="sm"
              isSquare={true}
            />
          ))}
          <div className={styles.more} onClick={() => setIsOpen(true)}>
            +
          </div>
        </div>
      </div>
    </>
  )
}

export default MeetupMembers
