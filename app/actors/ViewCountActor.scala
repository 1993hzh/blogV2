package actors

import akka.actor.Actor
import controllers.Application
import dao.PassageDAO
import play.api.Logger
import play.api.cache.Cache
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

import scala.collection.mutable.{Set => Mset}
import scala.util.{Failure, Success}

/**
 * Created by Leo.
 * 2015/11/15 1:58
 */
class ViewCountActor extends Actor {

  lazy val log = Logger
  lazy val passageDAO = PassageDAO()

  override def receive: Receive = {
    case isSyncUp: Boolean if isSyncUp =>
      log.info(Application.now + ": Receive do viewCountSyncUp message.")
      syncUp
  }

  private def syncUp() = {
    val set = getPassageViewCountSetFromCache

    set.foreach(id => {
      getViewCountFromCache(id) match {
        case Some(vc) =>
          doSyncUp(id, vc)
        case None => log("passageId: " + id + " not found in Cache.")
      }
    })

    if (set.isEmpty)
      log.info("No passage need to doSyncUp.")
  }

  private def doSyncUp(passageId: Int, viewCount: Int) = {
    passageDAO.updateViewCount(passageId, viewCount) onComplete {
      case Success(r) =>
        //remove cache "passage-id-`id` -> viewCount"
        Cache.remove(Application.PASSAGE_VIEW_COUNT_PREFIX + passageId)

        //remove passageId from cache PASSAGE_BEEN_READ_LIST, reset this cache
        val set = getPassageViewCountSetFromCache
        set.remove(passageId)
        setPassageViewCountSetFromCache(set)

        log.info(Application.now + ": passage: " + passageId + " doSyncUp succeed.")
      case Failure(f) =>
        log.warn("passage: " + passageId + " doSyncUp failed due to: " + f.getLocalizedMessage)
    }
  }

  private def getViewCountFromCache(id: Int): Option[Int] = {
    Cache.getAs[Int](Application.PASSAGE_VIEW_COUNT_PREFIX + id)
  }

  private def getPassageViewCountSetFromCache: Mset[Int] = {
    Cache.getOrElse[Mset[Int]](Application.PASSAGE_BEEN_READ_LIST)(Mset.empty[Int])
  }

  private def setPassageViewCountSetFromCache(set: Mset[Int]): Unit = {
    Cache.set(Application.PASSAGE_BEEN_READ_LIST, set)
  }
}
