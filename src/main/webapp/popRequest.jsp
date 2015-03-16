<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<head>

</head>

<body>
Pop request:<br/>
<a href="<c:out value="${popRequest}"/>"><c:out value="${popRequest}"/></a>

<img src="GenerateQRCode?popRequest=<c:out value="${popRequestUrlEncoded}"/>"/>
</body>
</html>