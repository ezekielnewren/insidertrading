<%@ page import="com.ezekielnewren.Build" %>
<%@ page import="com.ezekielnewren.insidertrading.BankAPI" %>

var urlprefix = <%= Build.get("urlprefix") %>;

function talk(service, payload) {
    "use strict";
    return $.ajax({
        type: 'POST',
        url: urlprefix + service,
        contentType: 'application/json',
        dataType: 'json',
        data: payload
    });
}

function makeRequest(cmd, args) {
    "use strict";
    var payload = JSON.stringify({cmd, args});

    return new Promise(function (resolve, reject) {
        talk('api', payload)
            .done(function (data) {
                resolve(data);
            }).catch(function (err) {
                reject(err);
            });
    });
}

function register(username, displayName, nickname, requireResidentKey) {
    "use strict";
    var payload = JSON.stringify({username, displayName, nickname, requireResidentKey});

    return new Promise(function (resolve, reject) {
        talk('register/start', payload).done(function (request) {
            if (request == null) {
                reject("that username has been taken");
                return;
            }
            webauthn.createCredential(request.publicKeyCredentialCreationOptions).then(function (res) {
                // Send new credential info to server for verification and registration.
                var credential = webauthn.responseToObject(res);
                var json = JSON.stringify({requestId: request.requestId, credential});
                talk('register/finish', json).done(function (response) {
                    if ("good" == response) {
                        resolve(username);
                    } else {
                        reject(response);
                    }
                }).catch(function (err) {
                    reject(err);
                });
            }).catch(function (err) {
                reject(err);
            });
        }).catch(function (err) {
            reject(err);
        });
    });
}

function login(username, requireResidentKey) {
    "use strict";

    var payload = JSON.stringify({username, requireResidentKey});
    return new Promise(function (resolve, reject) {
        if (window.username != null) {
            reject("you are already logged in");
            return;
        }
        talk('login/start', payload).done(function (data) {
            if (data == null) {
                reject("that username does not exist")
                return;
            }

            var pkcro = data.assertionRequest.publicKeyCredentialRequestOptions;
            return webauthn.getAssertion(pkcro).then(function(assertion) {
                var payload = JSON.stringify({
                    requestId: data.requestId,
                    publicKeyCredential: webauthn.responseToObject(assertion)
                });

                talk('login/finish', payload).done(function(data) {
                    if ("good" === data) resolve(username);
                    else reject("login failed");
                }).catch(function(err) {
                    reject(err);
                })
            }).catch(function(err) {
                reject(err);
            })
        }).catch(function (err) {
            reject(err);
        });
    });
}

<%= BankAPI.generateJSFunction() %>

