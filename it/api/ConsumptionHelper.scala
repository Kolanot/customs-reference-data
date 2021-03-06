package api

import com.typesafe.config.ConfigFactory
import models.{GenericListItem, ListName, MessageInformation, VersionId}
import play.api.libs.json.{JsObject, Json}

import java.time.LocalDate

trait ConsumptionHelper {

  val defaultSnapshotDate: LocalDate = LocalDate.of(2015, 12, 12)
  val defaultMessageId: String = "message-id-default"

  val defaultMessageInformation:  MessageInformation = MessageInformation(defaultMessageId, defaultSnapshotDate)

  val defaultListName: ListName = ListName("UnodLanguages")

  val firstDefaultDataItem: JsObject = Json.obj("state" -> "default", "activeFrom" -> "2015-12-12", "code" -> "CS", "description" -> Json.arr())
  val secondDefaultDataItem: JsObject = Json.obj("state" -> "default", "activeFrom" -> "2015-12-12", "code" -> "DE", "description" -> Json.arr())

  val defaultData: Seq[JsObject] = Seq(firstDefaultDataItem, secondDefaultDataItem)

  def  getListItem(versionId: VersionId, data: JsObject): GenericListItem = GenericListItem(
    defaultListName,
    defaultMessageInformation,
    versionId,
    data
  )

  def  basicList(versionId: VersionId): Seq[GenericListItem] = Seq(
    getListItem(versionId, firstDefaultDataItem),
    getListItem(versionId, secondDefaultDataItem)
  )
}
