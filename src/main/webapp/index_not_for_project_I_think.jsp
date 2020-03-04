<%--
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--%>
<%@ page session="false" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%
java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy");
request.setAttribute("year", sdf.format(new java.util.Date()));
request.setAttribute("tomcatUrl", "https://tomcat.apache.org/");
request.setAttribute("tomcatDocUrl", "/docs/");
request.setAttribute("tomcatExamplesUrl", "/examples/");
%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8" />
        <title><%=request.getServletContext().getServerInfo() %></title>
        <link href="favicon.ico" rel="icon" type="image/x-icon" />
        <link href="favicon.ico" rel="shortcut icon" type="image/x-icon" />
        <link href="tomcat.css" rel="stylesheet" type="text/css" />
    </head>
    <body>
        <script type="text/javascript">
            function convertTemp(buttonPressed){
                cToF = document.getElementById('c-to-f')
                fToC = document.getElementById('f-to-c')
                if(buttonPressed == 'c-to-f'){
                    c = parseInt(cToF.value)
                    f = null
                    if(c){
                        f = (c * (9/5)) + 32;
                        fToC.value = f.toString();
                    }
                }
                else if(buttonPressed == 'f-to-c'){
                    f = parseInt(fToC.value)
                    c = null
                    if(f){
                        c = (f - 32) * (5/9);
                        cToF.value = c.toString();
                    }
                }
            }
        </script>
        <Label> C to F</Label>
        <input id="c-to-f" type="text" name="C to F">
        <button onclick="convertTemp('c-to-f')">C to F</button> 
        <br>
        <Label> F to C</Label>
        <input id="f-to-c" type="text" name="F to C">
        <button onclick="convertTemp('f-to-c')">F to C</button>
    </body>

</html>
