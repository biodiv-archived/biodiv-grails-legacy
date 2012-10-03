<%@page import="species.participation.Recommendation"%>
<%@page import="species.participation.Observation"%>

<span class="yj-context">Comment on  
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
		observation
	</g:elseif>
	<g:else>
		media
	</g:else>
</span>