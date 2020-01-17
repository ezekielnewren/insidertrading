<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
    <script>
        function reqResp(payload, successCallback, errorCallback) {
            $.ajax({
                type: 'POST',
                url: 'http://localhost:8080/webauthn/index.jsp',
                contentType: 'application/json',
                dataType: 'json',
                data: JSON.stringify(payload),
                success: successCallback,
                error: errorCallback
            });
        }

        function good() {
            alert("success");
        }

        function bad() {
            alert("failure");
        }

        function register() {
            var username = $('#username').val();

            reqResp(username,
                function() {alert("success");},
                function() {alert("failure");}
            );

        }

    </script>


</head>
<body>
    <input type="text" id="username"/>
    <button id="register" onclick="register()">Register</button>
</body>
</html>
