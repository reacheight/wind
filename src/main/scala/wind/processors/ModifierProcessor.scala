package wind.processors

import skadistats.clarity.model.{CombatLogEntry, Entity}
import skadistats.clarity.processor.entities.OnEntityCreated
import skadistats.clarity.processor.gameevents.OnCombatLogEntry
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.wire.common.proto.DotaUserMessages.DOTA_COMBATLOG_TYPES
import wind.models.PlayerId

import scala.collection.mutable

class ModifierProcessor extends EntitiesProcessor {
  def smokedHeroes: Set[PlayerId] = _smoked.toSet

  private val _smoked = mutable.Set.empty[PlayerId]

  private val SmokeModifierName = "modifier_smoke_of_deceit"
  private var combatLogHeroNameToPlayerId = Map.empty[String, Int]

  @OnEntityCreated(classPattern = "CWorld")
  def init(ctx: Context, e: Entity): Unit = {
    combatLogHeroNameToPlayerId = ctx.getProcessor(classOf[HeroProcessor]).combatLogNameToPlayerId
  }

  @OnCombatLogEntry
  def onCombatLog(cle: CombatLogEntry): Unit = {
    if (cle.getInflictorName == SmokeModifierName) {
      if (cle.getType == DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_MODIFIER_ADD)
        combatLogHeroNameToPlayerId.get(cle.getTargetName).foreach(id => _smoked.addOne(PlayerId(id)))

      if (cle.getType == DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_MODIFIER_REMOVE)
        combatLogHeroNameToPlayerId.get(cle.getTargetName).foreach(id => _smoked.remove(PlayerId(id)))
    }
  }

    // will keep it there for future :clown_face:
//  @OnModifierTableEntry
//  def onModifierEntry(modifier: DotaModifiers.CDOTAModifierBuffTableEntry): Unit = {
//    val gameRules = Entities.getByDtName("CDOTAGamerulesProxy")
//    if (gameRules == null) return
//
//    val time = Util.getGameTimeState(gameRules)
//    val table = ctx.getProcessor(classOf[StringTables]).forName("ModifierNames")
//    val modifierName = table.getNameByIndex(modifier.getModifierClass)
//
//
//    if (modifierName.contains("smoke") && modifier.getEntryType == DOTA_MODIFIER_ENTRY_TYPE.DOTA_MODIFIER_ENTRY_TYPE_ACTIVE) {
//      val hero = Entities.getByHandle(modifier.getParent)
//      _smoked.addOne(modifier.getSerialNum -> Util.getPlayerId(hero))
//      println(s"${hero.getDtClass.getDtName} smoked with num ${modifier.getSerialNum} at $time")
//    }
//
//    if (modifier.getEntryType == DOTA_MODIFIER_ENTRY_TYPE.DOTA_MODIFIER_ENTRY_TYPE_REMOVED && _smoked.contains(modifier.getSerialNum)) {
//      _smoked.remove(modifier.getSerialNum)
//      println(s"remove ${modifier.getSerialNum} at $time")
//    }
//  }
}
