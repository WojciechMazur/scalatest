/*
 * Copyright 2001-2024 Artima, Inc.
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
package org.scalatest

import org.scalatest.prop.PropertyChecks
import org.scalatest.exceptions.TestFailedException
import org.scalactic.Prettifier
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers._

class ShouldBeAnySpec extends AnyFunSpec with PropertyChecks with ReturnsNormallyThrowsAssertion {

  // Checking for equality with "be"
  describe("The be token") {

    it("should compare arrays structurally") {
      Array(1, 2) should be (Array(1, 2))
    }

    it("should call .deep on an array in either left or ride sides") {
      Array(1, 2) should be (List(1, 2))
      List(1, 2) should be (Array(1, 2))
    }

    it("should do nothing when equal") {
      1 should be (1)
      // 1 shouldBe 1

      // objects should equal themselves
      forAll((s: String) => s should be (s))
      forAll((i: Int) => i should be (i))
      
      // a string should equal another string with the same value
      forAll((s: String) => s should be (new String(s)))
    }

    it("should do nothing when not equal and used with not") {
      1 should not { be (2) }
      1 should not be (2)

      // unequal objects should not equal each other
      forAll((s: String, t: String) => if (s != t) s should not { be (t) } else succeed)
      forAll((s: String, t: String) => if (s != t) s should not be (t) else succeed)
    }

    it("should do nothing when equal and used in a logical-and expression") {
      1 should (be (1) and be (2 - 1))
    }

    it("should do nothing when equal and used in multi-part logical expressions") {

        // Just to make sure these work strung together
        1 should (be (1) and be (1) and be (1) and be (1))
        1 should (be (1) and be (1) or be (1) and be (1) or be (1))
        1 should (
            be (1) and
            be (1) or
            be (1) and
            be (1) or
            be (1)
        )
    }

    it("should do nothing when equal and used in a logical-or expression") {
      1 should { be (1) or be (2 - 1) }
    }

    it("should do nothing when not equal and used in a logical-and expression with not") {
      1 should { not { be (2) } and not { be (3 - 1) }}
      1 should { not be (2) and (not be (3 - 1)) }
      1 should (not be (2) and not be (3 - 1))
    }

    it("should do nothing when not equal and used in a logical-or expression with not") {
      1 should { not { be (2) } or not { be (3 - 1) }}
      1 should { not be (2) or (not be (3 - 1)) }
      1 should (not be (2) or not be (3 - 1))
    }

    it("should throw an assertion error when not equal") {
      val caught1 = intercept[TestFailedException] {
        1 should be (2)
      }
      assert(caught1.getMessage === "1 was not equal to 2")

      // unequal objects used with "a should equal (b)" should throw an TestFailedException
      forAll((s: String, t: String) => if (s != t) assertThrows[TestFailedException](s should be (t)) else succeed)

      val caught2 = intercept[TestFailedException] {
        1 should not (not be (2))
      }
      assert(caught2.getMessage === "1 was not equal to 2")

      val s: String = null
      val caught3 = intercept[TestFailedException] {
        s should be ("hi")
      }
      assert(caught3.getMessage === "null was not equal to \"hi\"")
    }

    it("should throw an assertion error when equal but used with should not") {
      val caught1 = intercept[TestFailedException] {
        1 should not { be (1) }
      }
      assert(caught1.getMessage === "1 was equal to 1")

      val caught2 = intercept[TestFailedException] {
        1 should not be (1)
      }
      assert(caught2.getMessage === "1 was equal to 1")

      // the same object used with "a should not { equal (a) } should throw TestFailedException
      forAll((s: String) => assertThrows[TestFailedException](s should not { be (s) }))
      forAll((i: Int) => assertThrows[TestFailedException](i should not { be (i) }))
      forAll((s: String) => assertThrows[TestFailedException](s should not be (s)))
      forAll((i: Int) => assertThrows[TestFailedException](i should not be (i)))

      // two different strings with the same value used with "s should not { be (t) } should throw TestFailedException
      forAll((s: String) => assertThrows[TestFailedException](s should not { be (new String(s)) }))
      forAll((s: String) => assertThrows[TestFailedException](s should not be (new String(s))))

      val caught3 = intercept[TestFailedException] {
        1 should not (not (not be (1)))
      }
      assert(caught3.getMessage === "1 was equal to 1")
    }

    it("should throw an assertion error when not equal and used in a logical-and expression") {
      val caught = intercept[TestFailedException] {
        1 should { be (5) and be (2 - 1) }
      }
      assert(caught.getMessage === "1 was not equal to 5")
    }

    it("should throw an assertion error when not equal and used in a logical-or expression") {
      val caught = intercept[TestFailedException] {
        1 should { be (5) or be (5 - 1) }
      }
      assert(caught.getMessage === "1 was not equal to 5, and 1 was not equal to 4")
    }

    it("should throw an assertion error when equal and used in a logical-and expression with not") {

      val caught1 = intercept[TestFailedException] {
        1 should { not { be (1) } and not { be (3 - 1) }}
      }
      assert(caught1.getMessage === "1 was equal to 1")

      val caught2 = intercept[TestFailedException] {
        1 should { not be (1) and (not be (3 - 1)) }
      }
      assert(caught2.getMessage === "1 was equal to 1")

      val caught3 = intercept[TestFailedException] {
        1 should (not be (1) and not be (3 - 1))
      }
      assert(caught3.getMessage === "1 was equal to 1")

      val caught4 = intercept[TestFailedException] {
        1 should { not { be (2) } and not { be (1) }}
      }
      assert(caught4.getMessage === "1 was not equal to 2, but 1 was equal to 1")

      val caught5 = intercept[TestFailedException] {
        1 should { not be (2) and (not be (1)) }
      }
      assert(caught5.getMessage === "1 was not equal to 2, but 1 was equal to 1")

      val caught6 = intercept[TestFailedException] {
        1 should (not be (2) and not be (1))
      }
      assert(caught6.getMessage === "1 was not equal to 2, but 1 was equal to 1")
    }

    it("should throw an assertion error when equal and used in a logical-or expression with not") {

      val caught1 = intercept[TestFailedException] {
        1 should { not { be (1) } or not { be (2 - 1) }}
      }
      assert(caught1.getMessage === "1 was equal to 1, and 1 was equal to 1")

      val caught2 = intercept[TestFailedException] {
        1 should { not be (1) or { not be (2 - 1) }}
      }
      assert(caught2.getMessage === "1 was equal to 1, and 1 was equal to 1")

      val caught3 = intercept[TestFailedException] {
        1 should (not be (1) or not be (2 - 1))
      }
      assert(caught3.getMessage === "1 was equal to 1, and 1 was equal to 1")
    }

    it("should use custom implicit Prettifier when it is in scope") {
      implicit val customPrettifier =
        Prettifier {
          case s: String => "!!! " + s + " !!!"
          case other => Prettifier.default(other)
        }

      val e = intercept[TestFailedException] {
      "test 1" should be ("test 2")
      }
      assert(e.message == Some("!!! test [1] !!! was not equal to !!! test [2] !!!"))
    }

    // SKIP-SCALATESTJS,NATIVE-START
    it("should produce TestFailedExceptions that can be serialized") {
      import scala.util.Try
      val result = Try(1 shouldBe 2)
      val baos = new java.io.ByteArrayOutputStream
      val oos = new java.io.ObjectOutputStream(baos)
      oos.writeObject(result) // Should not throw an exeption
    }
    // SKIP-SCALATESTJS,NATIVE-END

    it("should show escaped string in analysis") {
      val a = "\u0000test"
      val b = "test"
      val e = 
        intercept[TestFailedException] {
          a should be (b)
        }
      e.analysis should be (Vector("\"[\\u0000]test\" -> \"[]test\""))
    }
  }
}
