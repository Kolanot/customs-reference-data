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

package services

import base.SpecBase
import models.OtherError
import org.scalatest.EitherValues
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class DecodingServiceSpec extends SpecBase with ScalaCheckDrivenPropertyChecks with EitherValues {

  "decodeFromBase64" - {

    "must return decoded Array[Byte] when given a valid encoded Array[Byte]" in {

      val testString: String = "Test"

      val encodeArrayByte = encode(testString.getBytes)

      val result         = DecodingService.decodeFromBase64(encodeArrayByte).right.value
      val resultToString = result.map(_.toChar).mkString

      resultToString mustBe testString
    }

    "must return OtherError when given invalid Base64 characters" in {

      val invalidString = "Test string with invalid base64 characters %"

      val result = DecodingService.decodeFromBase64(invalidString.getBytes).left.value

      result mustBe an[OtherError]
    }
  }

}
