<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<head>

</head>

<body>
<h1>Generate a Proof of Payment request</h1>
<form action="${pageContext.request.contextPath}/GeneratePopRequest" >
    <label for="txid" >Transaction id:</label>
    <input type="text" id="txid" name="txid"/><br/>

    <label for="amount" >Amount (BTC):</label>
    <input type="text" id="amount" name="amount"/><br/>

    <label for="text" >Text associated with tx:</label>
    <input type="text" id="text" name="text"/><br/>

    <input type="submit" value="Generate" />
</form>
</body>
</html>