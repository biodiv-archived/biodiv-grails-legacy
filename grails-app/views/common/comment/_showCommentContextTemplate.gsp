<%@page import="species.participation.Recommendation"%>
<%@page import="species.participation.Observation"%>
<%@page import="species.SpeciesField"%>

<div class="yj-context ellipsis"><g:message code="default.comment.on.label" />  
	<g:if test="${commentInstance.commentHolderType ==  Recommendation.class.getName()}" >
	<%
		def tmpReco = Recommendation.read(commentInstance.commentHolderId.toLong());
		def tmpSpeciesId = tmpReco?.taxonConcept?.findSpeciesId();
		
	%> species call:
		<g:if test="${tmpSpeciesId != null}">
				<g:link controller="species" action="show" id="${tmpSpeciesId}"><i>${tmpReco.name}</i></g:link>
		</g:if>
		<g:elseif test="${tmpReco.isScientificName}"><i>${tmpReco.name}</i>
		</g:elseif>
		<g:else>
			${tmpReco.name}
		</g:else>
	</g:if>
	<g:elseif test="${commentInstance.commentHolderType == Observation.class.getName()}" >
		<g:message code="default.observation.label" />
	</g:elseif>
	<g:elseif test="${commentInstance.commentHolderType == SpeciesField.class.getName() || commentInstance.commentHolderType.startsWith('species_')}" >
		<g:message code="default.species.field.label" />
	</g:elseif>
	<g:else>
		<g:message code="default.media.label" />
	</g:else>
</div>
