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
package org.apache.spark.sql.execution

import org.apache.spark.{Partition, TaskContext}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.internal.SQLConf

/**
 * It is just a wrapper over `sqlRDD`, which sets and makes effective all the configs from the
 * captured `SQLConf`.
 * Please notice that this means we may miss configurations set after the creation of this RDD and
 * before its execution.
 *
 * @param sqlRDD the `RDD` generated by the SQL plan
 * @param conf the `SQLConf` to apply to the execution of the SQL plan
 */
class SQLExecutionRDD(
    var sqlRDD: RDD[InternalRow], @transient conf: SQLConf) extends RDD[InternalRow](sqlRDD) {// SSY RDD core/src/main/scala/org/apache/spark/rdd/RDD.scala
  private val sqlConfigs = conf.getAllConfs
  private lazy val sqlConfExecutorSide = {
    val newConf = new SQLConf()
    sqlConfigs.foreach { case (k, v) => newConf.setConfString(k, v) }
    newConf
  }

  override val partitioner = firstParent[InternalRow].partitioner

  override def getPartitions: Array[Partition] = firstParent[InternalRow].partitions

  override def compute(split: Partition, context: TaskContext): Iterator[InternalRow] = {
    // If we are in the context of a tracked SQL operation, `SQLExecution.EXECUTION_ID_KEY` is set
    // and we have nothing to do here. Otherwise, we use the `SQLConf` captured at the creation of
    // this RDD.
    if (context.getLocalProperty(SQLExecution.EXECUTION_ID_KEY) == null) {
      SQLConf.withExistingConf(sqlConfExecutorSide) {
        firstParent[InternalRow].iterator(split, context)
      }
    } else {
      firstParent[InternalRow].iterator(split, context)
    }
  }
}
