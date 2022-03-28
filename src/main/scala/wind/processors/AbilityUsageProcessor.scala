package wind.processors

import skadistats.clarity.event.Insert
import skadistats.clarity.model.{Entity, FieldPath}
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.processor.stringtables.{StringTables, UsesStringTable}
import wind.Util
import wind.models.{GameTimeState, PlayerId}
import wind.extensions._

import scala.collection.mutable.ListBuffer

@UsesStringTable("EntityNames")
class AbilityUsageProcessor extends EntitiesProcessor {
  def unusedAbilities: Seq[(GameTimeState, PlayerId, String)] = _unusedAbilities.toSeq
  def unusedOnAllyAbilities: Seq[(GameTimeState, PlayerId, PlayerId, String)] = _unusedOnAllyAbilities.toSeq

  private val _unusedAbilities: ListBuffer[(GameTimeState, PlayerId, String)] = ListBuffer.empty
  private val _unusedOnAllyAbilities: ListBuffer[(GameTimeState, PlayerId, PlayerId, String)] = ListBuffer.empty

  @Insert
  private val ctx: Context = null

  @OnEntityPropertyChanged(classPattern = "CDOTA_Unit_Hero_.*", propertyPattern = "m_lifeState")
  def onHeroDied(hero: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    if (!Util.isHero(hero) || hero.getPropertyForFieldPath[Int](fp) != 1) return

    val gameRules = Entities.getByDtName("CDOTAGamerulesProxy")
    if (Util.getSpawnTime(hero, gameRules.getProperty[Float]("m_pGameRules.m_fGameTime")) < 10) return

    val time = Util.getGameTimeState(gameRules)
    val playerId = PlayerId(hero.getProperty[Int]("m_iPlayerID"))

    val abilities = getAbilities(hero)

    addUnusedAbility("CDOTA_Ability_Slark_ShadowDance", "Shadow Dance")
    addUnusedAbility("CDOTA_Ability_Slark_Depth_Shroud", "Depths Shroud")
    addUnusedAbility("CDOTA_Ability_Slark_Pounce", "Pounce")
    addUnusedAbility("CDOTA_Ability_Dazzle_Shallow_Grave", "Shallow Grave")
    addUnusedAbility("CDOTA_Ability_Terrorblade_Sunder", "Sunder")
    addUnusedAbility("CDOTA_Ability_Life_Stealer_Rage", "Rage")
    addUnusedAbility("CDOTA_Ability_Juggernaut_BladeFury", "Blade Fury")
    addUnusedAbility("CDOTA_Ability_DarkWillow_ShadowRealm", "Shadow Realm")
    addUnusedAbility("templar_assassin_refraction", "Refraction")
    addUnusedAbility("CDOTA_Ability_PhantomLancer_Doppelwalk", "Doppelganger")
    addUnusedAbility("CDOTA_Ability_Weaver_TimeLapse", "Time Lapse")
    addUnusedAbility("CDOTA_Ability_Winter_Wyvern_Cold_Embrace", "Cold Embrace")
    addUnusedAbility("puck_phase_shift", "Phase Shift")
    addUnusedAbility("CDOTA_Ability_VoidSpirit_Dissimilate", "Dissimilate")
    addUnusedAbility("CDOTA_Ability_Riki_TricksOfTheTrade", "Tricks of the Trade")
    addUnusedAbility("CDOTA_Ability_Legion_Commander_PressTheAttack", "Press the Attack")
    addUnusedAbility("CDOTA_Ability_Omniknight_Purification", "Purification")
    addUnusedAbility("CDOTA_Ability_Abaddon_AphoticShield", "Aphotic Shield")
    addUnusedAbility("CDOTA_Ability_AntiMage_Blink", "Blink")
    addUnusedAbility("CDOTA_Ability_QueenOfPain_Blink", "Blink")
    addUnusedAbility("CDOTA_Ability_ArcWarden_MagneticField", "Magnetic Field")

    def addUnusedAbility(entityName: String, realName: String): Unit =
      findUnusedAbility(hero, abilities, entityName)
        .foreach(_ => _unusedAbilities.addOne((time, playerId, realName)))
  }

