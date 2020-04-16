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

        function onSuccessfulRegistration(username) {
            alert("the username \""+username+"\" has now been registered");
            onSuccessfulLogin(username);
        }

        function onSuccessfulLogin(username) {
            window.username = username;
            urlList = window.location.href.split("/")
            urlList[urlList.length - 1] = "bank.jsp"
            bankUrl = urlList.join("/")
            window.location.assign(bankUrl)
        }

        function onSuccessfulLogout() {
            $('#welcome').text("Login");
            window.username = null;
        }

        function handleError(err) {
            console.log(err);
            if (err.responseJSON) {
                var errorCode = err.responseJSON.errorCode;
                var message = err.responseJSON.message;
                alert(message+" error code: "+errorCode);
            }
        }

        function getSelection(byId) {
            var e = document.getElementById(byId);
            return e.options[e.selectedIndex].value;
        }

        function onRegister() {
            getUsername().then(function(username) {
                if (username != null) {
                    alert("you must logout before creating an account");
                    return;
                }
                var username = $('#username').val();
                var attestationType = getSelection("attestationType");
                var authenticatorType = getSelection("authenticatorType");
                var userVerification = getSelection("userVerification");
                var requireResidentKey = document.getElementById("requireResidentKey").checked;

                register(username, attestationType, authenticatorType, userVerification, requireResidentKey).then(function (username) {
                    onSuccessfulRegistration(username);
                }).catch(function (err) {
                    handleError(err);
                })
            }).catch((err)=> {
                console.log(err);
            });
        }

        function onLogin() {
            var username = $('#username').val();
            if(username != null && username != ''){
                login(username, false).then(function (username) {
                    onSuccessfulLogin(username);
                }).catch(function (err) {
                    handleError(err);
                })
            }
            else{
                alert("Please enter a username")
            }
        }

        function onLogout() {
            if(window.username != null){
                logout().then(function(username) {
                    onSuccessfulLogout();
                    alert("goodbye "+username);
                }).catch(function(err) {
                    handleError(err);
                });
            }
            else{
                alert("You are not logged in.")
            }
        }

        function onTest() {
            getAccountList().then(function (list) {
                console.log(list);
            }).catch(function (err) {
                handleError(err);
            });
        }

    </script>


</head>
<body>
<div class="wrapper">
    <h2 id="welcome" class="center-text">Login</h2>
    <div id="login-utils">
        <div class="line-item">
            <label id="lbl-username">Username:</label>
            <input type="text" id="username"/></div>
        <div class="line-item">
            <button onclick="onRegister()">Register</button>
            <button onclick="onLogin()">Login</button>
            <button onclick="onLogout()">Logout</button>
            
        </div>
    </div>
    <div id="advanced-toggle">
        <a href="#" onclick="advancedToggle(event)">Advanced Options</a>
    </div>
    <div id="advanced">
        <div class="advanced-item">
            <span>attestation type</span>
            <select id="attestationType">
                <option value="NONE">NONE</option>
                <option value="INDIRECT">INDIRECT</option>
                <option value="DIRECT" selected>DIRECT</option>
            </select>
        </div>
    
        <div class="advanced-item">
            <span>authenticator type</span>
            <select id="authenticatorType">
                <option value="CROSS_PLATFORM" selected>CROSS_PLATFORM</option>
                <option value="PLATFORM">PLATFORM</option>
            </select>
        </div>
        
        <div class="advanced-item">
            <span>user verification</span>
            <select id="userVerification">
                <option value="DISCOURAGED" selected>DISCOURAGED</option>
                <option value="PREFERRED">PREFERRED</option>
                <option value="REQUIRED">REQUIRED</option>
            </select>
        </div>
    
        <div class="advanced-item">
            <span>require resident key</span>
            <input type="checkbox" id="requireResidentKey"></input>
        </div>

        <div class="advanced-item" style="justify-content: center;">
            <button onclick="onTest()">test</button>
        </div>
    
        <div>
</div>
<script src="./scripts/login.js"></script>
</body>
</html>
























































