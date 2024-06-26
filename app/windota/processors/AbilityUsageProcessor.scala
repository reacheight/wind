package windota.processors

import skadistats.clarity.event.Insert
import skadistats.clarity.processor.entities.OnEntityPropertyChanged
import skadistats.clarity.processor.stringtables.{StringTables, UsesStringTable}
import skadistats.clarity.model.Entity
import skadistats.clarity.processor.runner.Context
import windota.Util
import windota.extensions.{EntitiesExtension, FieldPath}
import windota.models.internal.UnusedAbility
import windota.models.{AbilityId, GameTimeState, PlayerId}

import scala.collection.mutable.ListBuffer

@UsesStringTable("EntityNames")
class AbilityUsageProcessor extends ProcessorBase {
  def unusedAbilities: Seq[UnusedAbility] = _unusedAbilities.toSeq
  private val _unusedAbilities: ListBuffer[UnusedAbility] = ListBuffer.empty

  @Insert
  private val ctx: Context = null

  @OnEntityPropertyChanged(classPattern = "CDOTA_Unit_Hero_.*", propertyPattern = "m_lifeState")
  def onHeroDied(hero: Entity, fp: FieldPath): Unit = {
    if (!Util.isHero(hero) || hero.getPropertyForFieldPath[Int](fp) != 1) return
    val gameTimeState = GameTimeHelper.State
    if (Util.getSpawnTime(hero, gameTimeState.gameTime) < 10) return

    val playerId = PlayerId(hero.getProperty[Int]("m_iPlayerID"))

    val abilities = AbilitiesHelper.getAbilities(hero)

    addUnusedAbility("CDOTA_Ability_Slark_ShadowDance", 5497)
    addUnusedAbility("CDOTA_Ability_Slark_Depth_Shroud", 729)
    addUnusedAbility("CDOTA_Ability_Slark_Pounce", 5495)
    addUnusedAbility("CDOTA_Ability_Dazzle_Shallow_Grave", 5234)
    addUnusedAbility("CDOTA_Ability_Terrorblade_Sunder", 5622)
    addUnusedAbility("CDOTA_Ability_Life_Stealer_Rage", 5249)
    addUnusedAbility("CDOTA_Ability_Life_Stealer_Infest", 5252, requireScepter = true)
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
    addUnusedAbility("CDOTA_Ability_FacelessVoid_TimeWalk", 5182)
    addUnusedAbility("CDOTA_Ability_Luna_MoonGlaive", 5223, requireShard = true)
    addUnusedAbility("CDOTA_Ability_Morphling_Waveform", 5052)
    addUnusedAbility("CDOTA_Ability_PhantomLancer_Juxtapose", 5067, requireShard = true)
    addUnusedAbility("CDOTA_Ability_Ursa_Enrage", 5360)
    addUnusedAbility("CDOTA_Ability_Ursa_Earthshock", 5357, requireShard = true)
    addUnusedAbility("CDOTA_Ability_Enchantress_NaturesAttendants", 5269)
    addUnusedAbility("CDOTA_Ability_Enchantress_Bunny_Hop", 320)
    addUnusedAbility("CDOTA_Ability_Obsidian_Destroyer_AstralImprisonment", 5392) //TODO: check no bkb active
    addUnusedAbility("CDOTA_Ability_Shadow_Demon_Disruption", 5421) //TODO: check no bkb active
    addUnusedAbility("CDOTA_Ability_WitchDoctor_Voodoo_Switcheroo", 632)
    addUnusedAbility("CDOTA_Ability_DarkSeer_Surge", 5257)
    addUnusedAbility("CDOTA_Ability_Mirana_Leap", 5050)
    addUnusedAbility("CDOTA_Ability_Pangolier_Swashbuckle", 6344)
    addUnusedAbility("CDOTA_Ability_Phoenix_IcarusDive", 5623)
    addUnusedAbility("CDOTA_Ability_Windrunner_Windrun", 5132)

    def addUnusedAbility(entityName: String, abilityId: Int, requireScepter: Boolean = false, requireShard: Boolean = false): Unit =
      AbilitiesHelper.findUnusedAbility(hero, abilities, entityName)
        .filter(_ => !requireScepter || hasScepter(hero))
        .filter(_ => !requireShard || hasShard(hero))
        .foreach(_ => _unusedAbilities.addOne(UnusedAbility(playerId, playerId, AbilityId(abilityId), gameTimeState, withBlink = false)))
  }

