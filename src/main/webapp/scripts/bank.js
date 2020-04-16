var menuShowing = false
var menu = document.getElementById('dialog-menu')
function showMenuToggle(){
    if(menuShowing){
        menu.style.display = 'none'
        menuShowing = false
    }
    else{
        menu.style.display = 'flex'
        menuShowing = true
    }
}

function closeMenu(){
    if(menuShowing){
        menu.style.display = 'none'
        menuShowing = false
    }
}

function insertAccounts(accounts){
    clearAccounts()
    var accountsElement = document.getElementById('account-list')
    var fromAccountElmement = document.getElementById('transfer-from')
    var toAccountElmement = document.getElementById('transfer-to')
    for(var account of accounts){
        accountsElement.innerHTML += `<div class="account-item">
        <div class="title">${account.title}</div>
        <div class="balance">\$${CentStringToDollarString(account.balance)}</div>
        </div>`
        fromAccountElmement.innerHTML += `<option value="${account.title}">${account.title} - \$${CentStringToDollarString(account.balance)}</option>`
        toAccountElmement.innerHTML += `<option value="${account.title}">${account.title}</option>`
    }
    fromAccountElmement.value = window.localStorage.getItem(fromAccountElmement.id)
    toAccountElmement.value = window.localStorage.getItem(toAccountElmement.id)
}

function clearAccounts(){
    document.getElementById('account-list').innerHTML = ''
    document.getElementById('transfer-from').innerHTML = ''
    document.getElementById('transfer-to').innerHTML = ''
}

function insertTransactions(transactions){
    clearTransactions()
    var transactions_elm = document.getElementById('transaction-history');
    for(var transaction of transactions.reverse()){
        transactions_elm.innerHTML += `<div class="history-item">
        <div class="history-text from">From: ${transaction.fromUser} - ${transaction.fromAccountType}</div>
        <div class="history-text to">To: ${transaction.toUser} - ${transaction.toAccountType}</div>
        <div class="history-amount">\$${CentStringToDollarString(transaction.amount)}</div>
    </div>`
    }
}

function clearTransactions(){
    document.getElementById('transaction-history').innerHTML = '';
}

function onLogout(){
    urlList = window.location.href.split("/")
    urlList[urlList.length - 1] = "index.jsp"
    indexUrl = urlList.join("/")
    logout().then((something)=>{
        window.location.assign(indexUrl)
    })
}

function transferSelect(caller){
    window.localStorage.setItem(caller.id, caller.value)
}

function onTransfer(){
    var recipient = document.getElementById('transfer-recipient').value
    var from = document.getElementById("transfer-from").value
    var to = document.getElementById("transfer-to").value
    var amount = DollarStringToCent(document.getElementById('amount').value)
    if(recipient == '' || recipient == null) recipient = username
    if(amount == null || amount == ''){
        console.log("%cERROR: Amount cannot be null", 'background: #FFBABA; color: ##D8000C;')
        return
    }
    transfer(recipient, from, to, amount).then((resp)=>{
        initOther()
    }).catch((error)=>{

        if (typeof error.responseJSON.errorCode !== 'undefined' && typeof error.responseJSON.message !== 'undefined') {
            var errorCode = error.responseJSON.errorCode;
            var message = error.responseJSON.message;
            alert("%cTransfer Failed", 'background: #FFBABA; color: ##D8000C;')
        }
        console.log(`%c${error}`, 'background: #FFBABA; color: ##D8000C;')
    })
}

function CentStringToDollarString(amount){
    amount_number = Number(amount)/100
    return amount_number.toFixed(2);
}

function DollarStringToCent(amount){
    return Number(amount)*100
}

function initOther(){
    getAccountList().then((accountList)=>{
        insertAccounts(accountList)
    }).catch((error)=>{
        console.log(`%c${error}`, 'background: #FFBABA; color: ##D8000C;')
    })

    getTransactionHistory().then((transactionHistory)=>{
        insertTransactions(transactionHistory)
    }).catch((error)=>{
        console.log(`%c${error}`, 'background: #FFBABA; color: ##D8000C;')
    })
}

var username = null

// if you are logged in then you can initialize everything else
function init(){
    getUsername().then((_username) => {
        if (_username == null) {
            window.location.assign("./index.jsp")
            return
        }
        var nav = document.getElementById('dialog-menu')
        if (_username != null) {
            username = _username
            nav.innerHTML = `
            <div class="dialog-menu-item">${username}</div>
            <div class="dialog-menu-item clickable bottom" onclick="onLogout()">Logout</div>
            `
        } else {
            nav.innerHTML = '<a href="./index.jsp">Login</a>'
        }
        initOther()
    }).catch(function(error) {
        console.log(`%c${error}`, 'background: #FFBABA; color: ##D8000C;')
    });
}

// On page load

document.getElementById('amount').addEventListener("keyup", function(event) {
    if (event.key === "Enter") {
        onTransfer()
    }
});

init()


