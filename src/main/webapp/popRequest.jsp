<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <script src="js/jquery-2.1.3.js"></script>
    <script>
        function pollPayment() {
            $.ajax({
                url : "${popPollUrl}",
                type: "GET",
                success : function(data) {
                    if (data === "VALID POP RECEIVED") {
                        $('#status').html("Pop received.");
                        $('#useServiceLink').css("visibility", "visible");
                        window.location.href = "${pageContext.request.contextPath}/Service?serviceId=${serviceId}";
                    } else {
                        setTimeout(pollPayment, 3000);
                    }
                },
                error : function(jqXHR, textStatus, errorThrown ) {
                    $('#status').html(textStatus + ': ' + errorThrown + '. Retrying...');
                    setTimeout(pollPayment, 3000);
                },
                dataType : "text"
            });
        }
        pollPayment();
    </script>
</head>

<body>
    Pop request:<br/>

    <img src="GenerateQRCode?popRequest=${popRequestUrlEncoded}"/><br/>

    <a href="${popRequest}">${popRequest}</a><br/>

    Have you not paid yet? <a href="${pageContext.request.contextPath}/RequestPayment?serviceId=${serviceId}&label=service${serviceId}">Pay here</a>

    <b/>
    <div id="status">
        Waiting for PoP...
    </div>
    <div id="useServiceLink" style="visibility: hidden">
        Great! here's your service.
    </div>
</body>
</html>