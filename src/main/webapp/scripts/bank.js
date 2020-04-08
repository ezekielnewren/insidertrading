function insertAccounts(accounts){
    console.log("Before Clear Accounts")
    clearAccounts()
    var accountsElement = document.getElementById('account-list')
    var fromAccountElmement = document.getElementById('transfer-from')
    var toAccountElmement = document.getElementById('transfer-to')
    for(var account of accounts){
        console.log("Foor loop for account " + account.title)
        accountsElement.innerHTML += `<div class="account-item">
        <div class="title">${account.title}</div>
        <div class="balance">\$${account.balance}</div>
        </div>`
        fromAccountElmement.innerHTML += `<option value="${account.id}">${account.title} - \$${account.balance}</option>`
        toAccountElmement.innerHTML += `<option value="${account.id}">${account.title} - \$${account.balance}</option>`
    }
    console.log("End of function")
}

function clearAccounts(){
    document.getElementById('account-list').innerHTML = ''
    document.getElementById('transfer-from').innerHTML = ''
    document.getElementById('transfer-to').innerHTML = ''
}

function insertTransactions(transactions){
    clearTransactions()
    var transactions_elm = document.getElementById('transaction-history');
    for(var transaction of transactions){
        transactions_elm.innerHTML += `<div class="history-item">
        <div class="history-text">From: ${transaction.from} To: ${transaction.to}</div>
        <div class="history-amount">\$${transaction.amount}</div>
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
    logout()
    window.location.assign(indexUrl)
}

function onTransfer(){
    from = document.getElementById("transfer-from").value
    to = document.getElementById("transfer-to").value
    amount = document.getElementById('amount').value
    recipient = Number(document.getElementById('transfer-recipient').value)
    transfer(recipient, from, to, amount).then((resp)=>{
        if(resp){
            init()
        }
        else{
            alert("Transfer Failed")
        }
    }).catch((error)=>{
        console.log(error)
    })
}

function initOther(){
    getAccountList().then((accountList)=>{
        console.log(accountList)
        insertAccounts(accountList)
    }).catch((error)=>{
        console.log(error)
    })

    getTransactionHistory().then((transactionHistory)=>{
        insertTransactions(transactionHistory)
    }).catch((error)=>{
        console.log(error)
    })
}

// if you are logged in then you can initialize everything else
function init(){
    getUsername().then((username) => {
        if (username == null) {
            window.location.replace("index.jsp")
            return
        }
        var nav = document.getElementById('right-side-nav')
        if (username != null) {
            nav.innerHTML += '<button onclick="onLogout()">Log Out</button>'
            nav.innerHTML += '<div>' + username + ' </div>'
        } else {
            nav.innerHTML = '<a href="./index.jsp">Login</a>'
        }
        initOther()
    }).catch(function(err) {
        console.log(err);
    });
}

// On page load

init()


