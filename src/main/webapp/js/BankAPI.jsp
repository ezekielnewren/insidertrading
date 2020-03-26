<%@ page import="com.ezekielnewren.Build" %>

var urlprefix = <%= Build.get("urlprefix") %>;

function talk(service, payload) {
  return $.ajax({
    type: 'POST',
    url: urlprefix + service,
    contentType: 'application/json',
    dataType: 'json',
    data: payload,
  })
}

function makeRequest(cmd, args) {
  var payload = JSON.stringify({cmd, args});

  return new Promise(function(resolve, reject) {
    talk('api', payload)
      .done(function (data) {
        resolve(data);
      }).catch(function (err) {
        reject(err);
      })
  });
}

function getAccountList() {
  return makeRequest(arguments.callee.name, null);
}

function getUsername() {
  return makeRequest(arguments.callee.name, null);
}

function getTransactionHistory() {
  return makeRequest(arguments.callee.name, null);
}

function logout() {
  return makeRequest(arguments.callee.name, null);
}

function transfer(recipient, accountTypeFrom, accountTypeTo, amount) {
  return makeRequest(arguments.callee.name, [recipient, accountTypeFrom, accountTypeTo, amount]);
}






