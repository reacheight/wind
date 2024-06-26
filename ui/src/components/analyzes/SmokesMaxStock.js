import React from 'react';
import styles from '../../styles/Analysis.module.css'
import '../../items.css'

const SmokesMaxStock = (props) =>
  <>
    <h5 className={styles.analysisTitle}><span className='smoke'>Smokes</span> max stock duration</h5>
    <ul>
      <li key={'smokesMaxStockRadiant'}>
        <span className={styles.green}>Radiant</span> — {props.smokesMaxStock[0]} sec
      </li>
      <li key={'smokesMaxStockDire'}>
        <span className={styles.red}>Dire</span> — {props.smokesMaxStock[1]} sec
      </li>
    </ul>
  </>

export default SmokesMaxStock