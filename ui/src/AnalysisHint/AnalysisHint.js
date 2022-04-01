import {Accordion, AccordionButton, AccordionIcon, AccordionItem, AccordionPanel, Box} from "@chakra-ui/react";
import styles from "../Analysis/Analysis.module.css";
import React from "react";

const AnalysisHint = ({ hint }) => {
  return <Accordion allowToggle textAlign={"center"} marginInline={"10%"}>
    <AccordionItem className={styles.accordionItem} backgroundColor={"dimgray"} textColor={"gray.100"}>
      <AccordionButton className={styles.accordionButton}>
        <span>What does it mean?</span>
        <AccordionIcon />
      </AccordionButton>
      <AccordionPanel>
        <Box border={"none"} borderRadius={10} padding={3}>
          {hint}
        </Box>
      </AccordionPanel>
    </AccordionItem>
  </Accordion>
}

export default AnalysisHint