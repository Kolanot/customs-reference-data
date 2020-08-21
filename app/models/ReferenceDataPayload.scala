/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models

import java.time.LocalDate

import play.api.libs.json._

class ReferenceDataPayload(data: JsObject) {
  outer =>

  case class ListName(listName: String)

  case class MessageInformation(messageId: String, snapshotDate: LocalDate)

  case class SingleList(listName: ListName, messageInformation: MessageInformation, list: Seq[JsObject]) {

    lazy val toGenericListItem: Seq[GenericListItem] =
      list.map(GenericListItem(listName, messageInformation, _))
  }

  private lazy val messageInformation: MessageInformation =
    (for {
      JsString(messageId) <- (data \ "messageInformation" \ "messageID").validate[JsString]
      snapshotDate        <- (data \ "messageInformation" \ "snapshotDate").validate[LocalDate]
    } yield MessageInformation(messageId, snapshotDate)).get

  private lazy val lists: JsObject = (data \ "lists").get.as[JsObject]

  def listsNames: collection.Set[ListName] =
    lists.keys.map(outer.ListName)

  def getList(listName: ListName): SingleList =
    SingleList(listName, messageInformation, (lists \ listName.listName \ "listEntries").as[Vector[JsObject]])

}

object ReferenceDataPayload extends MongoDateTimeFormats {

  def apply(data: JsObject): ReferenceDataPayload = new ReferenceDataPayload(data)

  implicit val oWritesListName: OWrites[ReferenceDataPayload#ListName] =
    listName => Json.obj("listName" -> listName.listName)

  implicit val oWritesMessageInformation: OWrites[ReferenceDataPayload#MessageInformation] =
    messageInformation =>
      Json.obj(
        "messageID"    -> messageInformation.messageId,
        "snapshotDate" -> messageInformation.snapshotDate
      )
}
