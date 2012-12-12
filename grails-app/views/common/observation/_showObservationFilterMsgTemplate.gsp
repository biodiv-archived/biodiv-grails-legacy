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
</g:javascript>

<div class="info-message" id="info-message">
	<g:if test="${instanceTotal == 0}">
		<search:noSearchResults />
	</g:if>
	<g:else>
		<span class="name" style="color: #b1b1b1;"><i
			class="icon-screenshot"></i> ${instanceTotal} </span>${resultType?:'observation'}<g:if test="${instanceTotal!=1}">s</g:if>




		<g:if test="${queryParams.groupId instanceof Long }">
			<g:if
				test="${queryParams.groupId && SpeciesGroup.get(queryParams.groupId)}">
                                    of <span class="highlight"> <a href="${uGroup.createLink(
						controller:"observation", action:"list",
						params:[sGroup: queryParams.groupId])}">
						${SpeciesGroup.get(queryParams.groupId).name}
						<a href="#" onclick="setDefaultGroup(); return false;">[X]</a>
					</a> </span> group
                            </g:if>
		</g:if>
		<g:elseif test="${queryParams.groupId }">
                           		of <span class="highlight"><a href="${uGroup.createLink(
					mapping:"userGroupGeneric", action:"list",
					params:[sGroup: queryParams.groupId])}">
					${queryParams.groupId }
					<a href="#" onclick="setDefaultGroup(); return false;">[X]</a>
				</a>
			</span> species group
                           </g:elseif>

		<g:if test="${queryParams.habitat instanceof Long }">
			<g:if
				test="${queryParams.habitat && Habitat.get(queryParams.habitat)}">
                                    in <span class="highlight"><a href="${uGroup.createLink(
						controller:"observation", action:"list",
						params:[habitat: queryParams.habitat])}">
						${Habitat.get(queryParams.habitat).name}
						<a href="#" onclick="setDefaultHabitat(); return false;">[X]</a>
					</a> </span> habitat
                            </g:if>
		</g:if>
		<g:elseif test="${queryParams.habitat }">
                           		in <span class="highlight"><a href="${uGroup.createLink(
					mapping:"userGroupGeneric", action:"list",
					params:[habitat: queryParams.habitat])}">
					${queryParams.habitat }
					<a href="#" onclick="setDefaultHabitat(); return false;">[X]</a>
				</a> </span>habitat
                           </g:elseif>
		<g:if test="${queryParams.tag}">
                                    tagged <span class="highlight">
				<a href="${uGroup.createLink(controller:"observation", action:"list",
					params:[tag: queryParams.tag])}">
					${queryParams.tag}
					<a id="removeTagFilter" href="#">[X]</a>
				</a> </span>
		</g:if>
		<g:if test="${queryParams.user && SUser.read(queryParams.user)}">
                                    by user <span class="highlight">
				<a href="${uGroup.createLink(controller:"SUser", action:"show", id:queryParams.user)}">
					${SUser.read(queryParams.user).name.encodeAsHTML()}
					<a id="removeUserFilter" href="#">[X]</a>
				</a> </span>
		</g:if>
		<g:if test="${queryParams.observation}">
                                    for  <span class="highlight">
				<a href="${uGroup.createLink(controller:"observation", action:"show",
					id:queryParams.observation)}">
					observation
					<a id="removeObvFilter" href="#">[X]</a>
				</a> </span>
		</g:if>
		<g:if test="${queryParams.query}">
                                    for search key <span
				class="highlight"> <a href="${uGroup.createLink(controller:"observation",
					action:"search", params:[query: queryParams.query])}">
					${queryParams.query.encodeAsHTML()}
					<a id="removeQueryFilter" href="#">[X]</a>
				</a> </span>
		</g:if>

	</g:else>
</div>
