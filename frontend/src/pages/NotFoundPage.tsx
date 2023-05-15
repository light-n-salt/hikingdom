// import React, { useContext } from 'react'
// import styles from './NotFoundPage.module.scss'
// import { useNavigate } from 'react-router-dom'
// import Button from 'components/common/Button'
// import { ThemeContext } from 'styles/ThemeProvider'
// import { ReactComponent as NotFound } from 'assets/svgs/notFound.svg'

import * as THREE from 'three'
import React, { useRef, useState } from 'react'
import { Canvas, useFrame, useLoader } from '@react-three/fiber'
import styles from './NotFoundPage.module.scss'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader'

function NotFoundPage() {
  // const { theme } = useContext(ThemeContext)
  // const navigate = useNavigate()

  // return (
  //   <div className={`page ${theme} ${styles.page}`}>
  //     <div className={styles.content}>
  //       <NotFound className={styles.star1} />
  //       <NotFound className={styles.star2} />
  //       <NotFound className={styles.star3} />
  //       <NotFound className={styles.star4} />
  //       <p>요청하신 페이지를 찾을 수 없습니다</p>
  //       <Button
  //         text="메인으로"
  //         size="md"
  //         color="secondary"
  //         onClick={() => navigate('/main')}
  //       />
  //     </div>
  //   </div>
  // )

  return (
    <Canvas className={styles.page}>
      <ambientLight />
      <pointLight position={[10, 10, 10]} />
      <Cube position={new THREE.Vector3(-1.2, 0, 0)} />
      <Asset position={new THREE.Vector3(0, -5, -10)} />
    </Canvas>
  )
}

export default NotFoundPage

// Mesh position type
type MeshPositionType = {
  position?: THREE.Vector3
}

function Cube({ position, ...props }: MeshPositionType) {
  const thisRef = useRef<THREE.Mesh>(null)

  const [hover, setHover] = useState(false)
  const [click, setClick] = useState(false)
  // 애니메이션
  useFrame((state, delta) => {
    if (thisRef.current) {
      thisRef.current.rotation.y += delta
    }
  })

  return (
    <mesh
      {...props}
      ref={thisRef}
      position={position}
      scale={click ? 1.5 : 1}
      onClick={() => setClick(!click)}
      onPointerOver={() => setHover(true)}
      onPointerOut={() => setHover(false)}
    >
      <boxGeometry args={[1, 1, 1]} />
      <meshStandardMaterial color={hover ? 'hotpink' : 'orange'} />
    </mesh>
  )
}

function Asset({ position, ...props }: MeshPositionType) {
  const gltf = useLoader(
    GLTFLoader,
    'https://lightnsalt.s3.ap-northeast-2.amazonaws.com/asset/main5.gltf'
  )

  const ref = useRef<THREE.Mesh>(null)

  // 호버, 클릭했을 때 효과
  const [hovered, hover] = useState(false)
  const [clicked, click] = useState(false)

  // useFrame - 애니메이션 효과
  // === (draw -> requestAnimationFrame)

  useFrame((state, delta, frame) => {
    const mesh = gltf.scene
    // mesh.rotation.y += delta
  })
  return (
    <primitive
      object={gltf.scene}
      position={position}
      scale={clicked ? 1.5 : 1}
      onClick={() => click(!clicked)}
      onPointerOver={() => hover(true)}
      onPointerOut={() => hover(false)}
    />
  )
}
