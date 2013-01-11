<%@page import="species.auth.SUser"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<g:javascript>
	function setDefaultGroup(){
		var defId = "#group_" + "${SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.ALL).id}"
		$(defId).click();
	}
	function setDefaultHabitat(){
		var defId = "#habitat_" + "${Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id}"
		$(defId).click();
	}
	$(document).ready(function() {
			initRelativeTime("${uGroup.createLink(controller:'activityFeed', action:'getServerTime')}");
	});
</g:javascript>
 
<div class="info-message" id="info-message">
	<g:if test="${instanceTotal == 0}">
		<search:noSearchResults />
	</g:if>
	<g:else>
		<g:if test="${speciesCountWithContent }"><span class="name" style="color: #b1b1b1;"><i
			class="icon-search"></i></span> ${speciesCountWithContent} species page<g:if test="${speciesCountWithContent>1}">s</g:if> and ${instanceTotal- speciesCountWithContent} species stubs are found</g:if>
		<g:else>
		<span class="name" style="color: #b1b1b1;"><i
			class="icon-search"></i></span> ${instanceTotal} 
		${resultType?:'observation'}<g:if test="${instanceTotal>1 && resultType != 'species'}">s</g:if> found 
		</g:else>
		<g:each in="${queryParams}" var="queryParam">
			<g:if
				test="${queryParam.key == 'groupId' && queryParam.value instanceof Long }">
				<g:if
					test="${queryParam.value && SpeciesGroup.get(queryParam.value)}">
                                    of <span class="highlight"> <a
						href="${uGroup.createLink(
						controller:"observation", action:"list",
						params:[sGroup: queryParam.value])}">
							${SpeciesGroup.get(queryParam.value).name} <a href="#"
							onclick="setDefaultGroup(); return false;">[X]</a> </a> </span> group
                            </g:if>
			</g:if>
			<g:elseif test="${queryParam.key == 'groupId' && queryParam.value }">
                           		of <span class="highlight"><a
					href="${uGroup.createLink(
					mapping:"userGroupGeneric", action:"list",
					params:[sGroup: queryParam.value])}">
						${queryParam.value } <a href="#"
						onclick="setDefaultGroup(); return false;">[X]</a> </a> </span> species group
                           </g:elseif>

			<g:if
				test="${queryParam.key == 'habitat' && queryParam.value instanceof Long }">
				<g:if test="${queryParam.value && Habitat.get(queryParam.value)}">
                                    in <span class="highlight"><a
						href="${uGroup.createLink(
						controller:"observation", action:"list",
						params:[habitat: queryParam.value])}">
							${Habitat.get(queryParam.value).name} <a href="#"
							onclick="setDefaultHabitat(); return false;">[X]</a> </a> </span> habitat
                            </g:if>
			</g:if>
			<g:elseif test="${queryParam.key == 'habitat' && queryParam.value}">
                           		in <span class="highlight"><a
					href="${uGroup.createLink(
					mapping:"userGroupGeneric", action:"list",
					params:[habitat: queryParam.value])}">
						${queryParam.value } <a href="#"
						onclick="setDefaultHabitat(); return false;">[X]</a> </a> </span>habitat
                           </g:elseif>
			<g:if test="${queryParam.key == 'tag' && queryParam.value}">
                                    tagged <span class="highlight">
					<a
					href="${uGroup.createLink(controller:"observation", action:"list",
					params:[tag: queryParam.value])}">
						${queryParam.value} <a id="removeTagFilter" href="#">[X]</a> </a> </span>
			</g:if>
			<g:if
				test="${queryParam.key == 'user' && SUser.read(queryParam.value)}">
                                    by user <span class="highlight">
					<a
					href="${uGroup.createLink(controller:"SUser", action:"show", id:queryParam.value)}">
						${SUser.read(queryParam.value).name.encodeAsHTML()} <a
						id="removeUserFilter" href="#">[X]</a> </a> </span>
			</g:if>
			<g:if test="${queryParam.key == 'observation' && queryParam.value}">
                                    for  <span class="highlight">
					<a
					href="${uGroup.createLink(controller:"observation", action:"show",
					id:queryParam.value)}">
						observation <a id="removeObvFilter" href="#">[X]</a> </a> </span>
			</g:if>
			<g:if test="${(queryParam.key == 'query' || queryParam.key == 'q') && queryParam.value}">
                                    for search key <span
					class="highlight"> <a
					href="${uGroup.createLink(controller:"observation",
					action:"search", params:[query: queryParam.value])}">
						${queryParam.value.encodeAsHTML()} <a id="removeQueryFilter"
						href="#">[X]</a> </a> </span>
			</g:if>
		</g:each>
	</g:else>
</div>
