package wind.processors

import skadistats.clarity.model.{CombatLogEntry, Entity}
import skadistats.clarity.processor.entities.OnEntityCreated
import skadistats.clarity.processor.gameevents.OnCombatLogEntry
import skadistats.clarity.processor.modifiers.OnModifierTableEntry
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.processor.stringtables.{StringTables, UsesStringTable}
import skadistats.clarity.wire.common.proto.DotaModifiers
import skadistats.clarity.wire.common.proto.DotaModifiers.DOTA_MODIFIER_ENTRY_TYPE
import skadistats.clarity.wire.common.proto.DotaUserMessages.DOTA_COMBATLOG_TYPES
import wind.Util
import wind.models.PlayerId

import scala.collection.mutable

@UsesStringTable("ModifierNames")
class ModifierProcessor extends EntitiesProcessor {
  def smokedHeroes: Set[PlayerId] = _smoked.toSet
  def scepter: Set[PlayerId] = _scepter.toSet
  def shard: Set[PlayerId] = _shard.toSet

  private val _smoked = mutable.Set.empty[PlayerId]
  private val _scepter = mutable.Set.empty[PlayerId]
  private val _shard = mutable.Set.empty[PlayerId]

  private val SmokeModifierName = "modifier_smoke_of_deceit"
  private val ShardModifierName = "modifier_item_aghanims_shard"
  private val ScepterModifierName = "modifier_item_ultimate_scepter"

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

  @OnModifierTableEntry
  def onModifierEntry(ctx: Context, modifier: DotaModifiers.CDOTAModifierBuffTableEntry): Unit = {
    val table = ctx.getProcessor(classOf[StringTables]).forName("ModifierNames")
    val modifierName = table.getNameByIndex(modifier.getModifierClass)
    val hero = Entities.getByHandle(modifier.getParent)
    if (hero == null) return

    if (modifierName == ScepterModifierName) {
      val playerId = Util.getPlayerId(hero)

      if (modifier.getEntryType == DOTA_MODIFIER_ENTRY_TYPE.DOTA_MODIFIER_ENTRY_TYPE_ACTIVE)
        _scepter.addOne(playerId)

      if (modifier.getEntryType == DOTA_MODIFIER_ENTRY_TYPE.DOTA_MODIFIER_ENTRY_TYPE_REMOVED)
        _scepter.remove(playerId)
    }

    if (modifierName == ShardModifierName) {
      val playerId = Util.getPlayerId(hero)

      if (modifier.getEntryType == DOTA_MODIFIER_ENTRY_TYPE.DOTA_MODIFIER_ENTRY_TYPE_ACTIVE)
        _shard.addOne(playerId)

      if (modifier.getEntryType == DOTA_MODIFIER_ENTRY_TYPE.DOTA_MODIFIER_ENTRY_TYPE_REMOVED)
        _shard.remove(playerId)
    }
  }
}
