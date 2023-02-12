import { HeroAbility } from "../../../models/HeroAbility";
import Image from "next/image";
import Routes from "../../../api/routs";
import { HStack } from "@chakra-ui/layout";

import styles from "./Hud.module.css";

interface AbilitiesPanelProps {
  selectedAbility: number
  onAbilityClick: Function
  abilities: ReadonlyArray<HeroAbility>
}

const AbilitiesPanel = ({ selectedAbility, onAbilityClick, abilities }: AbilitiesPanelProps) => {
  const abilitiesIcons = abilities.map(ability => {
    const abilityClassName = ability.id === selectedAbility ? styles.selectedAbility : styles.ability
    return <div className={abilityClassName}>
      <button onClick={() => onAbilityClick(ability.id)}>
        <Image src={Routes.Images.getAbilityIcon(ability.id)} width={100} height={100}/>
      </button>
    </div>;
  })

  return (
    <div className={styles.abilities}>
      <HStack spacing='24px'>
        {abilitiesIcons}
      </HStack>
    </div>
  )
}

export default AbilitiesPanel