<%@ page contentType="text/html"%>

Bonjour ${admin.name.capitalize()},
<br/><br/> 
<a href="${requesterUrl}" title="${requester.name}">${requester.name.capitalize()}</a> a demandé l'autorisation d'accès en tant que ${invitetype} pour le ${rankLevel} : ${taxon.name} sur le ${domain}.<br/>
<g:if test="${message}">
<br/>
Message : ${message}
<br/>
</g:if>
Merci <a href="${uri}" title="Confirmation code">cliquer ici</a> pour confirmer l'octroi d'une de ${invitetype} autorisation.<br/>
<br/><br/>
Merci,<br/>
L'équipe du portail "
