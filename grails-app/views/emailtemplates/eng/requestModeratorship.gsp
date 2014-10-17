<%@ page contentType="text/html"%>

Hi ${founder.name.capitalize()},
<br/><br/>
User <g:link url="${uGroup.createLink(controller:'user', action:'show', id:user.id, userGroup:userGroupInstance) }">${user.name.capitalize()}</g:link> is requesting to be moderator in a group - <g:link url="${uGroup.createLink(mapping:'userGroup', controller:'userGroup', action:'show', userGroup:userGroupInstance)}">${userGroupInstance.name}</g:link>, that you moderate on <b>${domain}</b>.
<br/><br/> 
<g:if test="${message && message != ''}">
<i>${message}</i>
<br/><br/>
</g:if>
Please <a href="${uri}" title="Confirmation code">click here</a> to confirm the moderator.<br/>
You may communicate directly with <g:link url="${uGroup.createLink(controller:'user', action:'show', id:user.id, userGroup:userGroupInstance) }">${user.name.capitalize()}</g:link> at ${user.email}.
<br/><br/>
Thank you,<br/>
The Portal Team
