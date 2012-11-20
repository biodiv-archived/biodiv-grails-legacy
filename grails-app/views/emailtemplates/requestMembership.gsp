<%@ page contentType="text/html"%>

Hai ${founder.name.capitalize()},
<br/><br/><br/>
User <g:link url="${uGroup.createLink(controller:'user', action:'show', id:user.id, userGroup:userGroupInstance) }">${user.name.capitalize()}</g:link> is requesting membership in one of the group <g:link url="${uGroup.createLink(mapping:'userGroup', controller:'userGroup', action:'show', userGroup:userGroupInstance)}">${userGroupInstance.name}</g:link> you own on portal <b>${domain}</b>.
<br/> 

Please <a href="${uri}" title="Confirmation code">click here</a> to confirm the membership.
<br/><br/><br/>
Thank you,<br/>
The Portal Team
