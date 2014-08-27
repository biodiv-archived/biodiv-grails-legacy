<%@page import="species.utils.Utils"%>
<html>
<head>
<meta name="layout" content="main" />
<title><g:message code="views.error" /></title>
<r:require modules="core" />
</head>

<body>
<%
String supportEmail = "";
String domain = Utils.getDomain(request);
if(domain.equals(grailsApplication.config.wgp.domain)) {
	supportEmail = grailsApplication.config.speciesPortal.wgp.supportEmail;
} else {//if(domain.equals(grailsApplication.config.ibp.domain)) {
	supportEmail =  grailsApplication.config.speciesPortal.ibp.supportEmail;
}
%>

			<div class="observation  span12">
				<p class="message">
					<g:message code="views.problem" /> <br /><g:message code="views.send.message" />  <span class="mailme">${supportEmail }</span>.<br/><g:message code="views.will.work" /> <br />
					<br />
				</p>
				<div class="errors">
					<strong><g:message code="views.error" /> ${request.'javax.servlet.error.status_code'}:</strong>
					${request.'javax.servlet.error.message'.encodeAsHTML()}<br />
				</div>


	</div>
</body>
</html>
