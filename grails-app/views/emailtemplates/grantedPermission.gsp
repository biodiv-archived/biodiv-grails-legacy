<%@ page contentType="text/html"%>

<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>

<g:message code="msg.Hi" /> ${user.name.capitalize()},
<br/><br/> 


<g:message code="msg.granted.permission" />${permissionType.toLowerCase()} <g:message code="msg.on.taxonomic.level" /> ${g.message(error:TaxonomyRank.list()[taxonConcept.rank])} : ${taxonConcept.name} <g:message code="msg.on.the" /> ${domain}.<br/>

<g:message code="msg.Please.click" /> <g:link href="${uGroup.createLink(controller:'species', action:'create', absolute:true)}"><g:message code="msg.here" /></g:link> <g:message code="msg.to.begin" />  

<br/><br/>
<g:message code="msg.msg.Thank.you" /><br/>
<g:message code="msg.msg.-The.portal.team" />
