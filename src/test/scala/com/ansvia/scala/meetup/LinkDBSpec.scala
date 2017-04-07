package com.ansvia.scala.meetup

import java.util.concurrent.{TimeUnit, CountDownLatch}

import org.specs2.mutable.Specification
import org.specs2.specification.Scope

/**
  * Author: Robin (r@ansvia.com)
  */

class LinkDBSpec extends Specification {

    "LinkDB" should {
        "able to do put operation" in new Ctx {
            db.put("1", "satu") must_== Record("1", "satu")
            db.put("2", "dua") must_== Record("2", "dua")
            db.put("3", "tiga") must_== Record("3", "tiga")

            db.get("1") must_== Some("satu")
            db.get("2") must_== Some("dua")
            db.get("3") must_== Some("tiga")
        }
        "able to get record (sync)" in new Ctx with Put {
            db.get("1") must_== Some("satu")
            db.get("2") must_== Some("dua")
        }
        "able to get record (async)" in new Ctx with Put {
            private var aCalled = false
            private var bCalled = false

            private val latch = new CountDownLatch(2)

            db.get("1", {
                case rec =>
                    rec.value must_== "satu"
                    aCalled = true
                    latch.countDown()
            })
            db.get("2", {
                case rec =>
                    rec.value must_== "dua"
                    bCalled = true
                    latch.countDown()
            })

            latch.await(5, TimeUnit.SECONDS)

            aCalled must beTrue
            bCalled must beTrue

        }
        "able to count" in new Ctx with Put {
            db.count must_== 2
        }
        "able to remove record" in new Ctx with Put {
            db.put("4", "empat")
            db.remove("1")
            db.get("1") must be equalTo None
            db.get("2") must_== Some("dua")
            db.get("3") must_== None
            db.count must_== 2
            db.remove("4")
            db.getLast must_== db.getFirst
            db.count must_== 1
            db.remove("2")
            db.count must_== 0
        }
        "able to clear up it contents" in new Ctx with Put {
            db.clear()
            db.get("1") must be equalTo None
            db.get("2") must be equalTo None
        }
    }

    trait Ctx extends Scope {
        val db = new LinkDB[String, String]()
    }

    trait Put extends Ctx {
        db.put("1", "satu")
        db.put("2", "dua")
    }
}
