/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.consumption

import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.ControllerComponents
import services.consumption.ListRetrievalService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

class ResourceLinksController @Inject() (
  cc: ControllerComponents,
  listRetrievalService: ListRetrievalService
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  private val logger: Logger = Logger("ResourceLinksController")

  def get: Action[AnyContent] =
    Action.async {
      implicit request =>
        listRetrievalService.getResourceLinks().map {
          case Some(resourceLinks) =>
            Ok(Json.toJsObject(resourceLinks))
          case None =>
            logger.error("Empty resource links list")
            InternalServerError
        }
    }
}
