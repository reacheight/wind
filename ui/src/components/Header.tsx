import React from 'react';
import styles from '../styles/Header.module.css'

const Header = () =>
  <div className={styles.header}>
    <span className={styles.title}>windota</span> <span className={styles.description}>Dota 2 replay analysis tool</span>
  </div>

export default Header