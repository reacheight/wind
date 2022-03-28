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
import wind.models.{GameTimeState, PlayerId}
import wind.extensions._

import scala.collection.mutable

@UsesStringTable("ModifierNames")
class ModifierProcessor extends EntitiesProcessor {
  def smokedHeroes: Map[PlayerId, GameTimeState] = _smoked.toMap
  def scepter: Set[PlayerId] = _scepter.toSet
  def shard: Set[PlayerId] = _shard.toSet

  private val _smoked = mutable.Map.empty[PlayerId, GameTimeState]
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
      val time = Util.getGameTimeState(Entities)
      val playerId = combatLogHeroNameToPlayerId.get(cle.getTargetName)

      if (cle.getType == DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_MODIFIER_ADD)
        time.foreach(t => playerId.foreach(id => _smoked(PlayerId(id)) = t))

      if (cle.getType == DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_MODIFIER_REMOVE)
        playerId.foreach(id => _smoked.remove(PlayerId(id)))
    }
  }

  @OnModifierTableEntry
  def onModifierEntry(ctx: Context, modifier: DotaModifiers.CDOTAModifierBuffTableEntry): Unit = {
    val table = ctx.getProcessor(classOf[StringTables]).forName("ModifierNames")
    val modifierName = table.getNameByIndex(modifier.getModifierClass)
    val hero = Entities.get(modifier.getParent)
    hero
      .filter(Util.isHero)
      .foreach(h => {
        val playerId = Util.getPlayerId(h)

        if (modifierName == ScepterModifierName) {
          if (modifier.getEntryType == DOTA_MODIFIER_ENTRY_TYPE.DOTA_MODIFIER_ENTRY_TYPE_ACTIVE)
            _scepter.addOne(playerId)

          if (modifier.getEntryType == DOTA_MODIFIER_ENTRY_TYPE.DOTA_MODIFIER_ENTRY_TYPE_REMOVED)
            _scepter.remove(playerId)
        }

        if (modifierName == ShardModifierName) {
          if (modifier.getEntryType == DOTA_MODIFIER_ENTRY_TYPE.DOTA_MODIFIER_ENTRY_TYPE_ACTIVE)
            _shard.addOne(playerId)

          if (modifier.getEntryType == DOTA_MODIFIER_ENTRY_TYPE.DOTA_MODIFIER_ENTRY_TYPE_REMOVED)
            _shard.remove(playerId)
      }
    })
  }
}
