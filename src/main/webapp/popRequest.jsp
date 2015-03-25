<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<head>
    <script src="js/jquery-2.1.3.js"></script>
    <script>
        function pollPayment() {
            $.ajax({
                url : 'http://localhost:8080/poppoc/PaymentPoll?address=${receiveAddress}',
                type: 'GET',
                success : function(data) {
                    if (data === 'VALID POP RECEIVED') {
                        $('#status').html('Pop received.');
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
Pop request:<br/>
<img src="GenerateQRCode?popRequest=<c:out value="${popRequestUrlEncoded}"/>"/>

<a href="<c:out value="${popRequest}"/>"><c:out value="${popRequest}"/></a><b/>


<b/>
<div id="status">
    Waiting for PoP...
</div>
<div id="useServiceLink" style="visibility: hidden">
    Great! here's your service.
</div>
</body>
</html>