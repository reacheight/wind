import React from 'react';
import styles from '../../styles/Analysis.module.css'
import '../../items.css'
import { ObserverOnVision } from "../../models/ObserverOnVision";
import { Heroes } from "../../constants/heroes";

type ObserversOnVisionProps = {
  observersOnVision: ReadonlyArray<ObserverOnVision>;
}

const Observers = ({ observersOnVision }: ObserversOnVisionProps) => {
  const observers = observersOnVision
    .map((observer) =>
      <li key={observer.time}>
        <span className={styles.glowing}>{Heroes[observer.hero]}</span> at {observer.time}
      </li>
    )
    
  return (
    <>
      <h5 className={styles.analysisTitle}><span className='observer'>Observers</span> placed on enemy vision 👀</h5>
      <ul>{observers}</ul>
    </>
  )
}

export default Observers