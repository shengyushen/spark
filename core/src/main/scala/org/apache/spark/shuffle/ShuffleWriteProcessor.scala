/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.shuffle

import org.apache.spark.{Partition, ShuffleDependency, SparkEnv, TaskContext}
import org.apache.spark.internal.Logging
import org.apache.spark.rdd.RDD
import org.apache.spark.scheduler.MapStatus

/**
 * The interface for customizing shuffle write process. The driver create a ShuffleWriteProcessor
 * and put it into [[ShuffleDependency]], and executors use it in each ShuffleMapTask.
 */
private[spark] class ShuffleWriteProcessor extends Serializable with Logging {

  /**
   * Create a [[ShuffleWriteMetricsReporter]] from the task context. As the reporter is a
   * per-row operator, here need a careful consideration on performance.
   */
  protected def createMetricsReporter(context: TaskContext): ShuffleWriteMetricsReporter = {
    context.taskMetrics().shuffleWriteMetrics
  }

  /**
   * The write process for particular partition, it controls the life circle of [[ShuffleWriter]]
   * get from [[ShuffleManager]] and triggers rdd compute, finally return the [[MapStatus]] for
   * this task.
   */
	// SSY called from core/src/main/scala/org/apache/spark/scheduler/ShuffleMapTask.scala 
	// only for a particular partition
  def write(
      rdd: RDD[_], // SSY computing of rdd will be triggered ../spark/core/src/main/scala/org/apache/spark/rdd/RDD.scala 
      dep: ShuffleDependency[_, _, _],
      mapId: Long,
      context: TaskContext,
      partition: Partition): MapStatus = {
    var writer: ShuffleWriter[Any, Any] = null
    try {
      val manager = SparkEnv.get.shuffleManager
			// SSY SortShuffleManager full defined core/src/main/scala/org/apache/spark/shuffle/sort/SortShuffleManager.scala
			// only getReader  no getWriter core/src/main/scala/org/apache/spark/shuffle/ShuffleManager.scala
      writer = manager.getWriter[Any, Any]( // SSY getWriter on core/src/main/scala/org/apache/spark/shuffle/sort/SortShuffleManager.scala 
        dep.shuffleHandle, // SSY only type, not real class or function
        mapId,
        context,
        createMetricsReporter(context))
      writer.write(
// SSY haha triggering rdd iterator and then compute in core/src/main/scala/org/apache/spark/rdd/RDD.scala 
        rdd.iterator(partition, context).asInstanceOf[Iterator[_ <: Product2[Any, Any]]])
      writer.stop(success = true).get
    } catch {
      case e: Exception =>
        try {
          if (writer != null) {
            writer.stop(success = false)
          }
        } catch {
          case e: Exception =>
            log.debug("Could not stop writer", e)
        }
        throw e
    }
  }
}
