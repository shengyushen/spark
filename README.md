starting up master and workers
	./sbin/start-all.sh
		core/src/main/scala/org/apache/spark/deploy/master/Master.scala 
		./core/src/main/scala/org/apache/spark/deploy/worker/Worker.scala
		

invoking script 
../HiBench/bin/workloads/micro/wordcount/spark/run.sh
1 bin/spark-submit
	1.1 bin/spark-class
		1.1.1 ./launcher/src/main/java/org/apache/spark/launcher/Main.java to build cmd
		1.1.2 ./core/src/main/scala/org/apache/spark/deploy/SparkSubmit.scala to launch app
2 ../HiBench/sparkbench/micro/src/main/scala/com/intel/sparkbench/micro/ScalaWordCount.scala until here we are still at master

	2.1 ../spark/core/src/main/scala/org/apache/spark/rdd/RDD.scala 
			core/src/main/scala/org/apache/spark/rdd/PairRDDFunctions.scala define additional functions for RDD with implicit linking
			core/src/main/scala/org/apache/spark/rdd/MapPartitionsRDD.scala define implimentation of functions such as compute
			./core/src/main/scala/org/apache/spark/scheduler/DAGScheduler.scala  post to eventProcessLoop
			core/src/main/scala/org/apache/spark/util/EventLoop.scala  extracting job from eventProcessLoop
			./core/src/main/scala/org/apache/spark/SparkContext.scala
			./core/src/main/scala/org/apache/spark/SparkEnv.scala

			./core/src/main/scala/org/apache/spark/scheduler/Stage.scala
			./core/src/main/scala/org/apache/spark/rdd/HadoopRDD.scala
			./core/src/main/scala/org/apache/spark/shuffle/ShuffleWriteProcessor.scala



instancing all managers ./core/src/main/scala/org/apache/spark/SparkEnv.scala
			./core/src/main/scala/org/apache/spark/storage/BlockManager.scala
					all sorts of storage that take care of put and get
					core/src/main/scala/org/apache/spark/storage/memory/MemoryStore.scala 
					core/src/main/scala/org/apache/spark/storage/DiskStore.scala 
./core/src/main/java/org/apache/spark/memory/TaskMemoryManager.java
			./core/src/main/scala/org/apache/spark/memory/MemoryManager.scala instancing all MemoryPools to controll all quota of all tasks
			./core/src/main/scala/org/apache/spark/memory/UnifiedMemoryManager.scala

	
			./core/src/main/scala/org/apache/spark/executor/Executor.scala
			run tasks
			./core/src/main/scala/org/apache/spark/scheduler/Task.scala
		
				

all sorts of RDD

core/src/main/scala/org/apache/spark/rdd/BlockRDD.scala
core/src/main/scala/org/apache/spark/rdd/CartesianRDD.scala
core/src/main/scala/org/apache/spark/rdd/CoalescedRDD.scala
core/src/main/scala/org/apache/spark/rdd/HadoopRDD.scala
core/src/main/scala/org/apache/spark/rdd/NewHadoopRDD.scala
core/src/main/scala/org/apache/spark/rdd/ParallelCollectionRDD.scala
core/src/main/scala/org/apache/spark/rdd/PartitionerAwareUnionRDD.scala
core/src/main/scala/org/apache/spark/rdd/PartitionwiseSampledRDD.scala
core/src/main/scala/org/apache/spark/rdd/RDD.scala
core/src/main/scala/org/apache/spark/rdd/ReliableCheckpointRDD.scala
core/src/main/scala/org/apache/spark/rdd/ShuffledRDD.scala
core/src/main/scala/org/apache/spark/rdd/UnionRDD.scala
core/src/main/scala/org/apache/spark/rdd/ZippedPartitionsRDD.scala
core/src/main/scala/org/apache/spark/rdd/ZippedWithIndexRDD.scala
external/kafka-0-10/src/main/scala/org/apache/spark/streaming/kafka010/KafkaRDD.scala
external/kafka-0-10-sql/src/main/scala/org/apache/spark/sql/kafka010/KafkaSourceRDD.scala
mllib/src/main/scala/org/apache/spark/mllib/rdd/SlidingRDD.scala
sql/core/src/main/scala/org/apache/spark/sql/execution/ShuffledRowRDD.scala
sql/core/src/main/scala/org/apache/spark/sql/execution/datasources/FileScanRDD.scala
sql/core/src/main/scala/org/apache/spark/sql/execution/datasources/v2/DataSourceRDD.scala
sql/core/src/main/scala/org/apache/spark/sql/execution/streaming/continuous/ContinuousDataSourceRDD.scala
sql/core/src/main/scala/org/apache/spark/sql/execution/streaming/state/StateStoreRDD.scala
streaming/src/main/scala/org/apache/spark/streaming/rdd/WriteAheadLogBackedBlockRDD.scala



