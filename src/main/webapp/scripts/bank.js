function insertAccounts(accounts){
    var accounts_elm = document.getElementById('accounts') 
    for(var account of accounts){
        accounts_elm.innerHTML += `<div class="account-item">
                <div class="title">${account.name}</div>
                <div class="balance">\$${account.amount}</div>
        </div>`
    }
}
function insertTransactions(transactions){
    var transactions_elm = document.getElementById('history');
    for(var transaction of transactions){
        transactions_elm.innerHTML += `<div class="history-item">
        <div class="history-authorized">${transaction.authorized}</div>
        <div class="history-text">From: ${transaction.from} To: ${transaction.to}</div>
        <div class="history-amount">\$${transaction.amount}</div>
    </div>`
    }
}

testAccounts = [
    {name: 'Savings', amount: 30.00},
    {name: 'Checking', amount: 105.00},
]
testHistory = [
    {authorized: true, from: 'Savings', to:'Checking', amount: 100.00},
    {authorized: false, from: 'Checking', to:'Savings', amount: 5.00},
]

insertAccounts(testAccounts);
insertTransactions(testHistory);