<%@ page contentType="text/html"%>

Bonjour ${founder.name.capitalize()},
<br/><br/>
L'utilisateur <g:link url="${uGroup.createLink(controller:'user', action:'show', id:user.id, userGroup:userGroupInstance) }">${user.name.capitalize()}</g:link> demande à être membre du Groupe - <g:link url="${uGroup.createLink(mapping:'userGroup', controller:'userGroup', action:'show', userGroup:userGroupInstance)}">${userGroupInstance.name}</g:link>, que vous modérez sur <b>${domain}</b>.
<br/> 

Merci <a href="${uri}" title="Confirmation code">cliquer ici</a> de conifrmer l'accord de membre.<br/>
Vous pouvez communiquer directement avec <g:link url="${uGroup.createLink(controller:'user', action:'show', id:user.id, userGroup:userGroupInstance) }">${user.name.capitalize()}</g:link> at ${user.email}.
<br/><br/>
Merci,<br/>
L'équipe du portail "
