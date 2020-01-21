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
        // function rr(payload, arg, successCallback, errorCallback) {
        //     $.ajax({
        //         type: 'POST',
        //         url: '/webauthn/'+arg,
        //         contentType: 'application/json',
        //         dataType: 'json',
        //         data: payload,
        //         success: function(data) {
        //             request = data;
        //             webauthn.createCredential(request.publicKeyCredentialCreationOptions);
        //         },
        //         error: function(errMsg) {bad(errMsg);}
        //     });
        // }

        // function rr(payload, arg, callback) {
        //     $.post("demo_test_post.asp",
        //     payload,
        //     function(data, status){
        //         alert("Data: " + data + "\nStatus: " + status);
        //     });
        // }

        // function register() {
        //     var username = $('#username').val();
        //     var payload = JSON.stringify(username);
        //
        //     $.ajax({
        //         type: 'POST',
        //         url: '/webauthn/'+'register/start',
        //         contentType: 'application/json',
        //         dataType: 'json',
        //         data: payload,
        //     }).then((response) => {
        //         request = data;
        //         var credential = webauthn.createCredential(request.publicKeyCredentialCreationOptions);
        //         var json = webauthn.responseToObject(credential);
        //         $.ajax({
        //             type: 'POST',
        //             url: '/webauthn/' + 'register/finish',
        //             contentType: 'application/json',
        //             dataType: 'json',
        //             data: payload,
        //         }).then((response) => {
        //             if ("good" == response) {
        //                 alert("registration successful");
        //             }
        //         });
        //     });
        //
        // }

        function register() {
            var username = $('#username').val();
            var payload = JSON.stringify({username, displayName: username, nickname: null, requireResidentKey: false});

            $.ajax({
                type: 'POST',
                url: '/webauthn/'+'register/start',
                contentType: 'application/json',
                dataType: 'json',
                data: payload,
                success: function(data) {
                    var request = data;
                    //var requestId = request.requestId;
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
                        console.log("client signature: "+json);
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
                    console.log("got a signature");
                    // var json = webauthn.responseToObject(credential);
                    // console.log("sig to json");
                    // $.ajax({
                    //     type: 'POST',
                    //     url: '/webauthn/' + 'register/finish',
                    //     contentType: 'application/json',
                    //     dataType: 'json',
                    //     data: payload,
                    //     success: function(data) {
                    //         var response = data;
                    //         if ("good" == response) {
                    //             alert("registration successful");
                    //         }
                    //     },
                    //     error: function(errMsg) {
                    //         console.log(errMsg);
                    //     }
                    // })
                },
                error: function(errMsg) {
                    console.log(errMsg);
                }
            });

        }

    </script>


</head>
<body>
    Version: <%= Build.get("version") %><br/>
    build time: <%= Build.get("buildtime") %><br/>
    fqdn: <%= Build.get("fqdn") %><br/>
    <input type="text" id="username"/>
    <button id="register" onclick="register()">Register</button>
</body>
</html>
