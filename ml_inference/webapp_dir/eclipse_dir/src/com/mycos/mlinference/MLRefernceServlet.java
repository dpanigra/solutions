/**
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      https://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mycos.mlinference;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author dpani The class's POST method reads the content of a form, 
 *          posts a request to the ML Inference server, and sends the
 *          results of the post to the browser.
 */
@WebServlet("/mlinference")
public class MLRefernceServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  // one instance, reuse
  private final HttpClient httpClient = HttpClient.newBuilder()
      .version(HttpClient.Version.HTTP_2).build();

    /**
     * Default constructor.
     */
    public MLRefernceServlet() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     *   The Servlet doesn't have a GET method to handle and passing to
     *   the doPst
     */
    protected void doGet(HttpServletRequest request, 
            HttpServletResponse response)
            throws ServletException, IOException {
        response.getWriter().append("GET is not supported.");
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response) The request has four parameters: 
     *      1. bounces - the field value the feature passed on to inference
     *      2. time_on_site - the field value the feature 
     *          passed on to inference 
     *      3. mlinference_host - the ML inference endpoint 
     *          e.g. http://172.17.0.1:8000/v1/models/rpm:predict 4.
     *      mlinference_check - skip or invoke the ML inference 
     *      (expecting value yes/no)
     */
    protected void doPost(HttpServletRequest request, 
            HttpServletResponse response)
            throws ServletException, IOException {
        String bounces = request.getParameter("bounces");
        String timeOnSite = request.getParameter("time_on_site");
        String mlInferenceHost = 
                request.getParameter("mlinference_host");
        String mlInferenceInvoke = 
                request.getParameter("mlinference_check");
        System.out.println("bounces:" + bounces);
        System.out.println("time_on_site:" + timeOnSite);
        System.out.println("mlinference_host:" + mlInferenceHost);
        System.out.println("mlinference_check:" + mlInferenceInvoke);
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        // create HTML response
        PrintWriter writer = response.getWriter();
        writer.append("<!DOCTYPE html>\r\n").append("<html>\r\n")
            .append("<head>\r\n")
            .append("<title>Inference</title>\r\n").append("<style>")
            .append(".myDiv {")
                .append("  border: 0px outset red;")
                .append("  background-color: lightgray;    ")
                .append("  text-align: left;").append("  width: 50%;")
                .append("}").append("</style>")
                .append("</head>\r\n").append("<body>\r\n");
        if (mlInferenceInvoke.toLowerCase().equals("yes")) {
            writer.append(infer(bounces, timeOnSite, mlInferenceHost));
        } else {
            writer.append("Skipping the ML inference invocation");
            writer.append(" for debugging purpose.");
        }
        writer.append("<p><button onclick=\"goBack()\">Go Back</button>")
            .append("<script>")
            .append("function goBack() {")
        .append("  window.history.back();").append("}")
        .append("</script>");
        writer.append("").append("<div class=\"myDiv\">")
                .append("Licensed under the Apache License, ")
                .append("Version 2.0 (the \"License\");<br>")
                .append("you may not use this file except in")
                .append("compliance with the License.<br>")
                .append("You may obtain a copy of the License at<p>")
                .append("")
                .append("     https://www.apache.org/licenses/LICENSE-2.0<p>")
                .append("")
                .append("Unless required by applicable law or")
                .append("agreed to in writing, software <br>")
                .append("distributed under the License is distributed")
                .append("on an \"AS IS\" BASIS, <br>")
                .append("WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, ")
                .append("either express or implied. <br>")
                .append("See the License for the specific language governing")
                .append(" permissions and <br>")
                .append("limitations under the License. <br>").append("</div>");
        writer.append("</body>\r\n").append("</html>\r\n");
    }

    /**
     * @param bounces    - supplied by the user in the form
     * @param timeOnSite - supplied by the user in the form
     * @return - returns the output from the ML Inference server
     * @throws ServletException
     * @throws IOException
     */
    private String infer(String bounces, String timeOnSite, 
            String mlInferenceHost)
            throws ServletException, IOException {

        var builder = new StringBuilder("{ \"signature_name\":  ");
        builder.append("\"serving_default\",  \"instances\": [{\"bounces\": [");
        builder.append(bounces);
        builder.append("], \"time_on_site\": [");
        builder.append(timeOnSite);
        builder.append("]}]}");

        System.out.println(builder.toString());

        HttpRequest request = HttpRequest.newBuilder()
        .POST(HttpRequest.BodyPublishers.ofString(builder.toString()))
                .uri(URI.create(mlInferenceHost))
        .setHeader("User-Agent", "My ecommerce app") // add request header
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // print status code
        System.out.println(response.statusCode());

        // print response body
        System.out.println(response.body());

        var output = new StringBuilder("Input:<br>");
        output.append(builder.toString());
        output.append("<p>");
        output.append("Output form ML Inference:<br>");
        output.append(response.body());

        return output.toString();
    }
}
