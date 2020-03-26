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

function getUsername() {
  var cmd = "getUsername";
  var args = null;
  var payload = JSON.stringify({cmd, args});

  return new Promise(function(resolve, reject) {
    talk('api', payload)
      .done(function (data) {
        resolve(data);
      }).catch(function (err) {
        reject(err);
      });
  });
}

function getAccountList() {
  var cmd = "getAccountList";
  var args = null;
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

function getTransactionHistory() {
  var cmd = "getTransactionHistory";
  var args = null;
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



