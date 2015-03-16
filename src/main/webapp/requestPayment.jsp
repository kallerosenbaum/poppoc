<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<head>

</head>

<body>
Please pay:<br/>
<a href="<c:out value="${paymentUri}"/>"><c:out value="${paymentUri}"/></a>

<img src="GenerateQRCode?popRequest=<c:out value="${paymentUriUrlEncoded}"/>"/>
</body>
</html>