import React from 'react';
import styles from '../../styles/Analysis.module.css'
import '../../items.css'
import { getItemClassName } from '../../util';
import { UnusedItem } from "../../models/UnusedItem";
import { Heroes } from "../../constants/heroes";

type UnusedItemsProps = {
  unusedItems: ReadonlyArray<UnusedItem>;
}

const UnusedItems = ({ unusedItems }: UnusedItemsProps) => {
  const unusedItemsList = unusedItems.map(({ user, target, item, time}) => {
    let targetName = <span className={styles.glowing}>{Heroes[target]}</span>
    let userName = <span className={styles.glowing}>{Heroes[target]}</span>
    let itemName = <span className={getItemClassName(item)}>{item}</span>

    if (user === target)
      return <li key={time}>
        {targetName} <span className={styles.gray}>died</span> without using {itemName} at {time}
      </li>

    return <li key={time}>
      {userName} didn't use {itemName} on {targetName} at {time}
    </li>
  })

  return (
    <>
      <h5 className={styles.analysisTitle}>Unused items ⚰️</h5>
      <ul>{unusedItemsList}</ul>
    </>
  )
}

export default UnusedItems