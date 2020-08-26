package repositories

import java.time.LocalDate

import base.ItSpecBase
import models.ListName
import models.MetaData
import org.scalatest.BeforeAndAfterAll
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.IntegrationPatience
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import reactivemongo.api.DefaultDB
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListRepositorySpec extends ItSpecBase with MongoSuite with IntegrationPatience with BeforeAndAfterEach with BeforeAndAfterAll with GuiceOneAppPerSuite {

  import ListRepositorySpec._

  override def beforeEach(): Unit = {
    database.flatMap(_.collection[JSONCollection](ListRepository.collectionName).drop(failIfNotFound = false))
    super.beforeEach()
  }

  override def afterAll(): Unit = {
    database.flatMap(_.collection[JSONCollection](ListRepository.collectionName).drop(failIfNotFound = false))
    super.afterAll()
  }

  def seedData(database: Future[DefaultDB], data: Seq[JsObject]): Unit =
    database.flatMap {
      _.collection[JSONCollection](ListRepository.collectionName)
        .insert(ordered = true)
        .many(data)
    }.futureValue

  "getList" - {

    "must handle" - {

      "single record found" in {

        seedData(database, Seq(sampleDataSet1))

        val repository = app.injector.instanceOf[ListRepository]
        val result     = repository.getList(ListName("AdditionalInformationIdCommon"), MetaData("", LocalDate.now))
        result.futureValue mustBe List(sampleDataSet1)
      }

      "multiple records found" in {

        seedData(database, Seq(sampleDataSet1, sampleDataSet2))

        val repository = app.injector.instanceOf[ListRepository]
        val result     = repository.getList(ListName("AdditionalInformationIdCommon"), MetaData("", LocalDate.now))
        result.futureValue mustBe List(sampleDataSet1, sampleDataSet2)
      }

      "no records found" in {

        val repository = app.injector.instanceOf[ListRepository]
        val result     = repository.getList(ListName("AdditionalInformationIdCommon"), MetaData("", LocalDate.now))

        result.futureValue mustBe Nil
      }

    }

  }

}

object ListRepositorySpec {

  val id1: BSONObjectID = BSONObjectID.generate()
  val id2: BSONObjectID = BSONObjectID.generate()

  val sampleDataSet1: JsObject =
    Json.obj(
      "_id"        -> id1.toString(),
      "listName"   -> "AdditionalInformationIdCommon",
      "snapshotId" -> "snapshot",
      "state"      -> "valid",
      "activeFrom" -> "2020-01-18",
      "code"       -> "00100",
      "remark"     -> "foo",
      "description" ->
        Json.obj(
          "en" -> "Simplified authorisation"
        )
    )

  val sampleDataSet2: JsObject =
    Json.obj(
      "_id"        -> id2.toString(),
      "listName"   -> "AdditionalInformationIdCommon",
      "snapshotId" -> "snapshot",
      "state"      -> "valid",
      "activeFrom" -> "2020-01-18",
      "code"       -> "00100",
      "remark"     -> "foo",
      "description" ->
        Json.obj(
          "en" -> "Simplified authorisation"
        )
    )
}
