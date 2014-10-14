<%@page import="species.utils.Utils"%>
<%@ page contentType="text/html"%>

Bonjour ${name},
<br/><br/>
<g:link url="${uGroup.createLink(controller:'user', action:'show', id:fromUser.id, userGroup:userGroupInstance) }">${fromUser.name.capitalize()}</g:link> vous invite à être le Modérateur du Groupe - <g:link url="${uGroup.createLink(mapping:'userGroup', controller:'userGroup', action:'show', userGroup:userGroupInstance)}">${userGroupInstance.name}</g:link> sur <b>${domain}</b>.
<br/><br/>
<g:if test="${expertsMsg && (expertsMsg != '')}">
<i>${expertsMsg}</i>
<br/><br/>
</g:if>
Merci <a href="${uri}" title="Confirmation code">cliquer ici</a> de confirmer l'invitation.
<br/><br/>
Merci,<br/>
L'équipe du portail "
