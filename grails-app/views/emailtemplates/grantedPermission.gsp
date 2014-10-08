<%@ page contentType="text/html"%>

<%@page import="species.ScientificName.TaxonomyRank"%>

<g:message code="msg.Hi" /> ${user.name.capitalize()},
<br/><br/> 


<g:message code="msg.granted.permission" />${permissionType.toLowerCase()} <g:message code="msg.on.taxonomic.level" /> ${g.message(error:TaxonomyRank.list()[taxonConcept.rank])} : ${taxonConcept.name} <g:message code="msg.on.the" /> ${domain}.<br/>

Please click <g:link url="${uGroup.createLink(controller:'species', action:'create', userGroup:userGroupInstance, absolute:true)}">here</g:link> to begin creating content. 

<br/><br/>
<g:message code="msg.msg.Thank.you" /><br/>
<g:message code="msg.msg.-The.portal.team" />
