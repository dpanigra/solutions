## License
```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

# How to make an ML model inference on KFServing from container apps (web, Spark) running on Google Cloud Kubernetes Engine?
You will learn how to take a pre-trained model, deploy it in KFServing, and infer the model from a Web Application and a Spark job. The [article](https://medium.com/@dpani/how-to-make-an-ml-inference-from-a-web-app-and-a-spark-job-using-kfserving-all-running-on-google-c50ca849c9f0) walks through a step by step approach from development to deployment of the opinionated solution.

The article uses a pre-trained model created in the [How to build an end-to-end propensity to purchase solution using BigQuery ML and Kubeflow Pipelines](https://medium.com/@dpani/how-to-build-an-end-to-end-propensity-to-purchase-solution-using-bigquery-ml-and-kubeflow-pipelines-cd4161f734d9). 

### Objectives
 To help you be conversant on the following:
1. Inspect a pre-built Machine Learning model signature (in development envrionment)
1. Develop a Web App (HTML/CSS/JS for front end and Java for backend) (in development envrionment)
1. Test the Web App (in development envrionment)
1. Develop a Spark app (Scala) (in development envrionment)
1. Test the scala app (in development envrionment)
1. Setup Google Cloud Kuberentes Engine Cluster
1. Deploy Spark, Web App, and KFServing in Google Kuberenetes Engine
1. Test the Web App app running in the Google Kuberenetes Engine 
1. Submit the Spark job to the Spark running in the Google Kuberenetes Engine 
1. Setup all required softwares in development envrionment (TensorFlow, saved_model_cli, Docker, TFServing, Apache Tomcat, sbt, kubectl, scala, java)

### Costs
This tutorial uses billable components of Google Cloud:
* [Google Cloud Kubernetes Engine (GKE)](https://cloud.google.com/kubernetes-engine)
* [Google Cloud Storage](https://cloud.google.com/storage)
* [Google Container Registry](https://cloud.google.com/container-registry)
* [Google CloudRun](https://cloud.google.com/run)
* [Google Cloud Logging](https://cloud.google.com/logging)

Use the [Pricing Calculator](https://cloud.google.com/products/calculator/) to generate a cost estimate based on your projected usage.

## Before you begin
For this reference guide, you need a [Google Cloud project](https://console.cloud.google.com/cloud-resource-manager).

You can create a new one, or select a project you already created.
The following steps are required.
* [Select or create a Google Cloud project](https://console.cloud.google.com/cloud-resource-manager). When you first create an account, you get a $300 free credit towards your compute/storage costs.
* [Make sure that billing is enabled for your project](https://cloud.google.com/billing/docs/how-to/modify-project). 

## The git repo contents
The git repo contains all the commands and code to successfully build and deploy the end to end solution.
1. [Commands](commands.txt)
2. [A pre-trained model](saved_model)
3. [Input file with sample data for testing](rpm_input_data/rpm-hdfs-input.csv)
4. [The web app war file](webapp_dir/webwar_dir/ecommerce.war)
4. [The Spark app jar file](sparkapp_dir/sparkappjar_dir/mlinference_2.12-1.0.jar)
6. Code
    1. [Java Servlet](webapp_dir/eclipse_dir/src/com/mycos/mlinference/MLRefernceServlet.java)
    2. [Scala](sparkapp_dir/src/main/scala/RPMMLInference.scala)
    3. [HTML/CSS](webapp_dir/eclipse_dir/WebContent/index.html)
    4. [sbt build file](sparkapp_dir/read_rpm.sbt)
7. [GKE deployment files](gke_deploy_dir)

## Delete the GCP Project
To avoid incurring charges to your Google Cloud Platform account for the resources used in this tutorial is to **Delete the project**.

The easiest way to eliminate billing is to delete the project you created for the tutorial.

**Caution**: Deleting a project has the following effects:
* *Everything in the project is deleted.* If you used an existing project for this tutorial, when you delete it, you also delete any other work you've done in the project.
* **Custom project IDs are lost.** When you created this project, you might have created a custom project ID that you want to use in the future. To preserve the URLs that use the project ID, such as an appspot.com URL, delete selected resources inside the project instead of deleting the whole project. 

If you plan to explore multiple tutorials and quickstarts, reusing projects can help you avoid exceeding project quota limits.

1. In the Cloud Console, go to the **Manage resources** page.
Go to the [Manage resources page](https://console.cloud.google.com/iam-admin/projects)
1. In the project list, select the project that you want to delete and then click **Delete** Trash icon.
1. In the dialog, type the project ID and then click **Shut down** to delete the project.
