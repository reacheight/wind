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
class AbilityUsageProcessor(checkUsage: Boolean) extends ProcessorBase {
  def unusedAbilities = _unusedAbilities.toSeq
  def unusedOnAllyAbilities = _unusedOnAllyAbilities.toSeq
  def unusedOnAllyWithBlinkAbilities = _unusedOnAllyWithBlinkAbilities.toSeq

  private val _unusedAbilities: ListBuffer[(GameTimeState, PlayerId, Int)] = ListBuffer.empty
  private val _unusedOnAllyAbilities: ListBuffer[(GameTimeState, PlayerId, PlayerId, Int)] = ListBuffer.empty
  private val _unusedOnAllyWithBlinkAbilities: ListBuffer[(GameTimeState, PlayerId, PlayerId, Int)] = ListBuffer.empty

  @Insert
  private val ctx: Context = null

  @OnEntityPropertyChanged(classPattern = "CDOTA_Unit_Hero_.*", propertyPattern = "m_lifeState")
  def onHeroDied(hero: Entity, fp: FieldPath): Unit = {
    if (!checkUsage || !Util.isHero(hero) || hero.getPropertyForFieldPath[Int](fp) != 1) return
    if (Util.getSpawnTime(hero, Time) < 10) return

    val playerId = PlayerId(hero.getProperty[Int]("m_iPlayerID"))

    val abilities = getAbilities(hero)

    addUnusedAbility("CDOTA_Ability_Slark_ShadowDance", 5497)
    addUnusedAbility("CDOTA_Ability_Slark_Depth_Shroud", 729)
    addUnusedAbility("CDOTA_Ability_Slark_Pounce", 5495)
    addUnusedAbility("CDOTA_Ability_Dazzle_Shallow_Grave", 5234)
    addUnusedAbility("CDOTA_Ability_Terrorblade_Sunder", 5622)
    addUnusedAbility("CDOTA_Ability_Life_Stealer_Rage", 5249)
    addUnusedAbility("CDOTA_Ability_Juggernaut_BladeFury", 5028)
    addUnusedAbility("CDOTA_Ability_DarkWillow_ShadowRealm", 6341)
    addUnusedAbility("templar_assassin_refraction", 5194)
    addUnusedAbility("CDOTA_Ability_PhantomLancer_Doppelwalk", 5066)
    addUnusedAbility("CDOTA_Ability_Weaver_TimeLapse", 5292)
    addUnusedAbility("CDOTA_Ability_Winter_Wyvern_Cold_Embrace", 5653)
    addUnusedAbility("puck_phase_shift", 5072)
    addUnusedAbility("CDOTA_Ability_VoidSpirit_Dissimilate", 6470)
    addUnusedAbility("CDOTA_Ability_Riki_TricksOfTheTrade", 5145)
    addUnusedAbility("CDOTA_Ability_Legion_Commander_PressTheAttack", 5596)
    addUnusedAbility("CDOTA_Ability_Omniknight_Purification", 5263)
    addUnusedAbility("CDOTA_Ability_Abaddon_AphoticShield", 5586)
    addUnusedAbility("CDOTA_Ability_AntiMage_Blink", 5004)
    addUnusedAbility("CDOTA_Ability_QueenOfPain_Blink", 5174)
    addUnusedAbility("CDOTA_Ability_ArcWarden_MagneticField", 5678)
    addUnusedAbility("alchemist_chemical_rage", 5369)
    addUnusedAbility("CDOTA_Ability_Zuus_Heavenly_Jump", 641)
    addUnusedAbility("sniper_concussive_grenade", 694)

    def addUnusedAbility(entityName: String, abilityId: Int): Unit =
      findUnusedAbility(hero, abilities, entityName)
        .foreach(_ => _unusedAbilities.addOne((TimeState, playerId, abilityId)))
  }

