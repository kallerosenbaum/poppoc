<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>Send Proof of Payment</title>
    <script src="${pageContext.request.contextPath}/js/jquery-2.1.3.js"></script>
    <script>
        function pollPayment() {
            $.ajax({
                url : "${popPollUrl}",
                type: "GET",
                success : function(data) {
                    if (data === "VALID POP RECEIVED") {
                        $('#status').html("Pop received.");
                        <c:if test="${not empty serviceType.popCallback}">
                            window.location.href = "${serviceType.popCallback}";
                        </c:if>
                    } else {
                        setTimeout(pollPayment, 500);
                    }
                },
                error : function(jqXHR, textStatus, errorThrown ) {
                    $('#status').html(textStatus + ': ' + errorThrown + '. Retrying...');
                    setTimeout(pollPayment, 500);
                },
                dataType : "text"
            });
        }
        pollPayment();
    </script>
</head>

<body>
    <h1>Log in using Proof of Payment</h1>

    <img src="GenerateQRCode?popRequest=${popRequestUrlEncoded}"/><br/>

    <a href="${popRequest}">${popRequest}</a><br/><br/>

    <div id="status">
        Waiting for PoP...
    </div>
</body>
</html>