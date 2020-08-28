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

import play.api.libs.json.OWrites
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class GenericListItem(listName: ListName, messageInformation: MessageInformation, data: JsObject)

object GenericListItem {

  implicit val writes: OWrites[GenericListItem] =
    (__.write[ListName] and
      __.write[MessageInformation] and
      (__ \ "data").write[JsObject])(unlift(GenericListItem.unapply))

  implicit val readers: Reads[GenericListItem] =
    (__.read[ListName] and
      __.read[MessageInformation] and
      (__ \ "data").read[JsObject])(GenericListItem(_, _, _))
}