  @OnEntityPropertyChanged(classPattern = "CDOTA_Unit_Hero_.*", propertyPattern = "m_lifeState")
  def onHeroDiedForAllies(hero: Entity, fp: FieldPath): Unit = {
    if (!checkUsage || !Util.isHero(hero) || hero.getPropertyForFieldPath[Int](fp) != 1) return

    val gameRules = Entities.getByDtName("CDOTAGamerulesProxy")
    if (Util.getSpawnTime(hero, Time) < 10) return

    val gameTime = TimeState
    val deadPlayerId = PlayerId(hero.getProperty[Int]("m_iPlayerID"))

    val allies = Entities.filter(Util.isHero)
      .filter(h => h.getProperty[Int]("m_iTeamNum") == hero.getProperty[Int]("m_iTeamNum"))
      .filter(h => h.getProperty[Int]("m_lifeState") == 0)
      .filter(h => h.getHandle != hero.getHandle)

    // todo брать свойства абилки из файлика доты с описанием всех скиллов или с stratz ???
    // todo учитывать роль убитого персонажа ???
    // todo смотреть на бкб

    addUnusedOnAllyAbility("Dazzle", "CDOTA_Ability_Dazzle_Shallow_Grave", 5234, {
      case 1 => 700
      case 2 => 800
      case 3 => 900
      case 4 => 1000
    }, checkBlink = true)

    addUnusedOnAllyAbility("Oracle", "CDOTA_Ability_Oracle_FalsePromise", 5640, {
      case 1 => 700
      case 2 => 850
      case 3 => 1000
    }, checkBlink = true)

    addUnusedOnAllyAbility("Rubick", "CDOTA_Ability_Rubick_Telekinesis", 5448, {
      case 1 => 550
      case 2 => 575
      case 3 => 600
      case 4 => 625
    }, requireShard = true, checkBlink = true)

    addUnusedOnAllyAbility("Winter_Wyvern", "CDOTA_Ability_Winter_Wyvern_Cold_Embrace", 5653, _ => 1000)
    addUnusedOnAllyAbility("Omniknight", "CDOTA_Ability_Omniknight_Purification", 5263, _ => 550)
    addUnusedOnAllyAbility("Abaddon", "CDOTA_Ability_Abaddon_AphoticShield", 5586, _ => 550)
    addUnusedOnAllyAbility("Abaddon", "CDOTA_Ability_Abaddon_DeathCoil", 5585, _ => 575)
    addUnusedOnAllyAbility("Legion_Commander", "CDOTA_Ability_Legion_Commander_PressTheAttack", 5596, _ => 700)
    addUnusedOnAllyAbility("ArcWarden", "CDOTA_Ability_ArcWarden_MagneticField", 5678, _ => 1050)
    addUnusedOnAllyAbility("Undying", "CDOTA_Ability_Undying_SoulRip", 5443, _ => 750)
    addUnusedOnAllyAbility("Weaver", "CDOTA_Ability_Weaver_TimeLapse", 5292, _ => 500, requireScepter = true, checkBlink = true)
    addUnusedOnAllyAbility("Pudge", "CDOTA_Ability_Pudge_Dismember", 5077, _ => 300, requireShard = true, checkBlink = true)
    addUnusedOnAllyAbility("Snapfire", "CDOTA_Ability_Snapfire_GobbleUp", 6484, _ => 150, requireScepter = true, checkBlink = true)

    def addUnusedOnAllyAbility(heroName: String,
                               entityName: String,
                               abilityId: Int,
                               castRange: PartialFunction[Int, Int],
                               requireScepter: Boolean = false,
                               requireShard: Boolean = false,
                               checkBlink: Boolean = false): Unit = {
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
                _unusedOnAllyAbilities.addOne(gameTime, deadPlayerId, allyPlayerId, abilityId)
              else if (checkBlink && abilityCastRange + getCastRangeIfHasBlink(ally) >= distance)
                _unusedOnAllyWithBlinkAbilities.addOne(gameTime, deadPlayerId, allyPlayerId, abilityId)
            })
      })
    }
  }

  def getAbilities(hero: Entity): Seq[Entity] = {
    (0 to 31)
      .map(i => hero.getProperty[Int](s"m_hAbilities.000$i"))
      .filter(_ != Util.NullValue)
      .flatMap(Entities.get)
  }

  def findAbility(abilities: Seq[Entity], name: String): Option[Entity] = {
    val stringTable = ctx.getProcessor(classOf[StringTables]).forName("EntityNames")
    abilities
      .find(ability => ability.getDtClass.getDtName == name ||
        stringTable.getNameByIndex(ability.getProperty[Int]("m_pEntity.m_nameStringableIndex")) == name)
      .filter(ability => ability.getProperty[Int]("m_iLevel") > 0)
  }

  private def findUnusedAbility(hero: Entity, abilities: Seq[Entity], name: String): Option[Entity] = {
    findAbility(abilities, name)
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
