import React from 'react'
import styles from '../../styles/Analysis.module.css'
import '../../items.css'

const Smokes = (props) => {
  const smokeInfo = props.smokes
    .map((entry) =>
      <li key={'smoke' + entry[0]}>
        <span className={styles.glowing}>{props.heroes[entry[1]]}</span> at {entry[0]}
      </li>
    )

  return (
    <>
      <h5 className={styles.analysisTitle}><span className='smoke'>Smokes</span> used on enemy vision 💨</h5>
      <ul>{smokeInfo}</ul>
    </>
  )
}

export default Smokes