<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<head>

</head>

<body>
<h1>Generate a Proof of Payment request</h1>
<form action="/GeneratePopRequest" >
    <label for="txid" >Transaction id:</label>
    <input type="text" id="txid" name="txid"/><br/>

    <label for="amount" >Amount (satoshis):</label>
    <input type="text" id="amount" name="amount"/><br/>

    <label for="text" >Text associated with tx:</label>
    <input type="text" id="text" name="text"/><br/>


    <input type="submit">Generate</input>
</form>
</body>
</html>