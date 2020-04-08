<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8" />
    <title><%=request.getServletContext().getServerInfo() %></title>
    <script src="js/BankAPI.jsp" type="application/javascript"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
    <link href="bank.css" rel="stylesheet" type="text/css" />
</head>

<body>
    <div id="nav">
        <div class="nav-item"></div>
        <h1 class="nav-item">Insider Trading</h1>
        <div id="right-side-nav" class="nav-item">
            <!-- Login or Username + logout -->
        </div>
    </div>
    <div id="body">`
        <div id="accounts">
            <h2>Accounts</h2>
            <div class="account-item">
                <div class="title">Title</div>
                <div class="balance">Account Total</div>
            </div>
            <div id="account-list">
                <!-- Accounts -->
            </div>
        </div>
        <div id="right">
            <div id="transfer">
                <h2>Transfers</h2>
                <select name="From" id="transfer-from" class="select-account">
                    <!-- Accounts -->
                </select>
                <div id="To"><label>To User - </label>&nbsp;<input id="transfer-recipient" placeholder="User id - Leave blank for internal transfers" type="text"></div>
                <select name="To" id="transfer-to" class="select-account">
                    <!-- Accounts -->
                </select>
                <div id="transfer-footer">
                    <input type="text" id="amount">
                    <button id="send-button" onclick="onTransfer()"> Send</button>
                </div>
            </div>
            <div id="history">
                <h2>Transactions</h2>
                <div class="history-item">
                    <div class="history-text">From: To:</div>
                    <div class="history-amount">$ Transfered</div>
                </div>
                <div id="transaction-history">
                    <!-- History -->
                </div>
            </div>
        </div>
    </div>
    <script src="scripts/bank.js"></script>
</body>

</html>