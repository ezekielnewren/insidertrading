<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8" />
    <title><%=request.getServletContext().getServerInfo() %></title>
    <script src="js/api.jsp" type="application/javascript"></script>
    <link href="bank.css" rel="stylesheet" type="text/css" />
</head>

<body>
    <div id="nav">
        <div class="nav-item"></div>
        <h1 class="nav-item">Insider Trading</h1>
        <a  class="nav-item" href="./index.jsp">Login</a>
    </div>
    <div id="body">
        <div id="accounts">
            <h2>Accounts</h2>
            <div class="account-item">
                <div class="title">Title</div>
                <div class="balance">Account Total</div>
            </div>
        </div>
        <div id="right">
            <div id="transfer">
                <h2>Transfers</h2>
                <select name="From" id="transfer-from" class="select-account">
                    <!-- Options -->
                    <option value="Value">Value</option>
                </select>
                <div id="To">To:</div>
                <select name="To" id="transfer-to" class="select-account">
                    <!-- Options -->
                </select>
                <div id="transfer-footer">
                    <input type="text" id="amount">
                    <button id="send-button"> Send</button>
                </div>
            </div>
            <div id="history">
                <h2>Transactions</h2>
                <!-- Use JSP for loop -->
                <div class="history-item">
                    <div class="history-authorized">Authorized</div>
                    <div class="history-text">From: To:</div>
                    <div class="history-amount">$ Transfered</div>
                </div>
                <!-- Use JSP for loop -->
            </div>
        </div>
    </div>
    <script src="scripts/bank.js"></script>
</body>

</html>