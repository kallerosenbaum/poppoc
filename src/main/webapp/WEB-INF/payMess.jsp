<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>PayMess</title>
    <script type="text/javascript">
        function displayControls(id) {
            document.getElementById('div' + id).style.visibility = "visible";
            document.getElementById('input' + id).focus();
        }
    </script>
</head>
<body>
  <h1>Welcome to PayMess</h1>
On this site you pay for a message space. The message space will appear at the top of the list below.
To update it you click on the Update button beside it. This will take you to the Proof of Payment page.
A payment will give one year exclusivity on the message space. A message must not be longer than 140 characters.


<form method="GET" action="NewMessageSpace">
    <p>
        <input type="text" name="messageText" value="" placeholder="New message here"/>
        <input type="submit" value="Create"/> (5 mBTC)
    </p>
</form>


<c:forEach var="mess" items="${messageSpaces}">
    <form method="GET" action="UpdateMessageSpace">
        <p>
            <input type="hidden" name="id" value="${mess.id}"/>
            <div class="messageSpaceId">${mess.id}:</div>
            <div onclick="displayControls('${mess.id}')">
                ${mess.message.htmlSafeMessage}
            </div>
            <div style="visibility: hidden" id="div${mess.id}">
                <input id="input${mess.id}" name="input${mess.id}" type="text" placeholder="${mess.message.htmlSafeMessage}"/>
                <input type="submit" value="Update"/>
            </div>
        </p>
    </form>
</c:forEach>

</body>
</html>
