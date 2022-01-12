import React from 'react';
import styles from '../Analysis/Analysis.module.css'
import '../items.css'

const SmokesMaxStock = (props) =>
  <>
    <h5 className={styles.analysisTitle}><span className='smoke'>Smokes</span> max stock duration</h5>
    <ul>
      <li key={'smokesMaxStockRadiant'}>
        <span className={styles.green}>Radiant</span> — {props.smokesMaxStock.Radiant} sec
      </li>
      <li key={'smokesMaxStockDire'}>
        <span className={styles.red}>Dire</span> — {props.smokesMaxStock.Dire} sec
      </li>
    </ul>
  </>

export default SmokesMaxStock