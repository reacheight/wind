package windota.processors

import skadistats.clarity.event.Insert
import skadistats.clarity.processor.entities.{Entities, UsesEntities}
import skadistats.clarity.processor.runner.Context
import windota.processors.helpers._

@UsesEntities
class ProcessorBase {
  @Insert
  private val ctx: Context = null

  protected lazy val AbilitiesHelper: AbilitiesHelperProcessor = ctx.getProcessor(classOf[AbilitiesHelperProcessor])
  protected lazy val ItemsHelper: ItemsHelperProcessor = ctx.getProcessor(classOf[ItemsHelperProcessor])
  protected lazy val GameTimeHelper: GameTimeHelperProcessor = ctx.getProcessor(classOf[GameTimeHelperProcessor])
  protected lazy val HeroProcessor: HeroProcessor = ctx.getProcessor(classOf[HeroProcessor])

  @Insert
  protected val Entities: Entities = null
}
