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
import wind.models.{GameTimeState, PlayerId, Stun}
import wind.extensions._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

@UsesStringTable("ModifierNames")
class ModifierProcessor extends EntitiesProcessor {
  def overlappedStuns: Seq[(GameTimeState, PlayerId, PlayerId)] = _overlappedStuns.toSeq

  def smokedHeroes: Map[PlayerId, GameTimeState] = _smoked.toMap
  def scepter: Set[PlayerId] = _scepter.toSet
  def shard: Set[PlayerId] = _shard.toSet
  def stunned: Map[PlayerId, Stun] = _stun.toMap

  private val _overlappedStuns = ListBuffer.empty[(GameTimeState, PlayerId, PlayerId)]

  private val _smoked = mutable.Map.empty[PlayerId, GameTimeState]
  private val _scepter = mutable.Set.empty[PlayerId]
  private val _shard = mutable.Set.empty[PlayerId]
  private val _stun = mutable.Map.empty[PlayerId, Stun]

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
    if (cle.hasStunDuration && cle.getStunDuration > 0.1 && cle.getInflictorName != "modifier_bashed" && !cle.isTargetIllusion) {
      for {
        time <- Util.getGameTimeState(Entities)
        stunnedPlayerId <- combatLogHeroNameToPlayerId.get(cle.getTargetName)
        attackerPlayerId <- combatLogHeroNameToPlayerId.get(cle.getAttackerName)
      } yield {
        val stunnedId = PlayerId(stunnedPlayerId)
        val attackerId = PlayerId(attackerPlayerId)
        val prevStun = _stun.get(stunnedId)

        if (cle.getType == DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_MODIFIER_ADD) {
          val newStun = Stun(time, cle.getStunDuration)
          prevStun match {
            case None => _stun(stunnedId) = newStun
            case Some(stun) =>
              if (newStun.end.gameTime > stun.end.gameTime)
                _stun(stunnedId) = newStun

              if ((stun.end.gameTime - time.gameTime) >= 1.5 && newStun.duration > 0.5)
                _overlappedStuns.addOne((time, stunnedId, attackerId))
          }
        }

        if (cle.getType == DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_MODIFIER_REMOVE)
          prevStun
            .filter(stun => math.abs(stun.end.gameTime - time.gameTime) < 0.07)
            .foreach(_ => _stun.remove(stunnedId))
      }
    }

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
