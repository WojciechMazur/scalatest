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
package org.scalatest.exceptions

import org.scalactic.source

// TODO: A test and code for null labels throwing an NPE
/**
 * Exception that indicates a <a href="http://www.artima.com/shop/scalacheck">ScalaCheck</a> property check failed.
 *
 * @param messageFun a function that returns a detail message (not optional) for this <code>GeneratorDrivenPropertyCheckFailedException</code>.
 * @param cause an optional cause, the <code>Throwable</code> that caused this <code>GeneratorDrivenPropertyCheckFailedException</code> to be thrown.
 * @param posOrStackDepthFun either a source position or a function that returns the depth in the stack trace of this exception at which the line of test code that failed resides.
 * @param payload an optional payload, which ScalaTest will include in a resulting <code>TestFailed</code> event
 * @param undecoratedMessage just a short message that has no redundancy with args, labels, etc. The regular "message" has everything in it.
 * @param args the argument values, if any, that caused the property check to fail.
 * @param namesOfArgs an optional list of string names for the arguments.
 * @param labels the labels, if any (see the ScalaCheck user guide for information on labels)
 *
 * @throws NullArgumentException if any parameter is <code>null</code> or <code>Some(null)</code>.
 *
 * @author Bill Venners
 */
class GeneratorDrivenPropertyCheckFailedException(
  messageFun: StackDepthException => String,
  cause: Option[Throwable],
  posOrStackDepthFun: Either[source.Position, StackDepthException => Int],
  payload: Option[Any],
  undecoratedMessage: String,
  args: List[Any],
  namesOfArgs: Option[List[String]],
  val labels: List[String]
) extends PropertyCheckFailedException(
  messageFun, cause, posOrStackDepthFun, payload, undecoratedMessage, args, namesOfArgs
) {

  /**
    * Constructs a <code>GeneratorDrivenPropertyCheckFailedException</code> with the given message function, cause exception, source position, payload,
    * undecorated message, argument values, names and labels.
    *
    * @param messageFun the message function
    * @param cause the optional cause
    * @param pos the source position
    * @param payload the payload
    * @param undecoratedMessage the undecorated message
    * @param args the argument values
    * @param namesOfArgs the argument names
    * @param labels the argument labels
    * @return
    */
  def this(
    messageFun: StackDepthException => String,
    cause: Option[Throwable],
    pos: source.Position,
    payload: Option[Any],
    undecoratedMessage: String,
    args: List[Any],
    namesOfArgs: Option[List[String]],
    labels: List[String]
  ) = this(messageFun, cause, Left(pos), payload, undecoratedMessage, args, namesOfArgs, labels)

  /**
    * Constructs a <code>GeneratorDrivenPropertyCheckFailedException</code> with the given message function, cause exception, stack depth function,
    * payload, undecorated message, argument values, names and labels.
    *
    * @param messageFun the message function
    * @param cause the optional cause
    * @param failedCodeStackDepthFun the function that returns the depth in the stack trace of this exception at which the line of test code that failed resides
    * @param payload the payload
    * @param undecoratedMessage the undecorated message
    * @param args the argument values
    * @param namesOfArgs the argument names
    * @param labels the argument labels
    */
  def this(
    messageFun: StackDepthException => String,
    cause: Option[Throwable],
    failedCodeStackDepthFun: StackDepthException => Int,
    payload: Option[Any],
    undecoratedMessage: String,
    args: List[Any],
    namesOfArgs: Option[List[String]],
    labels: List[String]
  ) = this(messageFun, cause, Right(failedCodeStackDepthFun), payload, undecoratedMessage, args, namesOfArgs, labels)

  /**
   * Returns an instance of this exception's class, identical to this exception,
   * except with the detail message option string replaced with the result of passing
   * the current detail message to the passed function, <code>fun</code>.
   *
   * @param fun A function that, given the current optional detail message, will produce
   * the modified optional detail message for the result instance of <code>GeneratorDrivenPropertyCheckFailedException</code>.
   */
  override def modifyMessage(fun: Option[String] => Option[String]): GeneratorDrivenPropertyCheckFailedException = {
    val mod =
      new GeneratorDrivenPropertyCheckFailedException(
        (_: StackDepthException) => fun(message).getOrElse(messageFun(this)),
        cause,
        posOrStackDepthFun,
        payload,
        undecoratedMessage,
        args,
        namesOfArgs,
        labels
      )
    mod.setStackTrace(getStackTrace)
    mod
  }

  /**
   * Returns an instance of this exception's class, identical to this exception,
   * except with the payload option replaced with the result of passing
   * the current payload option to the passed function, <code>fun</code>.
   *
   * @param fun A function that, given the current optional payload, will produce
   * the modified optional payload for the result instance of <code>TableDrivenPropertyCheckFailedException</code>.
   */
  override def modifyPayload(fun: Option[Any] => Option[Any]): GeneratorDrivenPropertyCheckFailedException = {
    val currentPayload = payload
    val mod =
      new GeneratorDrivenPropertyCheckFailedException(
        messageFun,
        cause,
        posOrStackDepthFun,
        fun(currentPayload),
        undecoratedMessage,
        args,
        namesOfArgs,
        labels
      )
    mod.setStackTrace(getStackTrace)
    mod
  }
}

