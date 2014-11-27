<%@ page contentType="text/html"%>

Bonjour ${founder.name.capitalize()},
<br/><br/>
L'utilisateur <g:link url="${uGroup.createLink(controller:'user', action:'show', id:user.id, userGroup:userGroupInstance) }">${user.name.capitalize()}</g:link> demande à être modératuer du Groupe  - <g:link url="${uGroup.createLink(mapping:'userGroup', controller:'userGroup', action:'show', userGroup:userGroupInstance)}">${userGroupInstance.name}</g:link>, que vous modérez <b>${domain}</b>.
<br/><br/> 
<g:if test="${message && message != ''}">
<i>${message}</i>
<br/><br/>
</g:if>
Merci <a href="${uri}" title="Confirmation code">cliquer ici</a> de confirmer le modérateur.<br/>
Vous pouvez communiquer directement avec  <g:link url="${uGroup.createLink(controller:'user', action:'show', id:user.id, userGroup:userGroupInstance) }">${user.name.capitalize()}</g:link> at ${user.email}.
<br/><br/>
Merci,<br/>
L'équipe du portail "
