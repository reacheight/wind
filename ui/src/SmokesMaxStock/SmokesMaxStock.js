import React from "react";
import styles from '../Analysis/Analysis.module.css'

export default class SmokesMaxStock extends React.Component {
  render() {
    return (
      <>
        <h5 className={styles.analysisTitle}><span className={styles.smoke}>Smokes</span> max stock duration</h5>
        <ul>
          <li key={"smokesMaxStockRadiant"}>
            <span className={styles.green}>Radiant</span> — {this.props.smokesMaxStock.Radiant} sec
          </li>
          <li key={"smokesMaxStockDire"}>
            <span className={styles.red}>Dire</span> — {this.props.smokesMaxStock.Dire} sec
          </li>
        </ul>
      </>
    )
  }
}