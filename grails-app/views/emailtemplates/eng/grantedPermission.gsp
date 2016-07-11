<%@ page contentType="text/html"%>

<%@page import="species.ScientificName.TaxonomyRank"%>

Hi ${user.name.capitalize()},
<br/><br/> 


You have been granted permission as a ${permissionType.toLowerCase()} on species pages for the taxonomic level ${TaxonomyRank.list()[taxonConcept.rank].value()} : ${taxonConcept.name} on the ${domain}.<br/>

Please click <a href="${uGroup.createLink(controller:'species', action:'create', absolute:true)}">here</a> to begin creating content. 

<br/><br/>
Thank you,<br/>
The Portal Team
