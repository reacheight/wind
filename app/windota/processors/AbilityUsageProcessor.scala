package windota.processors

import skadistats.clarity.event.Insert
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import skadistats.clarity.processor.stringtables.{StringTables, UsesStringTable}
import skadistats.clarity.model.Entity
import skadistats.clarity.processor.runner.Context
import windota.Util
import windota.extensions.{EntitiesExtension, FieldPath}
import windota.models.{GameTimeState, PlayerId}

import scala.collection.mutable.ListBuffer

@UsesStringTable("EntityNames")
class AbilityUsageProcessor extends EntitiesProcessor {
  def unusedAbilities: Seq[(GameTimeState, PlayerId, String)] = _unusedAbilities.toSeq
  def unusedOnAllyAbilities: Seq[(GameTimeState, PlayerId, PlayerId, String)] = _unusedOnAllyAbilities.toSeq
  def unusedOnAllyWithBlinkAbilities: Seq[(GameTimeState, PlayerId, PlayerId, String)] = _unusedOnAllyWithBlinkAbilities.toSeq

  private val _unusedAbilities: ListBuffer[(GameTimeState, PlayerId, String)] = ListBuffer.empty
  private val _unusedOnAllyAbilities: ListBuffer[(GameTimeState, PlayerId, PlayerId, String)] = ListBuffer.empty
  private val _unusedOnAllyWithBlinkAbilities: ListBuffer[(GameTimeState, PlayerId, PlayerId, String)] = ListBuffer.empty

  @Insert
  private val ctx: Context = null

  @OnEntityPropertyChanged(classPattern = "CDOTA_Unit_Hero_.*", propertyPattern = "m_lifeState")
  def onHeroDied(hero: Entity, fp: FieldPath): Unit = {
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
    addUnusedAbility("alchemist_chemical_rage", "Chemical Rage")
    addUnusedAbility("CDOTA_Ability_Zuus_Heavenly_Jump", "Heavenly Jump")

    def addUnusedAbility(entityName: String, realName: String): Unit =
      findUnusedAbility(hero, abilities, entityName)
        .foreach(_ => _unusedAbilities.addOne((time, playerId, realName)))
  }

  @OnEntityPropertyChanged(classPattern = "CDOTA_Unit_Hero_.*", propertyPattern = "m_lifeState")
  def onHeroDiedForAllies(hero: Entity, fp: FieldPath): Unit = {
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

    addUnusedOnAllyAbility("Dazzle", "CDOTA_Ability_Dazzle_Shallow_Grave", "Shalow Grave", {
      case 1 => 700
      case 2 => 800
      case 3 => 900
      case 4 => 1000
    }, checkBlink = true)

    addUnusedOnAllyAbility("Oracle", "CDOTA_Ability_Oracle_FalsePromise", "False Promise", {
      case 1 => 700
      case 2 => 850
      case 3 => 1000
    }, checkBlink = true)

    addUnusedOnAllyAbility("Rubick", "CDOTA_Ability_Rubick_Telekinesis", "Telekinesis", {
      case 1 => 550
      case 2 => 575
      case 3 => 600
      case 4 => 625
    }, requireShard = true, checkBlink = true)

    addUnusedOnAllyAbility("Winter_Wyvern", "CDOTA_Ability_Winter_Wyvern_Cold_Embrace", "Cold Embrace", _ => 1000)
    addUnusedOnAllyAbility("Omniknight", "CDOTA_Ability_Omniknight_Purification", "Purification", _ => 550)
    addUnusedOnAllyAbility("Abaddon", "CDOTA_Ability_Abaddon_AphoticShield", "Aphotic Shield", _ => 550)
    addUnusedOnAllyAbility("Abaddon", "CDOTA_Ability_Abaddon_DeathCoil", "Mist Coil", _ => 575)
    addUnusedOnAllyAbility("Legion_Commander", "CDOTA_Ability_Legion_Commander_PressTheAttack", "Press the Attack", _ => 700)
    addUnusedOnAllyAbility("ArcWarden", "CDOTA_Ability_ArcWarden_MagneticField", "Magnetic Field", _ => 1050)
    addUnusedOnAllyAbility("Undying", "CDOTA_Ability_Undying_SoulRip", "Soul Rip", _ => 750)
    addUnusedOnAllyAbility("Weaver", "CDOTA_Ability_Weaver_TimeLapse", "Time Lapse", _ => 500, requireScepter = true, checkBlink = true)
    addUnusedOnAllyAbility("Pudge", "CDOTA_Ability_Pudge_Dismember", "Dismember", _ => 300, requireShard = true, checkBlink = true)
    addUnusedOnAllyAbility("Snapfire", "CDOTA_Ability_Snapfire_GobbleUp", "Gobble Up", _ => 150, requireScepter = true, checkBlink = true)

    def addUnusedOnAllyAbility(heroName: String, entityName: String, realName: String, castRange: PartialFunction[Int, Int], requireScepter: Boolean = false, requireShard: Boolean = false, checkBlink: Boolean = false): Unit = {
      allies
        .find(ally => ally.getDtClass.getDtName.contains(heroName))
        .foreach(ally => {
          val allyPlayerId = PlayerId(ally.getProperty[Int]("m_iPlayerID"))
          findUnusedAbility(ally, getAbilities(ally), entityName)
            .filter(_ => !requireScepter || hasScepter(ally))
            .filter(_ => !requireShard || hasShard(ally))
            .foreach(ability => {
              val distance = Util.getDistance(hero, ally)
              val abilityCastRange = castRange(ability.getProperty[Int]("m_iLevel")) + getAdditionalCastRange(ally)

              if (abilityCastRange >= distance)
                _unusedOnAllyAbilities.addOne(gameTime, deadPlayerId, allyPlayerId, realName)
              else if (checkBlink && abilityCastRange + getCastRangeIfHasBlink(ally) >= distance)
                _unusedOnAllyWithBlinkAbilities.addOne(gameTime, deadPlayerId, allyPlayerId, realName)
            })
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
    val itemUsageProcessor = ctx.getProcessor(classOf[ItemUsageProcessor])
    itemUsageProcessor.getAdditionalCastRange(hero)
  }

  private def getCastRangeIfHasBlink(hero: Entity): Int = {
    val itemUsageProcessor = ctx.getProcessor(classOf[ItemUsageProcessor])
    itemUsageProcessor.getCastRangeIfHasBlink(hero)
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
