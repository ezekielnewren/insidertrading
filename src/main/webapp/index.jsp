<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ezekielnewren.Build" %>
<html>
<head>
    <title>Title</title>

    <link rel="stylesheet" type="text/css"  href="login.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
    <script src="lib/base64js/base64js-1.3.0.min.js"></script>
    <script src="js/base64url.js"></script>
    <script src="js/webauthn.js"></script>
    <script src="js/api.jsp" type="application/javascript"></script>

      <script>
        function register(username, displayName, nickname, requireResidentKey) {
            var payload = JSON.stringify({username, displayName, nickname, requireResidentKey});

            $.ajax({
                type: 'POST',
                url: urlprefix+'register/start',
                contentType: 'application/json',
                dataType: 'json',
                data: payload,
                success: function(data) {
                    var request = data;
                    console.log("got the request");
                    if (request == null) {
                        alert("that username has been taken");
                        return;
                    }
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
                            url: urlprefix + 'register/finish',
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

        function login(username, requireResidentKey) {
            var payload = JSON.stringify({username, requireResidentKey});

            talk('login/start', payload)
            .done(function(data) {
                console.log(data);
                // console.log('executeAuthenticateRequest', request);

                if (data == null) {
                    alert("you are already logged in");
                    return;
                }
                var pkcro = data.assertionRequest.publicKeyCredentialRequestOptions;

                return webauthn.getAssertion(pkcro).then(function(assertion) {
                    var requestId = data.requestId;
                    var publicKeyCredential = webauthn.responseToObject(assertion);

                    var payload = JSON.stringify({
                        requestId,
                        publicKeyCredential
                    });

                    talk('login/finish', payload)
                        .done(function(data) {
                            console.log(data);
                            if ("good" === data) {
                                alert("you are now logged in, navigating to bank home page");
                                window.location.href = urlprefix+"/bank.jsp"
                            }
                        }).catch(function(err) {
                        alert("uh oh, check the log");
                        console.log(err);
                    })
                }).catch(function(err) {
                    console.log(err);
                })
            }).catch(function(err) {
                console.log(err);
            })

        }

        function onRegister() {
            var username = $('#username').val();
            register(username, username, null, false);
        }

        function onLogin() {
            var username = $('#username').val();
            login(username, false);
        }

        function onTest() {
            getUsername().then(function(response) {
                if (response.error === null) {
                    var username = response.data;
                    if (username != null) {
                        alert("your username is: "+username);
                    } else {
                        alert("you haven't logged in yet");
                    }
                } else {
                    console.log(response.error);
                }
            }).catch(function(err) {
                console.log(err);
            });
        }

    </script>


</head>
<body>
    <div class="wrapper">
        <h2 class="center-text">Login</h2>
        <div>
            <div class="line-item">
                <label id="lbl-username">Username:</label>
                <input type="text" id="username"/></div>
            <div class="line-item">
                <button onclick="onRegister()">Register</button>
                <button onclick="onLogin()">Login</button>
                <button onclick="onTest()">whoami?</button>
            </div>
        </div>
    </div>
</body>
</html>
