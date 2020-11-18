/**
* Licensed under the Apache License, Version 2.0 (the "License")
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.mycos.test

import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.Row
import org.apache.spark.sql.SQLContext


//create instances which adheres to the ML Inference HTTP POST body
//{"signature_name":"serving_default","instances":[{"bounces":[0],
//  "time_on_site":[7363]}]}
//corresponds to the '[{"bounces":[0],"time_on_site":[7363]}]'
case class InputRecordInstances (bounces: Array[Int],
    time_on_site: Array[Int])

//create instances which adheres to the ML Inference HTTP POST body
//{"signature_name":"serving_default","instances":[{"bounces":[0],
//  "time_on_site":[7363]}]}
case class InputRecord (signature_name: String,
    instances: Array[InputRecordInstances])

/**
 * @author dpani
 * Reads a file from hdfs, makes an ML inference, and prints the values
 * @param args input argument passed from the spark-submit
 *   args(0) - the ML Inference endpoint
 *     e.g. http://172.17.0.1:8000/v1/models/rpm:predict
 *   args(1) - the input file in the hdfs
 *   e.g. /dp_hdfs/dp_inf_test/rpm-hdfs-input.csv
 *      or gs://pr-anthos-demo-tmp/hdfs/rpm-hdfs-input.csv
 *   args(2) - skip or invoke the ML inference (expecting value yes/no)
 */
object RPMMLInference {
    /*
    * Main method. spark-submit uses this as an entry point
    * It expects three args viz.
    *   ML inference end point,
    *   hdfs full file path,
    *   either to invoke the ML inference endpoint,
    */
    def main(args: Array[String]) {
        println("RPM ML inference program...")
        if (args.length != 3) {
            var message = new StringBuffer("The program expcts three args:")
            message.append(sys.props("line.separator"))
            message.append("args(0) - the ML Inference endpoint")
            message.append(sys.props("line.separator"))
            message.append("  e.g. http://172.17.0.1:8000/v1/models/rpm:predict")
            message.append(sys.props("line.separator"))
            message.append("args(1) - the input file in the hdfs")
            message.append("  e.g. /dp_hdfs/dp_inf_test/rpm-hdfs-input.csv")
            message.append(sys.props("line.separator"))
            message.append("  e.g. gs://pr-anthos-demo-tmp/hdfs/rpm-hdfs-input.csv")
            message.append(sys.props("line.separator"))
            message.append("args(2) - skip or invoke the ML inference (expecting value yes/no)")
            message.append(sys.props("line.separator"))
            println (message.toString)
            sys.exit(1)
        }

        val mlInferenceEndPoint = args(0) //expecting the ML Inference end point
        val csvFile = args(1) //expecting a full hdfs path to a csv file
        val mlInferenceInvoke = args(2); //if yes then dont
                    //invoke the ml inference end point

        val sc = new SparkContext(new SparkConf()) //create a spark context

        val sqlContext = new SQLContext(sc) //create a sql context
        //read the csv file
        val df = sqlContext.read.option("header", "true")
            .option("inferSchema", "true").csv(csvFile)
        //check the ClassName that reads the csv
        println (df.getClass().getName())
        //gather the fields those are interests to us
        val selectedData = df.select("fullVisitorId",
                                     "bounces",
                                     "time_on_site",
                                     "will_buy_on_return_visit")
        //print the filtered data
        println (selectedData)
        //print the entire dataset
        //DON'T DO this in production
        df.show()

        //iterate through each row of the file and make an ml inference
        //DON'T DO this in production
        //the below will pull all the data to the master
        //think about the consequence if you have billion rows to process
        //then your servers is going to go out of memory
        selectedData.collect().foreach(row => processEachRow(
            row,
            mlInferenceEndPoint,
            mlInferenceInvoke))//end of for loop
    }

    /**
    * Method processes each row of input data,
    * and makes an ML inference
    * @param row - each row of input csv file
    * @param mlInferenceEndPoint - the ML Inference endpoint
    * @param mlInferenceInvoke - skip or invoke the ML inference
    */
    private def processEachRow(row: Row,
                        mlInferenceEndPoint: String,
                        mlInferenceInvoke: String) {
        //print each row value
        println(row)
        //print constructed values from the row
        println("id:" + row.get(0), " bounces:" + row.get(1),
                " time_on_site:", row.get(2),
                " will_buy_label:", row.get(3))
        if (mlInferenceInvoke.equalsIgnoreCase("yes")) {
            //make an ML inference
            inferMLEndpoint(mlInferenceEndPoint, row.get(1).toString.toInt,
                    row.get(2).toString.toInt)
        } else {
            println("Skipped the ML inference invocation.")
        }
    }

    /**
    * Method constructs the HTTP post, makes ML inference,
    *   and prints the response from the inference
    * @param mlInferenceEndPoint - the ML Inference endpoint
    * @param bouncesField - the field values from the input file
    * @param timesOnSiteField - the field values from the input file
    */
    private def inferMLEndpoint(mlInferenceEndPoint: String,
                      bouncesField: Int,
                      timesOnSiteField: Int) {
        // create our object as a json string
        val bounces = new Array[Int](1)
        bounces(0) = bouncesField //e.g. creates "bounces:[0]"
        val timesOnSite = new Array[Int](1)
        timesOnSite(0) = timesOnSiteField //e.g. creates "time_on_site:[7363]"

        //e.g. creates '[{"bounces":[0],"time_on_site":[7363]}]'
        val instances = new Array[InputRecordInstances](1)
        instances(0) = new InputRecordInstances(bounces, timesOnSite)

        //e.g. creates {"signature_name":"serving_default","instances":
        // [{"bounces":[0],"time_on_site":[7363]}]}
        val inputRecord = new InputRecord("serving_default", instances)
        val inputJson = new Gson().toJson(inputRecord)
        println("inputJson:" + inputJson)

        //set the json created above and set the content type
        val post = new HttpPost(mlInferenceEndPoint)
        post.setEntity(new StringEntity(inputJson))
        post.setHeader("Content-type", "application/json")

        // send the post request
        val client = new DefaultHttpClient

       // send the post request
        val response = client.execute(post)

        // post POST processing
        println("--- HEADERS ---")
        response.getAllHeaders.foreach(arg => println(arg))
        response.getAllHeaders.foreach(arg => println(arg))
        println(response.toString())
        var entity = response.getEntity()
        // println(entity.getContent())
        println(entity.getContentLength())

        //read the HTTP response body content from the ML inference server
        val rd: BufferedReader = new BufferedReader(new InputStreamReader(
                                    entity.getContent(), "UTF-8"))
        val builder = new StringBuilder()
        try {
            var line = rd.readLine
            while (line != null) {
                builder.append(line + "\n")
                line = rd.readLine
            } //end of while loop
        } finally {
            rd.close
        } //end of try block
        println("--- BODY ---")
        println(builder.toString)
    } //end of infer function
}

