<%@page import="species.utils.Utils"%>
<html>
<head>
<title>Error</title>
<g:javascript src="species/util.js" />
<meta name="layout" content="main" />
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
	<div class="container_12 container outer-wrapper">
		<div class="row">
			<div class="observation  span12 ui-state-error">
				<p class="message">
					Oops!!! There seems to be some problem. <br /> Please mail us the
					following message as bug report here <span class="mailme">${supportEmail }</span><br />
					<br />
				</p>
				<div class="errors">
					<strong>Error ${request.'javax.servlet.error.status_code'}:</strong>
					${request.'javax.servlet.error.message'.encodeAsHTML()}<br />
				</div>
			</div>
		</div>


	</div>
</body>
</html>
