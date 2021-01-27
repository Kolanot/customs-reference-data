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

package models

import java.io.FileNotFoundException
import java.nio.file.Path

import org.leadpony.justify.api.JsonSchema
import org.leadpony.justify.api.JsonValidationService
import play.api.Environment

trait JsonSchemaProvider {

  def schema: JsonSchema

}

abstract class SimpleJsonSchemaProvider(env: Environment, jsonValidationService: JsonValidationService)(path: String) extends JsonSchemaProvider {

  val schema: JsonSchema =
    env
      .resourceAsStream(path)
      .fold(throw new FileNotFoundException(s"File for schema: ${getClass.getSimpleName} not find file at path $path"))(jsonValidationService.readSchema)
}
