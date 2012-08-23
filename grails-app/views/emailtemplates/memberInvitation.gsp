<%@ page contentType="text/html"%>

Hai ${member.name.capitalize()},

User <g:link controller="SUser" action="show" id="${fromUser.id }">${fromUser.name.capitalize()}</g:link> is inviting you to be a member in this interesting group <g:link controller="userGroup" action="show" id="${userGroupInstance.id }">${userGroupInstance.name}</g:link> on the portal <b>domain</b>.
<br/> 

Please <a href="${uri}" title="Confirmation code">click here</a> to confirm the invitation.

Thank you,
The Portal Team