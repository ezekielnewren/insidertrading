<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ezekielnewren.Build" %>
<html>
<head>
    <title>Title</title>

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
    <script src="lib/base64js/base64js-1.3.0.min.js"></script>
    <script src="js/base64url.js"></script>
    <script src="js/webauthn.js"></script>
    <script>
        function register(username, displayName, nickname, requireResidentKey) {
            var payload = JSON.stringify({username, displayName, nickname, requireResidentKey});

            $.ajax({
                type: 'POST',
                url: '/webauthn/'+'register/start',
                contentType: 'application/json',
                dataType: 'json',
                data: payload,
                success: function(data) {
                    var request = data;
                    console.log("got the request");
                    webauthn.createCredential(request.publicKeyCredentialCreationOptions)
                    .then(function (res) {
                        // Send new credential info to server for verification and registration.
                        var credential = webauthn.responseToObject(res);
                        const body = {
                            requestId: request.requestId,
                            credential,
                        };
                        var json = JSON.stringify(body);
                        // console.log("client signature: "+json);
                        $.ajax({
                            type: 'POST',
                            url: '/webauthn/' + 'register/finish',
                            contentType: 'application/json',
                            dataType: 'json',
                            data: json,
                            success: function(data) {
                                var response = data;
                                if ("good" == response) {
                                    alert("registration successful");
                                }
                            },
                            error: function(errMsg) {
                                console.log(errMsg);
                            }
                        })
                    }).catch(function (err) {
                        // No acceptable authenticator or user refused consent. Handle appropriately.
                        console.log(err);
                        alert("Failed to add Authenticator");
                    });
                    // console.log("got a signature");
                },
                error: function(errMsg) {
                    console.log(errMsg);
                }
            });

        }

        function onRegister() {
            var username = $('#username').val();
            register(username, username, null, false);
        }
    </script>


</head>
<body>
    Version: <%= Build.get("version") %><br/>
    build time: <%= Build.get("buildtime") %><br/>
    fqdn: <%= Build.get("fqdn") %><br/>
    <input type="text" id="username"/>
    <button id="register" onclick="onRegister()">Register</button>
</body>
</html>