  @OnEntityPropertyChanged(classPattern = "CDOTA_Unit_Hero_.*", propertyPattern = "m_lifeState")
  def onHeroDiedForAllies(hero: Entity, fp: FieldPath[_ <: FieldPath[_ <: AnyRef]]): Unit = {
    if (!Util.isHero(hero) || hero.getPropertyForFieldPath[Int](fp) != 1) return

    val gameRules = Entities.getByDtName("CDOTAGamerulesProxy")
    if (Util.getSpawnTime(hero, gameRules.getProperty[Float]("m_pGameRules.m_fGameTime")) < 10) return

    val gameTime = Util.getGameTimeState(gameRules)
    val deadPlayerId = PlayerId(hero.getProperty[Int]("m_iPlayerID"))

    val allies = Entities.filter(Util.isHero)
      .filter(h => h.getProperty[Int]("m_iTeamNum") == hero.getProperty[Int]("m_iTeamNum"))
      .filter(h => h.getProperty[Int]("m_lifeState") == 0)
      .filter(h => h.getHandle != hero.getHandle)

    // todo брать свойства абилки из файлика доты с описанием всех скиллов или с stratz ???
    // todo учитывать роль убитого персонажа ???
    // todo смотреть на бкб

    addUnusedOnAllyAbility("CDOTA_Ability_Dazzle_Shallow_Grave", "Shalow Grave", {
      case 1 => 700
      case 2 => 800
      case 3 => 900
      case 4 => 1000
    })

    addUnusedOnAllyAbility("CDOTA_Ability_Oracle_FalsePromise", "False Promise", {
      case 1 => 700
      case 2 => 850
      case 3 => 1000
    })

    addUnusedOnAllyAbility("CDOTA_Ability_Winter_Wyvern_Cold_Embrace", "Cold Embrace", _ => 1000)
    addUnusedOnAllyAbility("CDOTA_Ability_Omniknight_Purification", "Purification", _ => 550)
    addUnusedOnAllyAbility("CDOTA_Ability_Abaddon_AphoticShield", "Aphotic Shield", _ => 550)
    addUnusedOnAllyAbility("CDOTA_Ability_Abaddon_DeathCoil", "Mist Coil", _ => 575)
    addUnusedOnAllyAbility("CDOTA_Ability_Legion_Commander_PressTheAttack", "Press the Attack", _ => 700)
    addUnusedOnAllyAbility("CDOTA_Ability_ArcWarden_MagneticField", "Magnetic Field", _ => 1050)
    addUnusedOnAllyAbility("CDOTA_Ability_Weaver_TimeLapse", "Time Lapse", _ => 500, requireScepter = true)
    addUnusedOnAllyAbility("CDOTA_Ability_Pudge_Dismember", "Dismember", _ => 300, requireShard = true)
    addUnusedOnAllyAbility("CDOTA_Ability_Snapfire_GobbleUp", "Gobble Up", _ => 150, requireScepter = true)

    def addUnusedOnAllyAbility(entityName: String, realName: String, castRange: PartialFunction[Int, Int], requireScepter: Boolean = false, requireShard: Boolean = false): Unit = {
      allies.foreach(ally => {
        val allyPlayerId = PlayerId(ally.getProperty[Int]("m_iPlayerID"))
        findUnusedAbility(ally, getAbilities(ally), entityName)
          .filter(ability => {
            val distance = Util.getDistance(hero, ally)
            val abilityCastRange = castRange(ability.getProperty[Int]("m_iLevel")) + getAdditionalCastRange(ally)
            abilityCastRange >= distance
          })
          .filter(_ => !requireScepter || hasScepter(ally))
          .filter(_ => !requireShard || hasShard(ally))
          .foreach(_ => _unusedOnAllyAbilities.addOne(gameTime, deadPlayerId, allyPlayerId, realName))
      })
    }
  }

  private def getAbilities(hero: Entity): Seq[Entity] = {
    (0 to 31)
      .map(i => hero.getProperty[Int](s"m_hAbilities.000$i"))
      .filter(_ != Util.NullValue)
      .flatMap(Entities.get)
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

  private def getAdditionalCastRange(hero: Entity): Int = {
    val castRangeItems = Seq(
      ("CDOTA_Item_Aether_Lens", 225),
      ("item_octarine_core", 225),
      ("CDOTA_Item_Keen_Optic", 75),
      ("CDOTA_Item_Psychic_Headband", 100),
      ("CDOTA_Item_Seer_Stone", 350),
      ("CDOTA_Item_Telescope", 110),
    )

    val itemUsageProcessor = ctx.getProcessor(classOf[ItemUsageProcessor])
    val items = itemUsageProcessor.getItems(hero)
    castRangeItems.filter(item => itemUsageProcessor.findItem(items, item._1).nonEmpty).map(_._2).sum
  }

  private def hasScepter(hero: Entity): Boolean = {
    val modifierProcessor = ctx.getProcessor(classOf[ModifierProcessor])
    modifierProcessor.scepter.contains(Util.getPlayerId(hero))
  }

  private def hasShard(hero: Entity): Boolean = {
    val modifierProcessor = ctx.getProcessor(classOf[ModifierProcessor])
    modifierProcessor.shard.contains(Util.getPlayerId(hero))
  }
}
