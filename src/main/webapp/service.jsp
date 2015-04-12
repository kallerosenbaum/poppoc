<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Success</title>
</head>
<body>
  <h1>Yay!</h1>
  You have successfully authenticated to service ${serviceId} through Proof of Payment.
<form action="Logout">
    <input type="hidden" name="serviceId" value="${serviceId}"/>
    <input type="submit" value="Log out"/>
</form>
</body>
</html>
