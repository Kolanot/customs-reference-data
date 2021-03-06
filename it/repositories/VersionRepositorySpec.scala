package repositories

import java.time.LocalDate
import java.time.LocalDateTime

import base.ItSpecBase
import generators.BaseGenerators
import generators.ModelArbitraryInstances
import models.ListName
import models.MessageInformation
import models.VersionId
import models.VersionInformation
import org.mockito.Mockito
import org.scalacheck.Arbitrary
import org.scalactic.Equality
import org.scalatest.BeforeAndAfterAll
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection
import play.api.inject.bind
import org.mockito.Mockito.when
import models.ApiDataSource.ColDataFeed
import models.ApiDataSource.RefDataFeed
import services.consumption.TimeService

import scala.concurrent.ExecutionContext.Implicits.global

class VersionRepositorySpec
    extends ItSpecBase
    with BaseGenerators
    with ModelArbitraryInstances
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with GuiceOneAppPerSuite
    with FailOnUnindexedQueries {

  override def beforeAll(): Unit = {
    database.flatMap(_.drop()).futureValue
    super.beforeAll()
    started(app).futureValue
  }

  override def beforeEach(): Unit = {
    database
      .flatMap(
        _.collection[JSONCollection](VersionCollection.collectionName)
          .delete()
          .one(Json.obj())
      )
      .futureValue

    Mockito.reset(mockTimeService)

    super.beforeEach()
  }

  override def afterAll(): Unit = {
    database.flatMap(_.drop()).futureValue
    super.afterAll()
  }

  val mockTimeService       = mock[TimeService]

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .overrides(
        bind[TimeService].to(mockTimeService)
      )
      .build()

  "save" - {
    "saves and a version number when the version information is successfully saved" in {

      val repo = app.injector.instanceOf[VersionRepository]

      val messageInformation = Arbitrary.arbitrary[MessageInformation].sample.value
      val listName           = Arbitrary.arbitrary[ListName].sample.value

      val expectedVersionId = VersionId("1")

      when(mockTimeService.now()).thenReturn(LocalDateTime.now())

      val result = repo.save(expectedVersionId, messageInformation, RefDataFeed, Seq(listName)).futureValue

      val expectedVersionInformation = VersionInformation(messageInformation, expectedVersionId, LocalDateTime.now, RefDataFeed, Seq(listName))

      result mustEqual expectedVersionId

      val savedVersionInformation =
        database.flatMap(_.collection[JSONCollection](VersionCollection.collectionName).find(Json.obj(), None).one[VersionInformation]).futureValue.value

      savedVersionInformation mustEqual expectedVersionInformation
    }
  }

  "getLatest with listName filter" - {
    "returns the latest version for a listName by snapshotDate" in {
      val repo = app.injector.instanceOf[VersionRepository]

      val latestSnapshotDate = LocalDate.now()
      val latestCreatedOn    = LocalDateTime.now()

      val oldSnapshotDate = LocalDate.now().minusDays(1)
      val oldCreatedOn    = LocalDateTime.now().minusDays(1)

      when(mockTimeService.now()).thenReturn(oldCreatedOn, latestCreatedOn)

      val messageInformation = Arbitrary.arbitrary[MessageInformation].sample.value
      val listName           = Arbitrary.arbitrary[ListName].sample.value

      repo.save(VersionId("1"), messageInformation.copy(snapshotDate = oldSnapshotDate), RefDataFeed, Seq(listName)).futureValue
      repo.save(VersionId("2"), messageInformation.copy(snapshotDate = latestSnapshotDate), RefDataFeed, Seq(listName)).futureValue

      val expectedVersionInformation =
        VersionInformation(messageInformation.copy(snapshotDate = latestSnapshotDate), VersionId("2"), latestCreatedOn, RefDataFeed, Seq(listName))

      val result: VersionInformation = repo.getLatest(listName).futureValue.value

      result mustEqual expectedVersionInformation
    }

    "returns the latest version for a listName when the is a newer version that it does not belong to" in {
      val repo = app.injector.instanceOf[VersionRepository]

      val snapshotDate    = LocalDate.now()
      val latestCreatedOn = LocalDateTime.now()
      val oldCreatedOn    = LocalDateTime.now().minusDays(1)

      when(mockTimeService.now()).thenReturn(oldCreatedOn, latestCreatedOn)

      val messageInformation = Arbitrary.arbitrary[MessageInformation].sample.value
      val listName1          = ListName("1")
      val listName2          = ListName("2")

      repo.save(VersionId("1"), messageInformation.copy(snapshotDate = snapshotDate), RefDataFeed, Seq(listName1)).futureValue
      repo.save(VersionId("2"), messageInformation.copy(snapshotDate = snapshotDate), ColDataFeed, Seq(listName2)).futureValue

      val expectedVersionInformation =
        VersionInformation(messageInformation.copy(snapshotDate = snapshotDate), VersionId("1"), latestCreatedOn, RefDataFeed, Seq(listName1))

      val result: VersionInformation = repo.getLatest(listName1).futureValue.value

      result mustEqual expectedVersionInformation
    }
  }

  "getLatestListNames" - {
    "returns listnames for the latest REF and COL by snapshotDate" in {
      val repo = app.injector.instanceOf[VersionRepository]

      val newSnapshotDate      = LocalDate.now()
      val newMessageInfomation = MessageInformation("messageId", newSnapshotDate)
      val oldMessageInfomation = MessageInformation("messageId", newSnapshotDate.minusDays(1))

      when(mockTimeService.now())
        .thenReturn(LocalDateTime.now().minusDays(1))
        .thenReturn(LocalDateTime.now().minusDays(1))
        .thenReturn(LocalDateTime.now())

      val listNames1 = Seq(ListName("a"), ListName("b"))
      val listNames2 = Seq(ListName("1"), ListName("2"))
      val listNames3 = Seq(ListName("1.1"), ListName("2.1"))

      repo.save(VersionId("1"), oldMessageInfomation, ColDataFeed, listNames1).futureValue
      repo.save(VersionId("2"), oldMessageInfomation, RefDataFeed, listNames2).futureValue
      repo.save(VersionId("3"), newMessageInfomation, RefDataFeed, listNames3).futureValue

      val result         = repo.getLatestListNames().futureValue
      val expectedResult = listNames1 ++ listNames3

      result must contain theSameElementsAs expectedResult
    }
  }

  implicit val versionInformationEquality: Equality[VersionInformation] =
    (a, b) =>
      b match {
        case VersionInformation(mi, ver, _, sc, ln) =>
          (a.messageInformation == mi) && (a.versionId == ver) && (a.source == sc) && (a.listNames == ln)
        case _ => false
      }

}
