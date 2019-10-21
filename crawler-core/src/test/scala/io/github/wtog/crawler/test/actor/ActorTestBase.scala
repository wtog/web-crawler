package io.github.wtog.crawler.test.actor

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * @author : tong.wang
  * @since : 2019-04-22 08:06
  * @version : 1.0.0
  */
abstract class ActorTestBase extends TestKit(ActorSystem("testsystem")) with WordSpecLike
with Matchers
with BeforeAndAfterAll
