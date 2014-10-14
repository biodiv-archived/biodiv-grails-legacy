<%@ page contentType="text/html"%>

Bonjour ${curator.name.capitalize()},
<br/><br/> 
Vous avez été invité en tant que ${invitetype} pour ${rankLevel} : ${taxon.name} sur le ${domain}.<br/>
<g:if test="${message}">
<br/>
Message : ${message}
<br/>
</g:if>
Merci <a href="${uri}" title="Confirmation code">cliquer ici</a> pour accepter d'être  ${invitetype}.<br/>
<br/><br/>
Merci,<br/>
L'équipe du portail "
