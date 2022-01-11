package wind.processors

import skadistats.clarity.event.Insert
import skadistats.clarity.model.{Entity, FieldPath}
import skadistats.clarity.processor.entities.{Entities, OnEntityPropertyChanged}
import wind.{GameTimeState, Util}
import wind.models.PlayerId

import scala.collection.mutable.ListBuffer

class AbilityUsageProcessor {
  def unusedAbilities: Seq[(GameTimeState, PlayerId, String)] = _unusedAbilities.toSeq

  private val _unusedAbilities: ListBuffer[(GameTimeState, PlayerId, String)] = ListBuffer.empty

  @Insert
  private val entities: Entities = null

  @OnEntityPropertyChanged(classPattern = "CDOTA_Unit_Hero_.*", propertyPattern = "m_lifeState")
  def onHeroDied(hero: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    if (!Util.isHero(hero) || hero.getPropertyForFieldPath[Int](fp) != 2) return

    val time = Util.getGameTimeState(entities.getByDtName("CDOTAGamerulesProxy"))
    val playerId = PlayerId(hero.getProperty[Int]("m_iPlayerID"))

    val abilities = getAbilities(hero)

    findUnusedAbility(hero, abilities, "CDOTA_Ability_Slark_ShadowDance")
      .foreach(_ => _unusedAbilities.addOne((time, playerId, "Shadow Dance")))
    findUnusedAbility(hero, abilities, "CDOTA_Ability_Dazzle_Shallow_Grave")
      .foreach(_ => _unusedAbilities.addOne((time, playerId, "Shallow Grave")))
    findUnusedAbility(hero, abilities, "CDOTA_Ability_Terrorblade_Sunder")
      .foreach(_ => _unusedAbilities.addOne((time, playerId, "Sunder")))
    findUnusedAbility(hero, abilities, "CDOTA_Ability_Life_Stealer_Rage")
      .foreach(_ => _unusedAbilities.addOne((time, playerId, "Rage")))
  }

  private def getAbilities(hero: Entity): Seq[Entity] = {
    (0 to 31)
      .map(i => hero.getProperty[Int](s"m_hAbilities.000$i"))
      .filter(_ != Util.NullValue)
      .map(entities.getByHandle)
  }

  private def findUnusedAbility(hero: Entity, abilities: Seq[Entity], name: String): Option[Entity] = {
    abilities
      .find(ability => ability.getDtClass.getDtName == name)
      .filter(ability => ability.getProperty[Int]("m_iLevel") > 0)
      .filter(ability => Util.hasEnoughMana(hero, ability))
      .filterNot(Util.isOnCooldown)
  }
}
