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
  var payload = {cmd};

  return new Promise(function(resolve, reject) {
    talk('api', payload)
      .done(function (data) {
        console.log(data);
        var username = data;
        resolve(username);
      }).catch(function (err) {
        reject(err);
      })
  });
}


