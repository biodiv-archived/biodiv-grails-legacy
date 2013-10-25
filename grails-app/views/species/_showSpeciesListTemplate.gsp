<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.ImageUtils"%>
<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>
<%@ page import="species.Species"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@page import="species.utils.Utils"%>

<div class="observations_list" style="clear: both; top: 0px;">


	<div class="mainContentList">
		<div class="mainContent" name="p${params?.offset}">

			<ul class="grid_view thumbnails"
				style="list-style: none; text-align: left">
				<g:each in="${speciesInstanceList}" status="i" var="speciesInstance">

					<g:if test="${i%6 == 0}">
						<li
							class="clearfix thumbnail ${speciesInstance.percentOfInfo > 0?'rich_species_content':'poor_species_content'}">
					</g:if>
					<g:else>
						<li
							class="thumbnail ${speciesInstance.percentOfInfo > 0?'rich_species_content':'poor_species_content'}">
					</g:else>
		                            <g:render template="/species/showSpeciesSnippetTabletTemplate"
						model="['speciesInstance':speciesInstance, 'obvTitle':speciesInstance.title, 'userGroup':userGroup, canPullResource:canPullResource]"/>
					</li>
				</g:each>
			</ul>



		</div>
	</div>
	<div class="paginateButtons centered">
		<p:paginate controller="species" action="list"
			total="${instanceTotal}" userGroup="${userGroup}"
			userGroupWebaddress="${userGroupWebaddress}" params="${params}"
			max="${params.max }" offset="${params.offset}" maxsteps="10" />
	</div>
	<div class="paginateButtons centered">
		<p:paginateOnAlphabet controller="species" action="list"
			total="${instanceTotal}" userGroup="${userGroup }" params="${params}"
			userGroupWebaddress="${userGroupWebaddress}"/>

	</div>


</div>

