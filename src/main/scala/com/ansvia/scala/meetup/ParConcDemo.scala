/**
 * Copyright (C) 2017 Ansvia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ansvia.scala.meetup


/**
  * Author: Robin (r@ansvia.com)
  */
object ParConcDemo {

    def main(args:Array[String]){

        {
            val concCounter = new ConcurrentCounterSynchronized()
            concCounter.runParallel()
            println("synchronized counter: " + concCounter.getCount)
        }
        {
            val concCounter = new ConcurrentCounterReentrantLock()
            concCounter.runParallel()
            println("reentrant lock counter: " + concCounter.getCount)
        }
        {
            val concCounter = new ConcurrentCounterRWLock()
            concCounter.runParallel()
            println("rw lock counter: " + concCounter.getCount)
        }

    }

}
