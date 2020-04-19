<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ezekielnewren.Build" %>
<html>
<head>
    <title>Title</title>

    <link rel="stylesheet" type="text/css"  href="./styles/login.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
    <script src="lib/base64js/base64js-1.3.0.min.js"></script>
    <script src="js/base64url.js"></script>
    <script src="js/webauthn.js"></script>
    <script src="js/BankAPI.jsp" type="application/javascript"></script>
</head>
<body>
    <div class="wrapper">
        <h2 id="welcome" class="center-text">Login</h2>
        <div id="login-utils">
            <div class="line-item">
                <label id="lbl-username">Username:</label>
                <input type="text" id="username"/></div>
            <div class="line-item">
                <button class="std-button" onclick="onRegister()">Register</button>
                <button class="std-button" onclick="onLogin()">Login</button>
                <button class="std-button" onclick="onLogout()">Logout</button>
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
                <button class="std-button" onclick="onTest()">Test</button>
            </div>
        </div>
    </div>
    <script src="./scripts/errors.js"></script>
    <script src="./scripts/login.js"></script>
</body>
</html>