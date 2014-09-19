<%@ page contentType="text/html"%>

<g:message code="msg.Hi" /> ${admin.name.capitalize()},
<br/><br/> 
<a href="${requesterUrl}" title="${requester.name}">${requester.name.capitalize()}</a> <g:message code="msg.requested.permission" /> ${invitetype} <g:message code="msg.for" /> ${rankLevel} : ${taxon.name} <g:message code="msg.on.the" /> ${domain}.<br/>
<g:if test="${message}">
<br/>
<g:message code="msg.Message" /> : ${message}
<br/>
</g:if>
<g:message code="msg.Please" /> <a href="${uri}" title="Confirmation code"><g:message code="msg.click.here" /></a> <g:message code="msg.confirm.granting" /> ${invitetype}<g:message code="msg.permission" /> <br/>
<br/><br/>
<g:message code="msg.msg.Thank.you" /><br/>
<g:message code="msg.msg.-The.portal.team" />
