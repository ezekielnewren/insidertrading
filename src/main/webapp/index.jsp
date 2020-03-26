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
    <script src="js/BankAPI.jsp" type="application/javascript"></script>

    <script>
        window.username = null;
        function onPageLoad() {
            getUsername().then(function(_username) {
                if (_username != null) {
                    window.username = _username;
                    $('#welcome').text("Welcome "+_username);
                }
            });
        }
        window.onload = onPageLoad();

        function onSuccessfulLogin() {
            window.location.href = urlprefix+"/bank.jsp"
        }

        function onSuccessfulLogout() {
            $('#welcome').text("Login");
            window.username = null;
        }

        function login(username, requireResidentKey) {
            if (window.username != null) {
                alert("you are already logged in");
                return;
            }

            var payload = JSON.stringify({username, requireResidentKey});

            talk('login/start', payload)
                .done(function(data) {
                    console.log(data);

                    if (data == null) {
                        alert("that username does not exist")
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
                                    // alert("you are now logged in, navigating to bank home page");
                                    onSuccessfulLogin();
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
            getUsername().then(function(username) {
                if (username != null) {
                    alert("you must logout before creating an account");
                    return;
                }
                var username = $('#username').val();
                register(username, username, null, false);
            });
        }

        function onLogin() {
            var username = $('#username').val();
            login(username, false);
        }

        function onLogout() {
            logout().then(function(username) {
                onSuccessfulLogout();
                alert("goodbye "+username);
            }).catch(function(err) {
                console.log(err);
            });
        }

    </script>


</head>
<body>
<div class="wrapper">
    <h2 id="welcome" class="center-text">Login</h2>
    <div>
        <div class="line-item">
            <label id="lbl-username">Username:</label>
            <input type="text" id="username"/></div>
        <div class="line-item">
            <button onclick="onRegister()">Register</button>
            <button onclick="onLogin()">Login</button>
            <button onclick="onLogout()">Logout</button>
        </div>
    </div>
</div>
</body>
</html>
