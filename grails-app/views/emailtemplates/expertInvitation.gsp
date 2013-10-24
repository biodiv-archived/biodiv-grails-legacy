<%@page import="species.utils.Utils"%>
<%@ page contentType="text/html"%>

Hi ${name},
<br/><br/>
<g:link url="${uGroup.createLink(controller:'user', action:'show', id:fromUser.id, userGroup:userGroupInstance) }">${fromUser.name.capitalize()}</g:link> is inviting you to be a moderator for the group - <g:link url="${uGroup.createLink(mapping:'userGroup', controller:'userGroup', action:'show', userGroup:userGroupInstance)}">${userGroupInstance.name}</g:link> on <b>${domain}</b>.
<br/>
<g:if test="${expertsMsg && (expertsMsg != '')}">
<br/> 
${expertsMsg}
<br/>
</g:if>
Please <a href="${uri}" title="Confirmation code">click here</a> to confirm the invitation.
<br/><br/>
Thank you,<br/>
The Portal Team