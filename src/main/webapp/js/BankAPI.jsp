<%@ page import="com.ezekielnewren.Build" %>
<%@ page import="com.ezekielnewren.insidertrading.BankAPI" %>

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

<%= BankAPI.generateJSFunctions() %>

