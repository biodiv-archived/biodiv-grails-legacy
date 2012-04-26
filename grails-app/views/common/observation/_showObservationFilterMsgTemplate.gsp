<%@page import="species.auth.SUser"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>

<div class="info-message" id="info-message">
	<span class="name" style="color: #b1b1b1;"> <i
		class="icon-screenshot"></i> ${observationInstanceTotal} </span>
	Observation<g:if test="${observationInstanceTotal>1}">s</g:if>
	<g:if test="${queryParams.groupId}">
                                    of <span class="highlight">
			<g:link controller="observation" action="list" params="[sGroup: queryParams.groupId]">${SpeciesGroup.get(queryParams.groupId).name}</g:link> </span> group
                            </g:if>
	<g:if test="${queryParams.habitat}">
                                    in <span class="highlight"><g:link controller="observation" action="list" params="[habitat: queryParams.habitat]">
			${Habitat.get(queryParams.habitat).name}</g:link> </span> habitat
                            </g:if>
	<g:if test="${queryParams.tag}">
                                    tagged <span
			class="highlight"> <g:link controller="observation" action="list" params="[tag: queryParams.tag]">${queryParams.tag}</g:link> </span>
	</g:if>
	<g:if test="${queryParams.user}">
                                    by user <span
			class="highlight"> <g:link controller="SUser" action="show" id="${queryParams.user}"> ${SUser.read(queryParams.user).name.encodeAsHTML()}</g:link> </span>
	</g:if>
</div>
