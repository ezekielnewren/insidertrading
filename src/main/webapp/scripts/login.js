let advancedOpen = false;
let advancedSection = document.getElementById("advanced")
advancedSection.style.display = 'none';

function advancedToggle(event){
    event.preventDefault();
    if(advancedOpen){
        advancedSection.style.display = 'none';
        advancedOpen = false
    }
    else{
        advancedSection.style.display = 'block';
        advancedOpen = true
    }
}

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
        showError(message + "Error Code: " + errorCode);
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
        showError(err)
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
        showError("Username must be entered to login")
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
        showError("You are not logged in.")
    }
}

function onTest() {
    getAccountList().then(function (list) {
        console.log(list);
    }).catch(function (err) {
        handleError(err);
    });
}