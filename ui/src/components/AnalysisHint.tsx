import {Accordion, AccordionButton, AccordionIcon, AccordionItem, AccordionPanel, Box} from "@chakra-ui/react";
import styles from "../styles/Analysis.module.css";
import React from "react";

type AnalysisHintProps = {
  hint: string;
}

const AnalysisHint = ({ hint }: AnalysisHintProps) => {
  return <Accordion allowToggle textAlign={"center"} marginInline={"10%"}>
    <AccordionItem className={styles.accordionItem} backgroundColor={"dimgray"} textColor={"gray.100"}>
      <AccordionButton className={styles.accordionButton} fontSize={15}>
        <span>What does it mean?</span>
        <AccordionIcon />
      </AccordionButton>
      <AccordionPanel>
        <Box border={"none"} borderRadius={10}>
          {hint}
        </Box>
      </AccordionPanel>
    </AccordionItem>
  </Accordion>
}

export default AnalysisHint