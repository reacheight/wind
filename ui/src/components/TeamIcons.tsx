import { HeroId } from "../models/HeroId";
import { Box, Img, List } from "@chakra-ui/react";
import React from "react";

type TeamIconsProps = {
  radiant: ReadonlyArray<HeroId>,
  dire: ReadonlyArray<HeroId>
}

const TeamIcons = ({ radiant, dire }: TeamIconsProps) => {
  const ICONS_ENDPOINT = `${process.env.REACT_APP_API_ENDPOINT}/icon`

  const getIconsImg = (heroes: ReadonlyArray<HeroId>) => heroes
    .map(heroId => <li key={heroId}><Img src={`${ICONS_ENDPOINT}/${heroId}`} alt={"hero icon"} marginInline={"auto"} /></li>)

  const radiantIcons = getIconsImg(radiant)
  const direIcons = getIconsImg(dire)

  return (
    <List display={"grid"} gridTemplateColumns={"repeat(5, 1fr)"} rowGap={"5"} backgroundColor={"dimgray"} border={"none"} borderRadius={10} marginInline={"40%"} marginBottom={2} padding={4}>
      {radiantIcons}
      {direIcons}
    </List>
  )
}

export default TeamIcons