package wind

import skadistats.clarity.model.Entity
import skadistats.clarity.processor.entities.Entities

import scala.collection.mutable.ListBuffer

package object extensions {
  implicit class EntitiesExtension(val entities: Entities) {
    def get(handle: Int) = Option(entities.getByHandle(handle))
    def find(predicate: Entity => Boolean) = Option(entities.getByPredicate(e => predicate(e)))
    def filter(predicate: Entity => Boolean) = toList(entities.getAllByPredicate(e => predicate(e)))
    def findByName(className: String) = Option(entities.getByDtName(className))
    def filterByName(className: String) = toList(entities.getAllByDtName(className))
  }

  implicit class EntityExtension(val entity: Entity) {
    def getProperty[T](propertyName: String) = Option(entity.getProperty[T](propertyName))
  }

  private def toList[T](iterator: java.util.Iterator[T]): List[T] = {
    val result = ListBuffer.empty[T]
    iterator.forEachRemaining(i => { result += i })
    result.toList
  }
}
