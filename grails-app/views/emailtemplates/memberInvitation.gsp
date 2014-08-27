<%@ page contentType="text/html"%>

<g:message code="msg.Hi" /> ${name},
<br/><br/>
<g:link url="${uGroup.createLink(controller:'user', action:'show', id:fromUser.id, userGroup:userGroupInstance) }">${fromUser.name.capitalize()}</g:link> <g:message code="msg.inviting.member" /> <g:link url="${uGroup.createLink(mapping:'userGroup', controller:'userGroup', action:'show', userGroup:userGroupInstance)}">${userGroupInstance.name}</g:link><g:message code="msg.on" />  <b>${domain}</b>.
<br/><br/> 
<g:if test="${memberMsg && (memberMsg != '')}">
<i>${memberMsg}</i>
<br/><br/>
</g:if>
<g:message code="msg.Please" /> <a href="${uri}" title="Confirmation code"><g:message code="msg.click.here" /></a> <g:message code="msg.to.confirm" />
<br/><br/>
<g:message code="msg.msg.Thank.you" /><br/>
<g:message code="msg.msg.-The.portal.team" />
