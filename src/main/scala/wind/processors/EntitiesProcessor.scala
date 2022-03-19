package wind.processors

import skadistats.clarity.event.Insert
import skadistats.clarity.processor.entities.{Entities, UsesEntities}

@UsesEntities
class EntitiesProcessor {
  @Insert
  protected val Entities: Entities = null
}
