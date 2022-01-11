import React from "react";
import styles from '../Analysis/Analysis.module.css'
import '../items.css'

export default class ObserversMaxStock extends React.Component {
  render() {
    return (
      <>
        <h5 className={styles.analysisTitle}><span className={"observer"}>Observers</span> max stock duration</h5>
        <ul>
          <li key={"observersMaxStockRadiant"}>
            <span className={styles.green}>Radiant</span> — {this.props.observersMaxStock.Radiant} sec
          </li>
          <li key={"observersMaxStockDire"}>
            <span className={styles.red}>Dire</span> — {this.props.observersMaxStock.Dire} sec
          </li>
        </ul>
      </>
    )
  }
}