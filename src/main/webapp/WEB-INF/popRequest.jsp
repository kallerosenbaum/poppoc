<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <script src="${pageContext.request.contextPath}/js/jquery-2.1.3.js"></script>
    <script>
        function pollPayment() {
            $.ajax({
                url : "${popPollUrl}",
                type: "GET",
                success : function(data) {
                    if (data === "VALID POP RECEIVED") {
                        $('#status').html("Pop received.");
                        window.location.href = "${pageContext.request.contextPath}/Service?serviceId=${serviceId}";
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

    <a href="${popRequest}">${popRequest}</a><br/>

    <p>
    Have you not paid yet? <a href="${pageContext.request.contextPath}/RequestPayment?serviceId=${serviceId}&label=service${serviceId}">Pay here</a>
    </p>
    <div id="status">
        Waiting for PoP...
    </div>
</body>
</html>