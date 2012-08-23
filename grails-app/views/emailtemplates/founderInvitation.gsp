<%@ page contentType="text/html"%>

Hai ${founder.name.capitalize()},

User <g:link controller="SUser" action="show" id="${fromUser.id }">${fromUser.name.capitalize()}</g:link> is inviting you to be a founder for the group <g:link controller="userGroup" action="show" id="${userGroupInstance.id }">${userGroupInstance.name}</g:link> you own on portal <b>domain</b>.
<br/> 

Please <a href="${uri}" title="Confirmation code">click here</a> to confirm the invitation.

Thank you,
The Portal Team