<%@ page contentType="text/html"%>

<g:message code="msg.Hi" /> ${curator.name.capitalize()},
<br/><br/> 
<g:message code="msg.invited.as" /> ${invitetype} for ${rankLevel} : ${taxon.name} <g:message code="msg.on.the" /> ${domain}.<br/>
<g:if test="${message}">
<br/>
<g:message code="msg.Message" /> : ${message}
<br/>
</g:if>
<g:message code="msg.Please" /> <a href="${uri}" title="Confirmation code"><g:message code="msg.click.here" /></a><g:message code="msg.to.accept" />  ${invitetype}.<br/>
<br/><br/>
<g:message code="msg.msg.Thank.you" /><br/>
<g:message code="msg.msg.-The.portal.team" />
