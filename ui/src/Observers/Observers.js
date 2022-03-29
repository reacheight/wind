import React from 'react';
import styles from '../Analysis/Analysis.module.css'
import '../items.css'

const Observers = (props) => {
  const obsInfo = props.observers
    .map((entry) =>
      <li key={'observer' + entry[0]}>
        <span className={styles.glowing}>{props.heroes[entry[1]]}</span> at {entry[0]}
      </li>
    )
    
  return (
    <>
      <h5 className={styles.analysisTitle}><span className='observer'>Observers</span> placed on enemy vision 👀</h5>
      <ul>{obsInfo}</ul>
    </>
  )
}

export default Observers