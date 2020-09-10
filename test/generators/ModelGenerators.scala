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

package generators

import models.MessageInformation
import models.ReferenceDataPayload
import org.scalacheck.Gen
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.Json

trait ModelGenerators {
  self: BaseGenerators with JavaTimeGenerators =>

  val genSimpleJsString: Gen[JsString] = extendedAsciiWithMaxLength(100).map(JsString)

  val genSimpleJsObject: Gen[JsObject] =
    for {
      key   <- extendedAsciiWithMaxLength(100)
      value <- genSimpleJsString
    } yield Json.obj(key -> value)

  def genReferenceList(numberOfLists: Int = 5, dataItemsGen: Option[Gen[JsObject]] = None, listNameGen: Option[Gen[String]] = None): Gen[JsObject] =
    for {
      listNames <- listNameGen.getOrElse(extendedAsciiWithMaxLength(100))
      listItems <- Gen.listOfN(numberOfLists, dataItemsGen.getOrElse(genSimpleJsObject))
    } yield Json.obj(
      listNames -> Json.obj(
        "listName"    -> listNames,
        "listEntries" -> listItems
      )
    )

  /**
    * Generator that sample full reference data push as JSON
    *
    * @param numberOfLists The number of lists for the sample reference data json
    * @param numberOfListItems The number of list items to populate the list's listEntries with
    * @param messageInformation The metadata for the reference data. This allows the caller to override the default random values and specify their own
    * @return A [[play.api.libs.json.JsObject]] that represents a full reference data push
    */
  def genReferenceDataJson(
    numberOfLists: Int = 5,
    numberOfListItems: Int = 5,
    messageInformation: Option[Gen[MessageInformation]] = None,
    dataItemsGen: Option[Gen[JsObject]] = None
  ): Gen[JsObject] = {
    import generators.ModelArbitraryInstances.arbitraryMessageInformation

    require(numberOfLists >= 1, "Number of lists should be greater than 1")

    // This is used to ensure that there are no collisions in the listNames so that we generate the specified number of lists.
    val suffix: Iterator[String] = Iterator.from(0, 1).map(_.toString)

    val jsObjGen2: Gen[JsObject] = {
      val listNameGen: Gen[String] = extendedAsciiWithMaxLength(100).map(_ ++ suffix.next())
      genReferenceList(numberOfListItems, dataItemsGen, listNameGen = Some(listNameGen))
    }

    for {
      messageInformation <- messageInformation.getOrElse(arbitraryMessageInformation.arbitrary)
      lists              <- Gen.listOfN(numberOfLists, jsObjGen2)
    } yield Json.obj(
      "messageInformation" -> Json.obj(
        "messageID"    -> messageInformation.messageId,
        "snapshotDate" -> messageInformation.snapshotDate
      ),
      "lists" -> lists.foldLeft(Json.obj())(_ ++ _)
    )
  }

  def genReferenceDataPayload(numberOfLists: Int = 5, numberOfListItems: Int = 5, dataItemsGen: Option[Gen[JsObject]] = None): Gen[ReferenceDataPayload] =
    genReferenceDataJson(numberOfLists, numberOfListItems, dataItemsGen = dataItemsGen)
      .map(ReferenceDataPayload(_))

}

object ModelGenerators extends ModelGenerators with BaseGenerators with JavaTimeGenerators
