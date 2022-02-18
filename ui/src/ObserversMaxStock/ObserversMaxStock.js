import React from 'react';
import styles from '../Analysis/Analysis.module.css'
import '../items.css'

const ObserversMaxStock = (props) =>
  <>
    <h5 className={styles.analysisTitle}><span className={'observer'}>Observers</span> max stock duration</h5>
    <ul>
      <li key={'observersMaxStockRadiant'}>
        <span className={styles.green}>Radiant</span> — {props.observersMaxStock[0]} sec
      </li>
      <li key={'observersMaxStockDire'}>
        <span className={styles.red}>Dire</span> — {props.observersMaxStock[1]} sec
      </li>
    </ul>
  </>

export default ObserversMaxStock