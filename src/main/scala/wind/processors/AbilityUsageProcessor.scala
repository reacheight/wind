package wind.processors

import skadistats.clarity.event.Insert
import skadistats.clarity.model.{Entity, FieldPath}
import skadistats.clarity.processor.entities.{Entities, OnEntityPropertyChanged}
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.processor.stringtables.{StringTables, UsesStringTable}
import wind.{GameTimeState, Util}
import wind.models.PlayerId

import scala.collection.mutable.ListBuffer

@UsesStringTable("EntityNames")
class AbilityUsageProcessor {
  def unusedAbilities: Seq[(GameTimeState, PlayerId, String)] = _unusedAbilities.toSeq

  private val _unusedAbilities: ListBuffer[(GameTimeState, PlayerId, String)] = ListBuffer.empty

  @Insert
  private val entities: Entities = null
  @Insert
  private val ctx: Context = null

  @OnEntityPropertyChanged(classPattern = "CDOTA_Unit_Hero_.*", propertyPattern = "m_lifeState")
  def onHeroDied(hero: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    if (!Util.isHero(hero) || hero.getPropertyForFieldPath[Int](fp) != 1) return

    val gameRules = entities.getByDtName("CDOTAGamerulesProxy")
    if (Util.getSpawnTime(hero, gameRules.getProperty[Float]("m_pGameRules.m_fGameTime")) < 10) return

    val time = Util.getGameTimeState(gameRules)
    val playerId = PlayerId(hero.getProperty[Int]("m_iPlayerID"))

    val abilities = getAbilities(hero)

    addUnusedAbility("CDOTA_Ability_Slark_ShadowDance", "Shadow Dance")
    addUnusedAbility("CDOTA_Ability_Slark_Depth_Shroud", "Depths Shroud")
    addUnusedAbility("CDOTA_Ability_Dazzle_Shallow_Grave", "Shallow Grave")
    addUnusedAbility("CDOTA_Ability_Terrorblade_Sunder", "Sunder")
    addUnusedAbility("CDOTA_Ability_Life_Stealer_Rage", "Rage")
    addUnusedAbility("CDOTA_Ability_Juggernaut_BladeFury", "Blade Fury")
    addUnusedAbility("CDOTA_Ability_DarkWillow_ShadowRealm", "Shadow Realm")
    addUnusedAbility("templar_assassin_refraction", "Refraction")

    def addUnusedAbility(entityName: String, realName: String): Unit =
      findUnusedAbility(hero, abilities, entityName)
        .foreach(_ => _unusedAbilities.addOne((time, playerId, realName)))
  }

  private def getAbilities(hero: Entity): Seq[Entity] = {
    (0 to 31)
      .map(i => hero.getProperty[Int](s"m_hAbilities.000$i"))
      .filter(_ != Util.NullValue)
      .map(entities.getByHandle)
      .filter(_ != null)
  }

  private def findUnusedAbility(hero: Entity, abilities: Seq[Entity], name: String): Option[Entity] = {
    val stringTable = ctx.getProcessor(classOf[StringTables]).forName("EntityNames")
    abilities
      .find(ability => ability.getDtClass.getDtName == name ||
        stringTable.getNameByIndex(ability.getProperty[Int]("m_pEntity.m_nameStringableIndex")) == name)
      .filter(ability => ability.getProperty[Int]("m_iLevel") > 0)
      .filter(ability => Util.hasEnoughMana(hero, ability))
      .filterNot(Util.isOnCooldown)
  }
}
