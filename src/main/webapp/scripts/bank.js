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
        <div class="history-text from">From: ${transaction.fromAccountType}</div>
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
        if(resp){
            initOther()
        }
        else{
            alert("%cTransfer Failed", 'background: #FFBABA; color: ##D8000C;')
        }
    }).catch((error)=>{
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
        console.log(accountList)
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
        var nav = document.getElementById('right-side-nav')
        if (_username != null) {
            username = _username
            nav.innerHTML += '<button onclick="onLogout()">Log Out</button>'
            nav.innerHTML += '<div>' + _username + ' </div>'
        } else {
            nav.innerHTML = '<a href="./index.jsp">Login</a>'
        }
        initOther()
    }).catch(function(error) {
        console.log(`%c${error}`, 'background: #FFBABA; color: ##D8000C;')
    });
}

// On page load

init()


