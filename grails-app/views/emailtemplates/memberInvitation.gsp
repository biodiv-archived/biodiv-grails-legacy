<%@ page contentType="text/html"%>

Hi ${name},
<br/><br/>
<g:link url="${uGroup.createLink(controller:'user', action:'show', id:fromUser.id, userGroup:userGroupInstance) }">${fromUser.name.capitalize()}</g:link> is inviting you to be a member in this group - <g:link url="${uGroup.createLink(mapping:'userGroup', controller:'userGroup', action:'show', userGroup:userGroupInstance)}">${userGroupInstance.name}</g:link> on <b>${domain}</b>.
<br/> 
<g:if test="${memberMsg && (memberMsg != '')}">
<br/> 
${memberMsg}
<br/>
</g:if>
Please <a href="${uri}" title="Confirmation code">click here</a> to confirm the invitation.
<br/><br/>
Thank you,<br/>
The Portal Team