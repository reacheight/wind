import React from 'react';

import styles from '../styles/Analysis.module.css'
import {Accordion, AccordionButton, AccordionIcon, AccordionItem, AccordionPanel} from "@chakra-ui/react";
import Micro from "./Micro";
import Macro from "./Macro";
import { AnalysisResult } from "../models/AnalysisResult";
import TeamIcons from "./TeamIcons";

type AnalysisProps = {
  analysisResult: AnalysisResult | undefined;
}

const AnalysisComponent = ({ analysisResult }: AnalysisProps) => {
  if (!analysisResult || Object.keys(analysisResult).length === 0) {
    return <div/>
  }

  const analysis = analysisResult.analysis

  return (
    <div className={styles.analysis}>
      <TeamIcons radiant={analysisResult.matchInfo.radiant} dire={analysisResult.matchInfo.dire} />
      <Accordion allowMultiple pl="20px" pr="20px">
        <AccordionItem className={styles.accordionItem}>
          <AccordionButton className={styles.accordionButton} fontSize={20}>
            <span>Micro</span>
            <AccordionIcon />
          </AccordionButton>
          <AccordionPanel>
            <Micro analysis={analysis} />
          </AccordionPanel>
        </AccordionItem>
        <AccordionItem className={styles.accordionItem}>
          <AccordionButton className={styles.accordionButton} fontSize={20}>
            Macro
            <AccordionIcon />
          </AccordionButton>
          <AccordionPanel>
            <Macro analysis={analysis} />
          </AccordionPanel>
        </AccordionItem>
        {/*<AccordionItem className={styles.accordionItem}>*/}
        {/*  <AccordionButton className={styles.accordionButton} fontSize={20}>*/}
        {/*    <span>Item timings (statistics provided by <a className={styles.spectralLink} target="_blank" href="https://stats.spectral.gg/lrg2/?league=imm_ranked_731&mod=items-icritical">Spectral.gg</a>)</span>*/}
        {/*    <AccordionIcon />*/}
        {/*  </AccordionButton>*/}
        {/*  <AccordionPanel>*/}
        {/*    <ul className={styles.list}>*/}
        {/*      {itemTimings}*/}
        {/*    </ul>*/}
        {/*  </AccordionPanel>*/}
        {/*</AccordionItem>*/}
      </Accordion>
    </div>
  )
}

export default AnalysisComponent