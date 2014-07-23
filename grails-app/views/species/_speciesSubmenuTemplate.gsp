<g:if test="${speciesInstance}">
<g:set var="featureCount" value="${speciesInstance.featureCount}"/>
</g:if>

<g:if test="${entityName}">
<div class="page-header" style= "position:relative;">
    <g:if test="${speciesInstance}">
    <span class="badge ${(featureCount>0) ? 'featured':''}" style="left:-50px;"   title="${(featureCount>0) ? 'Featured':''}">
    </span>
    </g:if>

    <div class="pull-right">
        <g:if test="${isSpeciesContributor}">
        <a id="editSpecies" class="btn btn-primary pull-right" style="margin-right: 5px;"
            href="${uGroup.createLink(controller:'species', action:'edit', id:speciesInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
            <i class="icon-edit"></i>Edit</a>

        </g:if>

        <sUser:isAdmin>
        <a id="deleteSpecies" class="btn btn-danger btn-primary pull-right" style="margin-right: 5px;"
            href="${uGroup.createLink(controller:'species', action:'delete', id:speciesInstance.id)}"
            ><i class="icon-trash"></i>Delete</a>
        </sUser:isAdmin>

    </div>

    <s:showHeadingAndSubHeading model="['heading':entityName, 'subHeading':subHeading, 'headingClass':headingClass, 'subHeadingClass':subHeadingClass]"/>		
    </div>
</g:if>

<g:hasErrors bean="${speciesInstance}">
	<i class="icon-warning-sign"></i>
	<span class="label label-important"> <g:message
			code="fix.errors.before.proceeding" default="Fix errors" /> </span>
	<%--<g:renderErrors bean="${observationInstance}" as="list" />--%>
</g:hasErrors>