  @OnEntityPropertyChanged(classPattern = "CDOTA_Unit_Hero_.*", propertyPattern = "m_lifeState")
  def onHeroDiedForAllies(hero: Entity, fp: FieldPath): Unit = {
    if (!Util.isHero(hero) || hero.getPropertyForFieldPath[Int](fp) != 1) return

    val gameTimeState = GameTimeHelper.State
    if (Util.getSpawnTime(hero, gameTimeState.gameTime) < 10) return

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
      case 2 => 800
      case 3 => 900
    }, checkBlink = true)

    addUnusedOnAllyAbility("Rubick", "CDOTA_Ability_Rubick_Telekinesis", 5448, {
      case 1 => 550
      case 2 => 575
      case 3 => 600
      case 4 => 625
    }, requireShard = true, checkBlink = true)

    addUnusedOnAllyAbility("Winter_Wyvern", "CDOTA_Ability_Winter_Wyvern_Cold_Embrace", 5653, {
      case 1 => 850
      case 2 => 900
      case 3 => 950
      case 4 => 1000
    })

    addUnusedOnAllyAbility("Omniknight", "CDOTA_Ability_Omniknight_Purification", 5263, _ => 600)
    addUnusedOnAllyAbility("Omniknight", "CDOTA_Ability_Omniknight_Martyr", 895, _ => 700)
    addUnusedOnAllyAbility("Omniknight", "CDOTA_Ability_Omniknight_GuardianAngel", 5266, _ => Int.MaxValue)

    addUnusedOnAllyAbility("Abaddon", "CDOTA_Ability_Abaddon_AphoticShield", 5586, _ => 550)
    addUnusedOnAllyAbility("Abaddon", "CDOTA_Ability_Abaddon_DeathCoil", 5585, {
      case 1 => 600
      case 2 => 625
      case 3 => 650
      case 4 => 675
    })

    addUnusedOnAllyAbility("Legion_Commander", "CDOTA_Ability_Legion_Commander_PressTheAttack", 5596, _ => 700)
    addUnusedOnAllyAbility("ArcWarden", "CDOTA_Ability_ArcWarden_MagneticField", 5678, _ => 1200)

    addUnusedOnAllyAbility("Undying", "CDOTA_Ability_Undying_SoulRip", 5443, _ => 750)
    addUnusedOnAllyAbility("Undying", "CDOTA_Ability_Undying_Tombstone", 5444, _ => 500)

    addUnusedOnAllyAbility("Weaver", "CDOTA_Ability_Weaver_TimeLapse", 5292, _ => 500, requireScepter = true, checkBlink = true)
    addUnusedOnAllyAbility("Pudge", "CDOTA_Ability_Pudge_Dismember", 5077, _ => 300, requireShard = true, checkBlink = true)
    addUnusedOnAllyAbility("EarthSpirit", "CDOTA_Ability_EarthSpirit_GeomagneticGrip", 5610, _ => 1100, requireShard = true, checkBlink = true)
    addUnusedOnAllyAbility("Ogre_Magi", "CDOTA_Ability_Ogre_Magi_Smash", 648, _ => 600, requireShard = true)
    addUnusedOnAllyAbility("Snapfire", "CDOTA_Ability_Snapfire_GobbleUp", 6484, _ => 150, requireScepter = true, checkBlink = true)
    addUnusedOnAllyAbility("Centaur", "CDOTA_Ability_Centaur_Work_Horse", 980, _ => 300, requireScepter = true, checkBlink = true)
    addUnusedOnAllyAbility("Obsidian_Destroyer", "CDOTA_Ability_Obsidian_Destroyer_AstralImprisonment", 5392, _ => 650, checkBlink = true) //TODO: check no bkb active
    addUnusedOnAllyAbility("Shadow_Demon", "CDOTA_Ability_Shadow_Demon_Disruption", 5421, _ => 650, checkBlink = true) //TODO: check no bkb active
    addUnusedOnAllyAbility("DarkSeer", "CDOTA_Ability_DarkSeer_Surge", 5257, _ => 600)

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
          AbilitiesHelper.findUnusedAbility(ally, entityName)
            .filter(_ => !requireScepter || hasScepter(ally))
            .filter(_ => !requireShard || hasShard(ally))
            .foreach(ability => {
              val distance = Util.getDistance(hero, ally)
              val abilityCastRange = castRange(ability.getProperty[Int]("m_iLevel")) + ItemsHelper.getAdditionalCastRange(ally)
              val castRangeWithBlink = abilityCastRange + getCastRangeIfHasBlink(ally)
              val needUseBlink = abilityCastRange < distance

              if (castRangeWithBlink >= distance)
                _unusedAbilities.addOne(UnusedAbility(allyPlayerId, deadPlayerId, AbilityId(abilityId), gameTimeState, needUseBlink))
            })
      })
    }
  }

  private def getCastRangeIfHasBlink(hero: Entity): Int = {
    val blinkItemName = "CDOTA_Item_BlinkDagger"
    val items = ItemsHelper.getItems(hero)
    ItemsHelper.findItem(items, blinkItemName)
      .filterNot(Util.isOnCooldown)
      .map(_ => 1200).getOrElse(0)
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
