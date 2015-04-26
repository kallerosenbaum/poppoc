<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Success</title>
</head>
<body>
  <h1>Awesome!</h1>
  You have authenticated to this service through Proof of Payment.
<form action="Logout">
    <input type="hidden" name="serviceId" value="${serviceId}"/>
    <input type="submit" value="Log out"/>
</form>
</body>
</html>
