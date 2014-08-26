<%@page import="species.participation.Recommendation"%>
<%@page import="species.participation.Observation"%>
<%@page import="species.SpeciesField"%>

<div class="yj-context ellipsis"><g:message code="msg.comment.on" />  
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
		<g:message code="msg.Observation" />
	</g:elseif>
	<g:elseif test="${commentInstance.commentHolderType == SpeciesField.class.getName() || commentInstance.commentHolderType.startsWith('species_')}" >
		<g:message code="msg.species.field" />
	</g:elseif>
	<g:else>
		<g:message code="msg.media" />
	</g:else>
</div>
