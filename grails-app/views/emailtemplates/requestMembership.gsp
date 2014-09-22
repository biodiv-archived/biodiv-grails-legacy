<%@ page contentType="text/html"%>

<g:message code="msg.Hi" /> ${founder.name.capitalize()},
<br/><br/>
User <g:link url="${uGroup.createLink(controller:'user', action:'show', id:user.id, userGroup:userGroupInstance) }">${user.name.capitalize()}</g:link> <g:message code="msg.requesting.member" /> <g:link url="${uGroup.createLink(mapping:'userGroup', controller:'userGroup', action:'show', userGroup:userGroupInstance)}">${userGroupInstance.name}</g:link>,<g:message code="msg.moderate.on" />  <b>${domain}</b>.
<br/> 

<g:message code="msg.Please" />  <a href="${uri}" title="Confirmation code"><g:message code="msg.click.here" /></a> <g:message code="msg.confirm.membership" /><br/>
<g:message code="msg.communicate.directly" /> <g:link url="${uGroup.createLink(controller:'user', action:'show', id:user.id, userGroup:userGroupInstance) }">${user.name.capitalize()}</g:link> <g:message code="msg.at" /> ${user.email}.
<br/><br/>
<g:message code="msg.msg.Thank.you" /><br/>
<g:message code="msg.msg.-The.portal.team" />
