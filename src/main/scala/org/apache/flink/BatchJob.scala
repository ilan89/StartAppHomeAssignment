/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink
import org.apache.flink.api.common.functions.Partitioner
import org.apache.flink.api.common.operators.Order
import org.apache.flink.api.scala._


object StartAppTask {

  def main(args: Array[String]) {

    if(args.length == 2){
      // Retrieve the input and output paths
      val inputPath = args(0)
      val outputPath = args(1)

      // set up the execution environment
      val env = ExecutionEnvironment.getExecutionEnvironment

      // Import CSV file
      val ds = env.readCsvFile[(String, Int)](inputPath)

      // Count the number of distinct keys
      val numOfKeys = ds.distinct(0).count().toInt

      // Reorder the data, sort in ascending order, lose the key and write output to file
      ds.partitionCustom(new MyPartitioner ,0)
        .setParallelism(numOfKeys)
        .sortPartition(1,Order.ASCENDING)
        .mapPartition { p => p.map { a => a._2} }
        .writeAsText(outputPath)

      // execute program
      env.execute("StartApp Flink Task")
    } else {
      System.out.println("Please specify the input & output paths")
    }
  }

  class MyPartitioner extends Partitioner[String] {
    override def partition(k: String, i: Int): Int = (k.head.toInt - 40) % i
  }

}
