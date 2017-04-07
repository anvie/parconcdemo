package com.ansvia.scala.meetup

import java.util.concurrent.locks.ReentrantReadWriteLock

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Author: Robin (r@ansvia.com)
  */

case class Record[K, V](key:K, value:V){

    var prev:Option[Record[K, V]] = None

    var next:Option[Record[K, V]] = None

}

class LinkDB[K, V] {

    type R = Record[K, V]

    private var first:Option[R] = None
    private var last:Option[R] = None
    private val counter = new ConcurrentCounterRWLock()
    private val lock = new ReentrantReadWriteLock(true)

    def getFirst = first

    def getLast = last

    def put(k:K, v:V) = {
        lock.writeLock().lock()
        try {
            val rec = new Record(k, v)

            counter.increment() // assume always success

            last match {
                case Some(a) =>
                    rec.prev = last
                    last = Some(rec)
                    a.next = last
                case _ =>
                    first match {
                        case Some(a) =>
                            a.next = Some(rec)
                            last = Some(rec)
                        case _ =>
                            first = Some(rec)
                    }
            }
            rec
        }catch{
            case e:Throwable =>
                println("ERROR: " + e.getMessage)
                e.printStackTrace()
        }finally{
            lock.writeLock().unlock()
        }
    }

    def get(k:K): Option[V] = {
        lock.readLock().lock()
        try {
            lookup(k, first, reverse = false).map(_.value)
        }finally{
            lock.readLock().unlock()
        }
    }

    def get(k:K, callback:PartialFunction[R, Unit]){
        val fromHead = Future {
            lookup(k, first, reverse = false)
        }
        val fromTail = Future {
            lookup(k, last, reverse = true)
        }
        fromHead.onSuccess { case Some(result) => callback(result) }
        fromTail.onSuccess { case Some(result) => callback(result) }
    }


    private def lookup(k:K, r:Option[R], reverse:Boolean): Option[R] = {
        lock.readLock().lock()
        try {
            r.flatMap {
                case a if a.key == k => Some(a)
                case a =>
                    if (reverse){
                        lookup(k, a.prev, reverse)
                    }else{
                        lookup(k, a.next, reverse)
                    }
            }
        }finally{
            lock.readLock().unlock()
        }
    }


    def remove(k:K){
        remove(k, first)
    }


    private def remove(key:K, holder:Option[R]){
        lock.writeLock().lock()
        try {
            if (first.exists(_.key == key)) {
                first = first.flatMap(_.next)
                counter.decrement()
            }else if (last.exists(_.key == key)){
                last = last.flatMap(_.prev)
                counter.decrement()
            }else{
                lookup(key, first, reverse = false) match {
                    case Some(a) =>
                        val prev = a.prev
                        a.next.foreach(_.prev = prev)
                        prev.foreach(_.next = a.next)
                        counter.decrement()
                    case _ =>
                }
            }
        }catch{
            case e:Throwable =>
                println(s"ERROR: Cannot remove $key")
                e.printStackTrace()
        }finally{
            lock.writeLock().unlock()
        }
    }

    def count = counter.getCount

    def clear(){
        lock.writeLock().lock()
        try {
            clear(first)
            first = None
        }catch{
            case e:Throwable =>
                e.printStackTrace()
        }finally{
            lock.writeLock().unlock()
        }
    }

    private def clear(holder:Option[R]){
        holder match {
            case Some(a) =>
                clear(a.next)
                a.next = None
                a.prev = None
            case _ =>
        }
    }

}


