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

function getAccountList() {
  var cmd = arguments.callee.name;
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

function getUsername() {
  var cmd = arguments.callee.name;
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


function getTransactionHistory() {
  var cmd = arguments.callee.name;
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

function logout() {
  var cmd = arguments.callee.name;
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

function transfer(recipient, accountTypeFrom, accountTypeTo, amount) {
  var cmd = arguments.callee.name;
  var args = [recipient, accountTypeFrom, accountTypeTo, amount];
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






