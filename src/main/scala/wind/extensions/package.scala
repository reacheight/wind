package wind

import skadistats.clarity.model.Entity
import skadistats.clarity.processor.entities.Entities

import scala.collection.mutable.ListBuffer

package object extensions {
  implicit class EntitiesExtension(val entities: Entities) {
    def get(predicate: Entity => Boolean) = Option(entities.getByPredicate(e => predicate(e)))
    def getAll(predicate: Entity => Boolean) = toList(entities.getAllByPredicate(e => predicate(e)))
    def getAllByName(className: String) = toList(entities.getAllByDtName(className))
  }

  private def toList[T](iterator: java.util.Iterator[T]): List[T] = {
    val result = ListBuffer.empty[T]
    iterator.forEachRemaining(i => { result += i })
    result.toList
  }
}
