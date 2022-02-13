import React from 'react';
import ItemTimings from '../ItemTimings/ItemTimings';
import { formatHeroName} from '../util';

import styles from './Analysis.module.css'
import {Accordion, AccordionButton, AccordionIcon, AccordionItem, AccordionPanel} from "@chakra-ui/react";
import Micro from "../Micro/Micro";
import Macro from "../Macro/Macro";

export default class Analysis extends React.Component {
  render() {
    const analysis = this.props.analysis

    if (!analysis || Object.keys(analysis).length === 0) {
      return <div/>
    }

    const heroes = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9].map((id) => formatHeroName(analysis.heroes[id]))
    const itemTimings = Object.keys(analysis.purchases).map(hero =>
      <li key={"timings" + hero}>
        <ItemTimings hero={hero} purchases={analysis.purchases[hero]} />
      </li>
    )

    return (
      <div className={styles.analysis}>
        <Accordion allowMultiple pl="20px" pr="20px">
          <AccordionItem className={styles.accordionItem}>
            <AccordionButton className={styles.accordionButton}>
              <span>Micro</span>
              <AccordionIcon />
            </AccordionButton>
            <AccordionPanel>
              <Micro analysis={analysis} heroes={heroes} />
            </AccordionPanel>
          </AccordionItem>
          <AccordionItem className={styles.accordionItem}>
            <AccordionButton className={styles.accordionButton}>
              Macro
              <AccordionIcon />
            </AccordionButton>
            <AccordionPanel>
              <Macro analysis={analysis} heroes={heroes} />
            </AccordionPanel>
          </AccordionItem>
          <AccordionItem className={styles.accordionItem}>
            <AccordionButton className={styles.accordionButton}>
              <span>Item timings (statistics provided by <a className={styles.spectralLink} target="_blank" href="https://stats.spectral.gg/lrg2/?league=imm_ranked_730e&mod=items-icritical">Spectral.gg</a>)</span>
              <AccordionIcon />
            </AccordionButton>
            <AccordionPanel>
              <ul className={styles.list}>
                {itemTimings}
              </ul>
            </AccordionPanel>
          </AccordionItem>
        </Accordion>
      </div>
    )
  }
}