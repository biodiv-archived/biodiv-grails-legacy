<%@page import="species.utils.Utils"%>
<html>
<head>
<meta name="layout" content="main" />
<title>Error</title>
<r:require modules="core" />
</head>

<body>
<%
String supportEmail = "";
String domain = Utils.getDomain(request);
if(domain.equals(grailsApplication.config.wgp.domain)) {
	supportEmail = grailsApplication.config.speciesPortal.wgp.supportEmail;
} else if(domain.equals(grailsApplication.config.ibp.domain)) {
	supportEmail =  grailsApplication.config.speciesPortal.ibp.supportEmail;
}
%>

			<div class="observation  span12">
				<p class="message">
					Oops!!! There seems to be some problem. <br /> Please send the
                                        following message and other required information to reproduce this error on our side to <span class="mailme">${supportEmail }</span>.<br/> One of us will work on it asap.<br />
					<br />
				</p>
				<div class="errors">
					<strong>Error ${request.'javax.servlet.error.status_code'}:</strong>
					${request.'javax.servlet.error.message'.encodeAsHTML()}<br />
				</div>


	</div>
</body>
</html>
