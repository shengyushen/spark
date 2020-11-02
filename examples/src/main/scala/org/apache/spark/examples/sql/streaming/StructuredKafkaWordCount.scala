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

// scalastyle:off println
package org.apache.spark.examples.sql.streaming

import java.util.UUID

import org.apache.spark.sql.SparkSession

/**
 * Consumes messages from one or more topics in Kafka and does wordcount.
 * Usage: StructuredKafkaWordCount <bootstrap-servers> <subscribe-type> <topics>
 *     [<checkpoint-location>]
 *   <bootstrap-servers> The Kafka "bootstrap.servers" configuration. A
 *   comma-separated list of host:port.
 *   <subscribe-type> There are three kinds of type, i.e. 'assign', 'subscribe',
 *   'subscribePattern'.
 *   |- <assign> Specific TopicPartitions to consume. Json string
 *   |  {"topicA":[0,1],"topicB":[2,4]}.
 *   |- <subscribe> The topic list to subscribe. A comma-separated list of
 *   |  topics.
 *   |- <subscribePattern> The pattern used to subscribe to topic(s).
 *   |  Java regex string.
 *   |- Only one of "assign, "subscribe" or "subscribePattern" options can be
 *   |  specified for Kafka source.
 *   <topics> Different value format depends on the value of 'subscribe-type'.
 *   <checkpoint-location> Directory in which to create checkpoints. If not
 *   provided, defaults to a randomized directory in /tmp.
 *
 * Example:
 *    `$ bin/run-example \
 *      sql.streaming.StructuredKafkaWordCount host1:port1,host2:port2 \
 *      subscribe topic1,topic2`
 */
// SSY 
object StructuredKafkaWordCount {
  def main(args: Array[String]): Unit = {
    if (args.length < 3) {
      System.err.println("Usage: StructuredKafkaWordCount <bootstrap-servers> " +
        "<subscribe-type> <topics> [<checkpoint-location>]")
      System.exit(1)
    }

    val Array(bootstrapServers, subscribeType, topics, _*) = args
    val checkpointLocation =
      if (args.length > 3) args(3) else "/tmp/temporary-" + UUID.randomUUID.toString
		// SSY sql/core/src/main/scala/org/apache/spark/sql/SparkSession.scala
		// it replace sparkContext of normal bathing job
		// and we can 
    val spark = SparkSession  // SSY this is object, not class
      .builder
      .appName("StructuredKafkaWordCount")
      .getOrCreate()

		//SSY this spark is not path, it refer to SparkSession we created above
		// it can convert a seq into dataset or dataframe
		// but no more dataframe, only dataset, they are unified after spark 2.0
    import spark.implicits._

    // Create DataSet representing the stream of input lines from kafka
    val lines = spark
      .readStream // SSY a data stream that can read kafka and parquet sql/core/src/main/scala/org/apache/spark/sql/SparkSession.scala
      .format("kafka") // SSY this is the source
      .option("kafka.bootstrap.servers", bootstrapServers)
      .option(subscribeType, topics)
      .load() // SSY sql/core/src/main/scala/org/apache/spark/sql/streaming/DataStreamReader.scala geenrating Dataset
      .selectExpr("CAST(value AS STRING)") // SSY sql/core/src/main/scala/org/apache/spark/sql/Dataset.scala
      .as[String] // SSY sql/core/src/main/scala/org/apache/spark/sql/Dataset.scala and return Dataset

    // Generate running word count
		// groupBy geenrate RelationalGroupedDataset
		// while count generate DataFrame that is actually Dataset
    val wordCounts = lines.flatMap(_.split(" ")).groupBy("value").count() // SSY this is the old spark schema? just like RDD 

    // Start running the query that prints the running counts to the console
		// wordCounts is Dataset
    val query = wordCounts.writeStream // SSY DataStreamWriter sql/core/src/main/scala/org/apache/spark/sql/streaming/DataStreamWriter.scala
      .outputMode("complete") // SSY DataStreamWriter
      .format("console") // SSY DataStreamWriter output to console, this is also source
      .option("checkpointLocation", checkpointLocation)
      .start() // SSY sql/core/src/main/scala/org/apache/spark/sql/streaming/DataStreamWriter.scala
			// SSY return StreamingQuery sql/core/src/main/scala/org/apache/spark/sql/streaming/StreamingQuery.scala
			// but actually start may produce ContinuousExecution and MicroBatchExecution both extend StreamExecution (with awaitTermination) again extend StreamingQuery 

    query.awaitTermination() // SSY so awaitTermination come from StreamExecution
  }

}
// scalastyle:on println
