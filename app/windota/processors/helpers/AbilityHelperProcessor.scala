package windota.processors.helpers

import skadistats.clarity.event.Insert
import skadistats.clarity.model.Entity
import skadistats.clarity.processor.entities.Entities
import skadistats.clarity.processor.runner.Context
import skadistats.clarity.processor.stringtables.{StringTables, UsesStringTable}
import windota.Util
import windota.extensions._

@UsesStringTable("EntityNames")
class AbilityHelperProcessor {
  @Insert
  private val ctx: Context = null

  @Insert
  protected val Entities: Entities = null

  def getAbilities(entity: Entity): Seq[Entity] = {
    (0 to 31)
      .flatMap(i => entity.get[Int](s"m_hAbilities.000$i"))
      .filter(_ != Util.NullValue)
      .flatMap(Entities.get)
  }

  def findAbility(entity: Entity, name: String, shouldBeLearned: Boolean): Option[Entity] = {
    val stringTable = ctx.getProcessor(classOf[StringTables]).forName("EntityNames")
    getAbilities(entity)
      .find(ability => ability.getDtClass.getDtName == name ||
        stringTable.getNameByIndex(ability.getProperty[Int]("m_pEntity.m_nameStringableIndex")) == name)
      .filter(ability => !shouldBeLearned || ability.getProperty[Int]("m_iLevel") > 0)
  }

  def findUnusedAbility(hero: Entity, name: String): Option[Entity] = {
    findAbility(hero, name, shouldBeLearned = true)
      .filter(ability => Util.hasEnoughMana(hero, ability))
      .filterNot(Util.isOnCooldown)
  }
}
