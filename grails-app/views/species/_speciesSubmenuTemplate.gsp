<%@page import="species.NamesMetadata.NamePosition"%>
<%@page import="species.NamesMetadata.NameStatus"%>
<%@page import="species.Species"%>

<g:if test="${speciesInstance}">
<g:set var="featureCount" value="${speciesInstance.featureCount}"/>
</g:if>

<g:if test="${entityName}">
<div class="page-header" style= "position:relative;">
    <g:if test="${speciesInstance}">
    <span class="badge ${(featureCount>0) ? 'featured':''}" style="left:-50px;"   title="${(featureCount>0) ? g.message(code:'text.featured'):''}">
    </span>
    </g:if>

    <div class="pull-right">
     	<g:if test="${isSpeciesContributor}">
	        <a id="editSpecies" class="btn btn-primary pull-right" style="margin-right: 5px;"
	            href="${uGroup.createLink(controller:'species', action:'edit', id:speciesInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
	            <i class="icon-edit"></i><g:message code="button.edit" /></a>
	   </g:if>
       <sUser:isAdmin>
            <g:if test="${speciesInstance}">
            <a id="deleteSpecies" class="btn btn-danger btn-primary pull-right" style="margin-right: 5px;"
                href="${uGroup.createLink(controller:'species', action:'deleteSpecies', id:speciesInstance.id)}"
                ><i class="icon-trash"></i><g:message code="button.delete" /></a>
            </g:if>
        </sUser:isAdmin>

    </div>

    <g:if test="${speciesInstance}">
    <% def status = speciesInstance.taxonConcept.status.label(); %>
    <g:if test="${speciesInstance.taxonConcept.status == NameStatus.SYNONYM}">
    <% status += ' of ';%>
    <g:each in="${speciesInstance.taxonConcept.fetchAcceptedNames()}" var="acceptedName">
    <% def s= acceptedName.findSpecies()%>
    <g:if test="${s}">
    <% status += "<a href='"+uGroup.createLink(controller:'species', action:'show', id:s.id)+"'>"+acceptedName.italicisedForm+"</a>"%> 
    </g:if>
    <g:else>
    <% status += acceptedName.italicisedForm%> 
    </g:else>
    </g:each>
    </g:if>

    <s:showHeadingAndSubHeading model="['heading':entityName, 'subHeading':subHeading, 'headingClass':headingClass, 'subHeadingClass':subHeadingClass, position:speciesInstance.taxonConcept.position, status:status, taxon:speciesInstance.taxonConcept]"/>		
        </g:if>
    </div>
</g:if>

<g:hasErrors bean="${speciesInstance}">
	<i class="icon-warning-sign"></i>
	<span class="label label-important"> <g:message
			code="fix.errors.before.proceeding" default="Fix errors" /> </span>
	<%--<g:renderErrors bean="${observationInstance}" as="list" />--%>
</g:hasErrors>
