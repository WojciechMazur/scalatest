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

import org.scalactic.Equality
import org.scalactic.Uniformity
import org.scalactic.Prettifier
import org.scalactic.StringNormalizations._
import SharedHelpers._
import FailureMessages.decorateToStringValue
import scala.collection.mutable.ListBuffer
import exceptions.TestFailedException
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class ListShouldContainInOrderSpec extends AnyFunSpec with Matchers {

  private val prettifier = Prettifier.default

  private def upperCase(value: Any): Any = 
    value match {
      case l: List[_] => l.map(upperCase(_))
      case s: String => s.toUpperCase
      case c: Char => c.toString.toUpperCase.charAt(0)
      case (s1: String, s2: String) => (s1.toUpperCase, s2.toUpperCase)
      case e: java.util.Map.Entry[_, _] => 
        (e.getKey, e.getValue) match {
          case (k: String, v: String) => Entry(k.toUpperCase, v.toUpperCase)
          case _ => value
        }
      case _ => value
    }

  val upperCaseStringEquality =
    new Equality[String] {
      def areEqual(a: String, b: Any): Boolean = upperCase(a) == upperCase(b)
    }

  //ADDITIONAL//

  describe("a List") {

    val fumList: List[String] = List("fex", "fum", "fum", "foe", "fie", "fie", "fie", "fee", "fee")
    val toList: List[String] = List("happy", "happy", "happy", "birthday", "to", "you", "too")
    val ecList: List[String] = List("\u0000feex", "fum", "fum", "foe", "fie", "fie", "fie", "fee", "fee")

    describe("when used with contain inOrder (..)") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        fumList should contain inOrder ("fum", "foe", "fie", "fee")
        val e1 = intercept[TestFailedException] {
          fumList should contain inOrder ("fee", "fie", "foe", "fum")
        }
        e1.failedCodeFileName.get should be ("ListShouldContainInOrderSpec.scala")
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message.get should be (Resources.didNotContainAllOfElementsInOrder(decorateToStringValue(prettifier, fumList), "\"fee\", \"fie\", \"foe\", \"fum\""))
      }
      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality
        fumList should contain inOrder ("FUM", "FOE", "FIE", "FEE")
        intercept[TestFailedException] {
          fumList should contain inOrder ("fee", "fie", "foe", "fum")
        }
      }
      it("should use an explicitly provided Equality") {
        (fumList should contain inOrder ("FUM", "FOE", "FIE", "FEE")) (decided by upperCaseStringEquality)
        intercept[TestFailedException] {
          (fumList should contain inOrder ("fee", "fie", "foe", "fum")) (decided by upperCaseStringEquality)
        }
        intercept[TestFailedException] {
          fumList should contain inOrder (" FUM ", " FOE ", " FIE ", " FEE ")
        }
        (fumList should contain inOrder (" FUM ", " FOE ", " FIE ", " FEE ")) (after being lowerCased and trimmed)
      }
      it("should throw NotAllowedException with correct stack depth and message when RHS contain duplicated value") {
        val e1 = intercept[exceptions.NotAllowedException] {
          fumList should contain inOrder ("fee", "fie", "foe", "fie", "fum")
        }
        e1.failedCodeFileName.get should be ("ListShouldContainInOrderSpec.scala")
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some(Resources.inOrderDuplicate))
      }
      it("should pass when evaluating List(5, 2, 5) should contain inOrder (5, 2)") {
        List(5, 2, 5) should contain inOrder (5, 2)
      }
      it("should pass when evaluating List(1, 2, 3, 4, 5) should contain inOrder (1, 3, 5)") {
        List(1, 2, 3, 4, 5) should contain inOrder (1, 3, 5)
      }
      it("should fail when evaluating " + List(1, 2, 3) + " should contain inOrder (1, 2, 8)") {
        val e1 = intercept[TestFailedException] {
          List(1, 2, 3) should contain inOrder (1, 2, 8)
        }
      }
      it("should fail when evaluating List(0, 1, 2, 2, 99, 3, 3, 3, 5) should contain inOrder (1, 3, 99)") {
        val e1 = intercept[TestFailedException] {
          List(0, 1, 2, 2, 99, 3, 3, 3, 5) should contain inOrder (1, 3, 99)
        }
      }
      it("should throw TestFailedException with analysis showing escaped string") {
        val e1 = intercept[exceptions.TestFailedException] {
          ecList should contain inOrder ("fee", "fie", "foe", "fum")
        }
        e1.analysis should be (Vector("LHS contains at least one string with characters that might cause problem, the escaped string: " + prettifier(escapedString("\u0000feex"))))
      }
    }

    describe("when used with (contain inOrder (..))") {
      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        fumList should (contain inOrder ("fum", "foe", "fie", "fee"))
        val e1 = intercept[TestFailedException] {
          fumList should (contain inOrder ("fee", "fie", "foe", "fum"))
        }
        e1.failedCodeFileName.get should be ("ListShouldContainInOrderSpec.scala")
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message.get should be (Resources.didNotContainAllOfElementsInOrder(decorateToStringValue(prettifier, fumList),  "\"fee\", \"fie\", \"foe\", \"fum\""))
      }
      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality
        fumList should (contain inOrder ("FUM", "FOE", "FIE", "FEE"))
        intercept[TestFailedException] {
          fumList should (contain inOrder ("fee", "fie", "foe", "fum"))
        }
      }
      it("should use an explicitly provided Equality") {
        (fumList should (contain inOrder ("FUM", "FOE", "FIE", "FEE"))) (decided by upperCaseStringEquality)
        intercept[TestFailedException] {
          (fumList should (contain inOrder ("fee", "fie", "foe", "fum"))) (decided by upperCaseStringEquality)
        }
        intercept[TestFailedException] {
          fumList should (contain inOrder (" FUM ", " FOE ", " FIE ", " FEE "))
        }
        (fumList should (contain inOrder (" FUM ", " FOE ", " FIE ", " FEE "))) (after being lowerCased and trimmed)
      }
      it("should throw NotAllowedException with correct stack depth and message when RHS contain duplicated value") {
        val e1 = intercept[exceptions.NotAllowedException] {
          fumList should (contain inOrder ("fee", "fie", "foe", "fie", "fum"))
        }
        e1.failedCodeFileName.get should be ("ListShouldContainInOrderSpec.scala")
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some(Resources.inOrderDuplicate))
      }
      it("should throw TestFailedException with analysis showing escaped string") {
        val e1 = intercept[exceptions.TestFailedException] {
          ecList should (contain inOrder ("fee", "fie", "foe", "fum"))
        }
        e1.analysis should be (Vector("LHS contains at least one string with characters that might cause problem, the escaped string: " + prettifier(escapedString("\u0000feex"))))
      }
    }

    describe("when used with not contain inOrder (..)") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        toList should not contain inOrder ("you", "to", "birthday", "happy")
        val e1 = intercept[TestFailedException] {
          toList should not contain inOrder ("happy", "birthday", "to", "you")
        }
        e1.failedCodeFileName.get should be ("ListShouldContainInOrderSpec.scala")
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message.get should be (Resources.containedAllOfElementsInOrder(decorateToStringValue(prettifier, toList), "\"happy\", \"birthday\", \"to\", \"you\""))
      }
      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality
        toList should not contain inOrder ("YOU", "TO", "BIRTHDAY", "HAPPY")
        intercept[TestFailedException] {
          toList should not contain inOrder ("HAPPY", "BIRTHDAY", "TO", "YOU")
        }
      }
      it("should use an explicitly provided Equality") {
        (toList should not contain inOrder ("YOU", "TO", "BIRTHDAY", "HAPPY")) (decided by upperCaseStringEquality)
        intercept[TestFailedException] {
          (toList should not contain inOrder ("HAPPY", "BIRTHDAY", "TO", "YOU")) (decided by upperCaseStringEquality)
        }
        toList should not contain inOrder (" HAPPY ", " BIRTHDAY ", " TO ", " YOU ")
        intercept[TestFailedException] {
          (toList should not contain inOrder (" HAPPY ", " BIRTHDAY ", " TO ", " YOU ")) (after being lowerCased and trimmed)
        }
      }
      it("should throw NotAllowedException with correct stack depth and message when RHS contain duplicated value") {
        val e1 = intercept[exceptions.NotAllowedException] {
          toList should not contain inOrder ("fee", "fie", "foe", "fie", "fum")
        }
        e1.failedCodeFileName.get should be ("ListShouldContainInOrderSpec.scala")
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some(Resources.inOrderDuplicate))
      }
    }

    describe("when used with (not contain inOrder (..))") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        toList should (not contain inOrder ("you", "to", "birthday", "happy"))
        val e1 = intercept[TestFailedException] {
          toList should (not contain inOrder ("happy", "birthday", "to", "you"))
        }
        e1.failedCodeFileName.get should be ("ListShouldContainInOrderSpec.scala")
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message.get should be (Resources.containedAllOfElementsInOrder(decorateToStringValue(prettifier, toList), "\"happy\", \"birthday\", \"to\", \"you\""))
      }
      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality
        toList should (not contain inOrder ("YOU", "TO", "BIRTHDAY", "HAPPY"))
        intercept[TestFailedException] {
          toList should (not contain inOrder ("HAPPY", "BIRTHDAY", "TO", "YOU"))
        }
      }
      it("should use an explicitly provided Equality") {
        (toList should (not contain inOrder ("YOU", "TO", "BIRTHDAY", "HAPPY"))) (decided by upperCaseStringEquality)
        intercept[TestFailedException] {
          (toList should (not contain inOrder ("HAPPY", "BIRTHDAY", "TO", "YOU"))) (decided by upperCaseStringEquality)
        }
        toList should (not contain inOrder (" HAPPY ", " BIRTHDAY ", " TO ", " YOU "))
        intercept[TestFailedException] {
          (toList should (not contain inOrder (" HAPPY ", " BIRTHDAY ", " TO ", " YOU "))) (after being lowerCased and trimmed)
        }
      }
      it("should throw NotAllowedException with correct stack depth and message when RHS contain duplicated value") {
        val e1 = intercept[exceptions.NotAllowedException] {
          toList should (not contain inOrder ("fee", "fie", "foe", "fie", "fum"))
        }
        e1.failedCodeFileName.get should be ("ListShouldContainInOrderSpec.scala")
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some(Resources.inOrderDuplicate))
      }
    }
    
    describe("when used with shouldNot contain inOrder (..)") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        toList shouldNot contain inOrder ("you", "to", "birthday", "happy")
        val e1 = intercept[TestFailedException] {
          toList shouldNot contain inOrder ("happy", "birthday", "to", "you")
        }
        e1.failedCodeFileName.get should be ("ListShouldContainInOrderSpec.scala")
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message.get should be (Resources.containedAllOfElementsInOrder(decorateToStringValue(prettifier, toList), "\"happy\", \"birthday\", \"to\", \"you\""))
      }
      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality
        toList shouldNot contain inOrder ("YOU", "TO", "BIRTHDAY", "HAPPY")
        intercept[TestFailedException] {
          toList shouldNot contain inOrder ("HAPPY", "BIRTHDAY", "TO", "YOU")
        }
      }
      it("should use an explicitly provided Equality") {
        (toList shouldNot contain inOrder ("YOU", "TO", "BIRTHDAY", "HAPPY")) (decided by upperCaseStringEquality)
        intercept[TestFailedException] {
          (toList shouldNot contain inOrder ("HAPPY", "BIRTHDAY", "TO", "YOU")) (decided by upperCaseStringEquality)
        }
        toList shouldNot contain inOrder (" HAPPY ", " BIRTHDAY ", " TO ", " YOU ")
        intercept[TestFailedException] {
          (toList shouldNot contain inOrder (" HAPPY ", " BIRTHDAY ", " TO ", " YOU ")) (after being lowerCased and trimmed)
        }
      }
      it("should throw NotAllowedException with correct stack depth and message when RHS contain duplicated value") {
        val e1 = intercept[exceptions.NotAllowedException] {
          toList shouldNot contain inOrder ("fee", "fie", "foe", "fie", "fum")
        }
        e1.failedCodeFileName.get should be ("ListShouldContainInOrderSpec.scala")
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some(Resources.inOrderDuplicate))
      }
    }

    describe("when used with shouldNot (contain inOrder (..))") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        toList shouldNot (contain inOrder ("you", "to", "birthday", "happy"))
        val e1 = intercept[TestFailedException] {
          toList shouldNot (contain inOrder ("happy", "birthday", "to", "you"))
        }
        e1.failedCodeFileName.get should be ("ListShouldContainInOrderSpec.scala")
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message.get should be (Resources.containedAllOfElementsInOrder(decorateToStringValue(prettifier, toList), "\"happy\", \"birthday\", \"to\", \"you\""))
      }
      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality
        toList shouldNot (contain inOrder ("YOU", "TO", "BIRTHDAY", "HAPPY"))
        intercept[TestFailedException] {
          toList shouldNot (contain inOrder ("HAPPY", "BIRTHDAY", "TO", "YOU"))
        }
      }
      it("should use an explicitly provided Equality") {
        (toList shouldNot (contain inOrder ("YOU", "TO", "BIRTHDAY", "HAPPY"))) (decided by upperCaseStringEquality)
        intercept[TestFailedException] {
          (toList shouldNot (contain inOrder ("HAPPY", "BIRTHDAY", "TO", "YOU"))) (decided by upperCaseStringEquality)
        }
        toList shouldNot (contain inOrder (" HAPPY ", " BIRTHDAY ", " TO ", " YOU "))
        intercept[TestFailedException] {
          (toList shouldNot (contain inOrder (" HAPPY ", " BIRTHDAY ", " TO ", " YOU "))) (after being lowerCased and trimmed)
        }
      }
      it("should throw NotAllowedException with correct stack depth and message when RHS contain duplicated value") {
        val e1 = intercept[exceptions.NotAllowedException] {
          toList shouldNot (contain inOrder ("fee", "fie", "foe", "fie", "fum"))
        }
        e1.failedCodeFileName.get should be ("ListShouldContainInOrderSpec.scala")
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some(Resources.inOrderDuplicate))
      }
    }
  }

  describe("a col of Lists") {

    val list1s: Vector[List[Int]] = Vector( List(0, 1, 2, 2, 3), List(0, 1, 1, 2, 3, 3, 3), List(0, 1, 2, 3))
    val lists: Vector[List[Int]] = Vector( List(0, 1, 2, 2, 3, 3, 3), List(0, 1, 1, 1, 2, 3), List(8, 2, 2, 3, 4))
    val listsNil: Vector[List[Int]] = Vector( List(0, 1, 1, 1, 2, 2, 2, 3, 3, 3), List(0, 1, 2, 2, 3), Nil)
    val hiLists: Vector[List[String]] = Vector( List("hello", "hi", "hi", "he"), List("hello", "hi", "he", "he", "he"), List("hello", "hi", "he"))
    val toLists: Vector[List[String]] = Vector( List("nice", "to", "you"), List("nice", "to", "you"), List("nice", "to", "you"))

    describe("when used with contain inOrder (..)") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        all (list1s) should contain inOrder (1, 2, 3)
        atLeast (2, lists) should contain inOrder (1, 2, 3)
        atMost (2, lists) should contain inOrder (1, 2, 3)
        no (lists) should contain inOrder (3, 4, 5)

        val e1 = intercept[TestFailedException] {
          all (lists) should contain inOrder (1, 2, 3)
        }
        e1.failedCodeFileName.get should be ("ListShouldContainInOrderSpec.scala")
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some("'all' inspection failed, because: \n" +
                                   "  at index 2, " + decorateToStringValue(prettifier, lists(2)) + " did not contain all of " + "(1, 2, 3)" + " in order (ListShouldContainInOrderSpec.scala:" + (thisLineNumber - 5) + ") \n" +
                                   "in " + decorateToStringValue(prettifier, lists)))

        val e3 = intercept[TestFailedException] {
          all (listsNil) should contain inOrder (1, 2, 3)
        }
        e3.failedCodeFileName.get should be ("ListShouldContainInOrderSpec.scala")
        e3.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e3.message should be (Some("'all' inspection failed, because: \n" +
                                   "  at index 2, " + decorateToStringValue(prettifier, listsNil(2)) + " did not contain all of " + "(1, 2, 3)" + " in order (ListShouldContainInOrderSpec.scala:" + (thisLineNumber - 5) + ") \n" +
                                   "in " + decorateToStringValue(prettifier, listsNil)))
      }

      it("should use the implicit Equality in scope") {
        all (hiLists) should contain inOrder ("hi", "he")
        intercept[TestFailedException] {
          all (hiLists) should contain inOrder ("hi", "ho")
        }

        {
          implicit val ise = upperCaseStringEquality
          all (hiLists) should contain inOrder ("HI", "HE")
          intercept[TestFailedException] {
            all (hiLists) should contain inOrder ("HI", "HO")
          }
        }
      }
      it("should use an explicitly provided Equality") {
        (all (hiLists) should contain inOrder ("HI", "HE")) (decided by upperCaseStringEquality)
        intercept[TestFailedException] {
          (all (hiLists) should contain inOrder ("HI", "HO")) (decided by upperCaseStringEquality)
        }
        implicit val ise = upperCaseStringEquality
        (all (hiLists) should contain inOrder ("hi", "he")) (decided by defaultEquality[String])
        intercept[TestFailedException] {
          (all (hiLists) should contain inOrder ("hi", "ho")) (decided by defaultEquality[String])
        }
      }
      it("should throw NotAllowedException with correct stack depth and message when RHS contain duplicated value") {
        val e1 = intercept[exceptions.NotAllowedException] {
          all (list1s) should contain inOrder (1, 2, 2, 3)
        }
        e1.failedCodeFileName.get should be ("ListShouldContainInOrderSpec.scala")
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some(Resources.inOrderDuplicate))
      }
    }

    describe("when used with (contain inOrder (..))") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        all (list1s) should (contain inOrder (1, 2, 3))
        atLeast (2, lists) should (contain inOrder (1, 2, 3))
        atMost (2, lists) should (contain inOrder (1, 2, 3))
        no (lists) should (contain inOrder (3, 4, 5))
        no (listsNil) should (contain inOrder (3, 4, 5))

        val e1 = intercept[TestFailedException] {
          all (lists) should (contain inOrder (1, 2, 3))
        }
        e1.failedCodeFileName.get should be ("ListShouldContainInOrderSpec.scala")
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some("'all' inspection failed, because: \n" +
                                   "  at index 2, " + decorateToStringValue(prettifier, lists(2)) + " did not contain all of " + "(1, 2, 3)" + " in order (ListShouldContainInOrderSpec.scala:" + (thisLineNumber - 5) + ") \n" +
                                   "in " + decorateToStringValue(prettifier, lists)))

        val e4 = intercept[TestFailedException] {
          all (listsNil) should (contain inOrder (1, 2, 3))
        }
        e4.failedCodeFileName.get should be ("ListShouldContainInOrderSpec.scala")
        e4.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e4.message should be (Some("'all' inspection failed, because: \n" +
                                   "  at index 2, " + decorateToStringValue(prettifier, listsNil(2)) + " did not contain all of " + "(1, 2, 3)" + " in order (ListShouldContainInOrderSpec.scala:" + (thisLineNumber - 5) + ") \n" +
                                   "in " + decorateToStringValue(prettifier, listsNil)))
      }

      it("should use the implicit Equality in scope") {
        all (hiLists) should (contain inOrder ("hi", "he"))
        intercept[TestFailedException] {
          all (hiLists) should (contain inOrder ("he", "hi"))
        }

        {
          implicit val ise = upperCaseStringEquality
          all (hiLists) should (contain inOrder ("HI", "HE"))
          intercept[TestFailedException] {
            all (hiLists) should (contain inOrder ("HI", "HO"))
          }
        }
      }
      it("should use an explicitly provided Equality") {
        (all (hiLists) should (contain inOrder ("HI", "HE"))) (decided by upperCaseStringEquality)
        intercept[TestFailedException] {
          (all (hiLists) should (contain inOrder ("HI", "HO"))) (decided by upperCaseStringEquality)
        }
        implicit val ise = upperCaseStringEquality
        (all (hiLists) should (contain inOrder ("hi", "he"))) (decided by defaultEquality[String])
        intercept[TestFailedException] {
          (all (hiLists) should (contain inOrder ("he", "hi"))) (decided by defaultEquality[String])
        }
      }
      it("should throw NotAllowedException with correct stack depth and message when RHS contain duplicated value") {
        val e1 = intercept[exceptions.NotAllowedException] {
          all (list1s) should (contain inOrder (1, 2, 2, 3))
        }
        e1.failedCodeFileName.get should be ("ListShouldContainInOrderSpec.scala")
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some(Resources.inOrderDuplicate))
      }
    }

    describe("when used with not contain inOrder (..)") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        all (toLists) should not contain inOrder ("you", "to")
        val e1 = intercept[TestFailedException] {
          all (toLists) should not contain inOrder ("to", "you")
        }
        e1.failedCodeFileName.get should be ("ListShouldContainInOrderSpec.scala")
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some("'all' inspection failed, because: \n" +
                                   "  at index 0, " + decorateToStringValue(prettifier, toLists(0)) + " contained all of " + "(\"to\", \"you\")" +  " in order (ListShouldContainInOrderSpec.scala:" + (thisLineNumber - 5) + ") \n" +
                                   "in " + decorateToStringValue(prettifier, toLists)))
      }
      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality
        all (toLists) should not contain inOrder ("YOU", "TO")
        intercept[TestFailedException] {
          all (toLists) should not contain inOrder ("TO", "YOU")
        }
      }
      it("should use an explicitly provided Equality") {
        (all (toLists) should not contain inOrder ("YOU", "TO")) (decided by upperCaseStringEquality)
        intercept[TestFailedException] {
          (all (toLists) should not contain inOrder ("TO", "YOU")) (decided by upperCaseStringEquality)
        }
        all (toLists) should not contain inOrder (" TO ", " YOU ")
        intercept[TestFailedException] {
          (all (toLists) should not contain inOrder (" TO ", " YOU ")) (after being lowerCased and trimmed)
        }
      }
      it("should throw NotAllowedException with correct stack depth and message when RHS contain duplicated value") {
        val e1 = intercept[exceptions.NotAllowedException] {
          all (toLists) should not contain inOrder ("fee", "fie", "foe", "fie", "fum")
        }
        e1.failedCodeFileName.get should be ("ListShouldContainInOrderSpec.scala")
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some(Resources.inOrderDuplicate))
      }
    }

    describe("when used with (not contain inOrder (..))") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        all (toLists) should (not contain inOrder ("you", "to"))
        val e1 = intercept[TestFailedException] {
          all (toLists) should (not contain inOrder ("to", "you"))
        }
        e1.failedCodeFileName.get should be ("ListShouldContainInOrderSpec.scala")
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some("'all' inspection failed, because: \n" +
                                   "  at index 0, " + decorateToStringValue(prettifier, toLists(0)) + " contained all of " + "(\"to\", \"you\")" + " in order (ListShouldContainInOrderSpec.scala:" + (thisLineNumber - 5) + ") \n" +
                                   "in " + decorateToStringValue(prettifier, toLists)))
      }
      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality
        all (toLists) should (not contain inOrder ("YOU", "TO"))
        intercept[TestFailedException] {
          all (toLists) should (not contain inOrder ("TO", "YOU"))
        }
      }
      it("should use an explicitly provided Equality") {
        (all (toLists) should (not contain inOrder ("YOU", "TO"))) (decided by upperCaseStringEquality)
        intercept[TestFailedException] {
          (all (toLists) should (not contain inOrder ("TO", "YOU"))) (decided by upperCaseStringEquality)
        }
        all (toLists) should (not contain inOrder (" TO ", " YOU "))
        intercept[TestFailedException] {
          (all (toLists) should (not contain inOrder (" TO ", " YOU "))) (after being lowerCased and trimmed)
        }
      }
      it("should throw NotAllowedException with correct stack depth and message when RHS contain duplicated value") {
        val e1 = intercept[exceptions.NotAllowedException] {
          all (toLists) should (not contain inOrder ("fee", "fie", "foe", "fie", "fum"))
        }
        e1.failedCodeFileName.get should be ("ListShouldContainInOrderSpec.scala")
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some(Resources.inOrderDuplicate))
      }
    }
    
    describe("when used with shouldNot contain inOrder (..)") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        all (toLists) shouldNot contain inOrder ("you", "to")
        val e1 = intercept[TestFailedException] {
          all (toLists) shouldNot contain inOrder ("to", "you")
        }
        e1.failedCodeFileName.get should be ("ListShouldContainInOrderSpec.scala")
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some("'all' inspection failed, because: \n" +
                                   "  at index 0, " + decorateToStringValue(prettifier, toLists(0)) + " contained all of " + "(\"to\", \"you\")" +  " in order (ListShouldContainInOrderSpec.scala:" + (thisLineNumber - 5) + ") \n" +
                                   "in " + decorateToStringValue(prettifier, toLists)))
      }
      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality
        all (toLists) shouldNot contain inOrder ("YOU", "TO")
        intercept[TestFailedException] {
          all (toLists) shouldNot contain inOrder ("TO", "YOU")
        }
      }
      it("should use an explicitly provided Equality") {
        (all (toLists) shouldNot contain inOrder ("YOU", "TO")) (decided by upperCaseStringEquality)
        intercept[TestFailedException] {
          (all (toLists) shouldNot contain inOrder ("TO", "YOU")) (decided by upperCaseStringEquality)
        }
        all (toLists) shouldNot contain inOrder (" TO ", " YOU ")
        intercept[TestFailedException] {
          (all (toLists) shouldNot contain inOrder (" TO ", " YOU ")) (after being lowerCased and trimmed)
        }
      }
      it("should throw NotAllowedException with correct stack depth and message when RHS contain duplicated value") {
        val e1 = intercept[exceptions.NotAllowedException] {
          all (toLists) shouldNot contain inOrder ("fee", "fie", "foe", "fie", "fum")
        }
        e1.failedCodeFileName.get should be ("ListShouldContainInOrderSpec.scala")
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some(Resources.inOrderDuplicate))
      }
    }

    describe("when used with shouldNot (contain inOrder (..))") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        all (toLists) shouldNot (contain inOrder ("you", "to"))
        val e1 = intercept[TestFailedException] {
          all (toLists) shouldNot (contain inOrder ("to", "you"))
        }
        e1.failedCodeFileName.get should be ("ListShouldContainInOrderSpec.scala")
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some("'all' inspection failed, because: \n" +
                                   "  at index 0, " + decorateToStringValue(prettifier, toLists(0)) + " contained all of " + "(\"to\", \"you\")" + " in order (ListShouldContainInOrderSpec.scala:" + (thisLineNumber - 5) + ") \n" +
                                   "in " + decorateToStringValue(prettifier, toLists)))
      }
      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality
        all (toLists) shouldNot (contain inOrder ("YOU", "TO"))
        intercept[TestFailedException] {
          all (toLists) shouldNot (contain inOrder ("TO", "YOU"))
        }
      }
      it("should use an explicitly provided Equality") {
        (all (toLists) shouldNot (contain inOrder ("YOU", "TO"))) (decided by upperCaseStringEquality)
        intercept[TestFailedException] {
          (all (toLists) shouldNot (contain inOrder ("TO", "YOU"))) (decided by upperCaseStringEquality)
        }
        all (toLists) shouldNot (contain inOrder (" TO ", " YOU "))
        intercept[TestFailedException] {
          (all (toLists) shouldNot (contain inOrder (" TO ", " YOU "))) (after being lowerCased and trimmed)
        }
      }
      it("should throw NotAllowedException with correct stack depth and message when RHS contain duplicated value") {
        val e1 = intercept[exceptions.NotAllowedException] {
          all (toLists) shouldNot (contain inOrder ("fee", "fie", "foe", "fie", "fum"))
        }
        e1.failedCodeFileName.get should be ("ListShouldContainInOrderSpec.scala")
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some(Resources.inOrderDuplicate))
      }
    }
  }
}
