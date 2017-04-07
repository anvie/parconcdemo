package com.ansvia.scala.meetup

import java.util.concurrent.CountDownLatch
import java.util.concurrent.locks.{ReentrantReadWriteLock, ReentrantLock}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Author: Robin (r@ansvia.com)
  */

trait Counter {

    private var count = 0

    def increment(){
        count += 1
    }

    def decrement(){
        count -= 1
    }


    def getCount = count

}

trait SerialRunner {
    this: Counter =>

    def runSerial(){
        // @TODO(you): code here
    }

}

trait ParallelRunner {
    this: Counter =>

    def runParallel() = {
        val latch = new CountDownLatch(1000)
        for (i <- 1 to 1000){
            Future {
                increment()
                latch.countDown()
            }
        }
        latch.await()
    }
}


class ConcurrentCounterSynchronized extends Counter with ParallelRunner {

    override def increment() = synchronized {
        super.increment()
    }

}


class ConcurrentCounterReentrantLock extends Counter with ParallelRunner {

    private val lock = new ReentrantLock(true)

    override def increment(): Unit = {
        lock.lock()
        try {
            super.increment()
        }catch{
            case e:Exception =>
                // @TODO(you): your handler here.
        }finally{
            lock.unlock()
        }
    }

}


class ConcurrentCounterRWLock extends Counter with ParallelRunner {

    private val lock = new ReentrantReadWriteLock(true)

    override def increment(): Unit = {
        lock.writeLock().lock()
        try {
            super.increment()
        }catch{
            case e:Exception =>
            // @TODO(you): your handler here.
        }finally{
            lock.writeLock().unlock()
        }
    }


    override def decrement(): Unit = {
        lock.writeLock().lock()
        try {
            super.decrement()
        }catch{
            case e:Exception =>
            // @TODO(you): your handler here.
        }finally{
            lock.writeLock().unlock()
        }
    }

    override def getCount: Int = {
        lock.readLock().lock()
        try {
            super.getCount
        }finally {
            lock.readLock().unlock()
        }
    }
}





