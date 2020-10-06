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

import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.Json
import play.api.libs.json.OFormat

case class ResourceLinks(_links: Map[String, JsObject], metaData: MetaData)

object ResourceLinks {

  def apply(listNames: Set[ListName], metaData: MetaData): ResourceLinks =
    new ResourceLinks(linkFormatter(listNames), metaData)

  private def linkFormatter(listNames: Set[ListName]): Map[String, JsObject] = {

    val buildUri: String => String =
      uri => s"/customs-reference-data/$uri"

    //TODO fix ordering here
    val resourceLinks: Seq[Map[String, JsObject]] = listNames.zipWithIndex.map {
      case (listName, index) => Map(s"list${index + 1}" -> JsObject(Seq("href" -> JsString(buildUri(listName.listName)))))
    }.toSeq

    Map("self" -> JsObject(Seq("href" -> JsString(buildUri("lists"))))) ++
      resourceLinks.flatten
  }

  implicit val formats: OFormat[ResourceLinks] = Json.format[ResourceLinks]
}
