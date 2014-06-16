<%@ page contentType="text/html"%>

<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>

Hi ${user.name.capitalize()},
<br/><br/> 
Congratulations. You now have a permission as ${speciesPermission.permissionType.replace('ROLE_','')} on taxon concept ${TaxonomyRank.list()[speciesPermission.taxonConcept.rank].value()} : ${speciesPermission.taxonConcept.name} on the ${domain}.<br/>
<br/><br/>
Thank you,<br/>
The Portal Team
