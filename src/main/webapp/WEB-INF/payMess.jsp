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
  <p>For more information regarding this system and to download a PoP enabled android wallet,
      see <a href="/">main page</a>.</p>

<p>On this site you pay for a message space. The message space will appear at the top of the list below.
To update it you click on the text you want to update. This will take you to the Proof of Payment page.
A payment will give one year exclusivity on the message space. A message must not be longer than 140 characters.</p>


<form method="GET" action="NewMessageSpace">
    <p>
        <input required="required" pattern=".{1,140}" type="text" name="messageSpaceText" value="" placeholder="New message here"/>
        <input type="submit" value="Create"/> (1 mBTC)
    </p>
</form>


<c:forEach var="mess" items="${messageSpaces}">
    <form method="GET" action="UpdateMessageSpace">
        <p>
            <input type="hidden" name="messageSpaceId" value="${mess.id}"/>
            <div class="messageSpaceId">${mess.id}:</div>
            <div onclick="displayControls('${mess.id}')">
                <c:out value="${mess.message.messageText}"/>
            </div>
            <div style="visibility: hidden" id="div${mess.id}">
                <input required="required" pattern=".{1,140}" id="input${mess.id}" name="messageSpaceText" type="text" placeholder="<c:out value="${mess.message.messageText}"/>"/>
                <input type="submit" value="Update"/>
            </div>
        </p>
    </form>
</c:forEach>

</body>
</html>
