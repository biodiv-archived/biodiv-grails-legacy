<%@ page contentType="text/html"%>

<%@page import="species.ScientificName.TaxonomyRank"%>

Bonjour ${user.name.capitalize()},
<br/><br/> 


Vous avez reçu la permission de  ${permissionType.toLowerCase()} sur les pages espèces au niveau taxonomique ${TaxonomyRank.list()[taxonConcept.rank].value()} : ${taxonConcept.name} sur le ${domain}.<br/>

Merci de cliquer <g:link href="${uGroup.createLink(controller:'species', action:'create', absolute:true)}">ici</g:link> pour commencer à créer du contenu. 

<br/><br/>
Merci,<br/>
L'équipe du portail "
