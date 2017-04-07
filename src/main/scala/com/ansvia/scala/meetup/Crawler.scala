package com.ansvia.scala.meetup

/**
  * Author: Robin (r@ansvia.com)
  */


import java.util.concurrent.{TimeUnit, Semaphore, LinkedBlockingQueue}
import scala.collection.mutable
import scala.collection.mutable.HashSet
import scala.concurrent.ExecutionContext.Implicits.global
import dispatch.classic.{url, Http}

import scala.concurrent.Future


case class Crawler(max:Int) extends Thread {
    import dispatch._
    import dispatch.classic.jsoup.JSoupHttp._
    import scala.collection.JavaConversions._

    private val queue = new LinkedBlockingQueue[String]()
    private val semaphore = new Semaphore(max, true)
    private val counter = new ConcurrentCounterRWLock()
    private var done = false

    def status(){
        println("  Running: " + counter.getCount + "/" + max)
    }

    def start(targetUrl:String){
        super.start()
        crawl(targetUrl)
        println("Starting Crawler")
    }


    override def run(): Unit = {
        while (!done){
            crawl(queue.poll(100, TimeUnit.SECONDS))
        }
    }

    private def crawl(link:String): Future[Unit] = {
        Future {
            try {
                semaphore.acquire()
                counter.increment()
                println(s"Crawling: $link")
                Http(url(link) </> { doc =>
                    doc.select("a").foreach { a =>
                        val _link = normalize(a.attr("href"))
                        if (_link.nonEmpty){
                            queue.put(_link)
                        }
                    }
                })
            }catch{
                case e:Throwable =>
                // @TODO(you): your handler here.
            }finally{
                semaphore.release()
                counter.decrement()
            }
        }
    }

    private def normalize(link:String) = {
        link.replaceAll("(#|^//)+", "")
    }

    def waitAll(){
        while (!done){
            status()
            Thread.sleep(1000)
        }
    }

    def setStop(){
        this.done = true
    }


}


object Crawler {

    def main(args: Array[String]) {
        val crawler = Crawler(2)
        crawler.start("http://www.detik.com")
        crawler.waitAll()
    }
}


object SimpleCrawler {

    import dispatch.classic.jsoup.JSoupHttp._
    import scala.collection.JavaConversions._

    private val semaphore = new Semaphore(3)

    def crawl(link:String){
        Future {
            semaphore.acquire()
            println(s"Crawling: $link")
            try {
                Http(url(link) </> { doc =>
                    doc.select("a").map(_.attr("href"))
                        .foreach {
                            case href if href.trim.length > 0 =>
                                crawl(href)

                    }
                })
            }catch{
                case e:Throwable =>
                // @TODO(you): your handler here.
            }finally{
                semaphore.release()
            }
        }
    }

    def main(args: Array[String]) {
        crawl("https://www.detik.com")
        Thread.sleep(10000)
    }

}