SQL
examples/src/main/scala/org/apache/spark/examples/sql/streaming/StructuredKafkaWordCount.scala


tungsten project:
https://databricks.com/blog/2015/04/28/project-tungsten-bringing-spark-closer-to-bare-metal.html
llvm compilation : https://databricks.com/blog/2016/05/23/apache-spark-as-a-compiler-joining-a-billion-rows-per-second-on-a-laptop.html


# Apache Spark

Spark is a unified analytics engine for large-scale data processing. It provides
high-level APIs in Scala, Java, Python, and R, and an optimized engine that
supports general computation graphs for data analysis. It also supports a
rich set of higher-level tools including Spark SQL for SQL and DataFrames,
MLlib for machine learning, GraphX for graph processing,
and Structured Streaming for stream processing.

<https://spark.apache.org/>

[![Jenkins Build](https://amplab.cs.berkeley.edu/jenkins/job/spark-master-test-sbt-hadoop-2.7-hive-2.3/badge/icon)](https://amplab.cs.berkeley.edu/jenkins/job/spark-master-test-sbt-hadoop-2.7-hive-2.3)
[![AppVeyor Build](https://img.shields.io/appveyor/ci/ApacheSoftwareFoundation/spark/master.svg?style=plastic&logo=appveyor)](https://ci.appveyor.com/project/ApacheSoftwareFoundation/spark)
[![PySpark Coverage](https://img.shields.io/badge/dynamic/xml.svg?label=pyspark%20coverage&url=https%3A%2F%2Fspark-test.github.io%2Fpyspark-coverage-site&query=%2Fhtml%2Fbody%2Fdiv%5B1%5D%2Fdiv%2Fh1%2Fspan&colorB=brightgreen&style=plastic)](https://spark-test.github.io/pyspark-coverage-site)


## Online Documentation

You can find the latest Spark documentation, including a programming
guide, on the [project web page](https://spark.apache.org/documentation.html).
This README file only contains basic setup instructions.

## Building Spark

Spark is built using [Apache Maven](https://maven.apache.org/).
To build Spark and its example programs, run:

    ./build/mvn -DskipTests clean package

(You do not need to do this if you downloaded a pre-built package.)

More detailed documentation is available from the project site, at
["Building Spark"](https://spark.apache.org/docs/latest/building-spark.html).

For general development tips, including info on developing Spark using an IDE, see ["Useful Developer Tools"](https://spark.apache.org/developer-tools.html).

## Interactive Scala Shell

The easiest way to start using Spark is through the Scala shell:

    ./bin/spark-shell

Try the following command, which should return 1,000,000,000:

    scala> spark.range(1000 * 1000 * 1000).count()

## Interactive Python Shell

Alternatively, if you prefer Python, you can use the Python shell:

    ./bin/pyspark

And run the following command, which should also return 1,000,000,000:

    >>> spark.range(1000 * 1000 * 1000).count()

## Example Programs

Spark also comes with several sample programs in the `examples` directory.
To run one of them, use `./bin/run-example <class> [params]`. For example:

    ./bin/run-example SparkPi

will run the Pi example locally.

You can set the MASTER environment variable when running examples to submit
examples to a cluster. This can be a mesos:// or spark:// URL,
"yarn" to run on YARN, and "local" to run
locally with one thread, or "local[N]" to run locally with N threads. You
can also use an abbreviated class name if the class is in the `examples`
package. For instance:

    MASTER=spark://host:7077 ./bin/run-example SparkPi

Many of the example programs print usage help if no params are given.

## Running Tests

Testing first requires [building Spark](#building-spark). Once Spark is built, tests
can be run using:

    ./dev/run-tests

Please see the guidance on how to
[run tests for a module, or individual tests](https://spark.apache.org/developer-tools.html#individual-tests).

There is also a Kubernetes integration test, see resource-managers/kubernetes/integration-tests/README.md

## A Note About Hadoop Versions

Spark uses the Hadoop core library to talk to HDFS and other Hadoop-supported
storage systems. Because the protocols have changed in different versions of
Hadoop, you must build Spark against the same version that your cluster runs.

Please refer to the build documentation at
["Specifying the Hadoop Version and Enabling YARN"](https://spark.apache.org/docs/latest/building-spark.html#specifying-the-hadoop-version-and-enabling-yarn)
for detailed guidance on building for a particular distribution of Hadoop, including
building for particular Hive and Hive Thriftserver distributions.

## Configuration

Please refer to the [Configuration Guide](https://spark.apache.org/docs/latest/configuration.html)
in the online documentation for an overview on how to configure Spark.

## Contributing

Please review the [Contribution to Spark guide](https://spark.apache.org/contributing.html)
for information on how to get started contributing to the project.
