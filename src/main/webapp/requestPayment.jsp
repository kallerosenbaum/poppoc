<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <script src="js/jquery-2.1.3.js"></script>
    <script>
        function pollPayment() {
            $.ajax({
                url : '${paymentPollUrl}',
                type: 'GET',
                success : function(data) {
                    if (data === 'PAYMENT RECEIVED') {
                        $('#status').html('Payment received.');
                        $('#useServiceLink').css("visibility", "visible");
                    } else {
                        setTimeout(pollPayment, 3000);
                    }
                },
                dataType : 'text'
            });
        }
        pollPayment();
    </script>
</head>

<body>

    Please pay any amount to:<br/>
    <a href="<c:out value="${paymentUri}"/>"><c:out value="${paymentUri}"/></a><br/>

    <img src="GenerateQRCode?popRequest=<c:out value="${paymentUriUrlEncoded}"/>"/><br/>

    <div id="status">
        Waiting for payment...
    </div>
    <div id="useServiceLink" style="visibility: hidden">
        Now <a href="${pageContext.request.contextPath}/Service?serviceId=${serviceId}">use the service</a>
    </div>
</body>
</html>