<%@page import="species.Habitat.HabitatType"%>
<%@page import="species.utils.ImageType"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>

<g:javascript>

$(document).ready(function(){
	$("#speciesGroupFilter").button();
	$('#speciesGroupFilter button[value="${params.sGroup}"]').addClass('active');
    $('#speciesGroupFilter button').tooltip({placement:'top'});
	
	
	$("#habitatFilter").button();
	$('#habitatFilter button[value="${params.habitat}"]').addClass('active');
	$('#habitatFilter button').tooltip({placement:'bottom'});
		
});

</g:javascript>
<div id="speciesGroupFilter" data-toggle="buttons-radio">
	<%def othersGroup = SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.OTHERS)%>
	<g:each in="${SpeciesGroup.list() }" var="sGroup" status="i">
		<g:if test="${sGroup != othersGroup }">
			<button class="btn species_groups_sprites ${sGroup.iconClass()}"
				id="${"group_" + sGroup.id}" value="${sGroup.id}"
				title="${sGroup.name}"></button>
		</g:if>

	</g:each>
	<button class="btn species_groups_sprites ${ othersGroup.iconClass()}"
		id="${"group_" + othersGroup.id}" value="${othersGroup.id}"
		title="${othersGroup.name}"></button>
</div>


<g:if test="${(params.controller != 'species' ||  params.controller != 'checklist') && !hideHabitatFilter}">
	<div id="habitatFilter" data-toggle="buttons-radio">
		<%def othersHabitat = species.Habitat.findByName(HabitatType.OTHERS.value())%>
		<g:each in="${species.Habitat.list()}" var="habitat" status="i">
			<g:if test="${habitat.id != othersHabitat.id }">
				<button class="btn habitats_sprites ${habitat.iconClass()}"
					id="${"habitat_" + habitat.id}" value="${habitat.id}"
					title="${habitat.name}"
					data-content="${message(code: 'habitat.definition.' + habitat.name)}"
					rel="tooltip" data-original-title="A Title"></button>
			</g:if>
		</g:each>
		<button class="btn habitats_sprites ${othersHabitat.iconClass()}"
			id="${"habitat_" + othersHabitat.id}" value="${othersHabitat.id}"
			title="${othersHabitat.name}"
			data-content="${message(code: 'habitat.definition.' + othersHabitat.name)}"
			rel="tooltip"></button>
	</div>
</g:if>

<g:if test="${forObservations}">
	<div id="speciesNameFilter" class="btn-group"
		style="float: right; margin-right: 5px; z-index: 10; position: absolute; margin-top: -65px; right: 0;">
		<input type="text" id="speciesNameFilter"
			value="${params.speciesName}" style="display: none" />
		<button id="speciesNameAllButton" class="btn" rel="tooltip"
			data-original-title="Show all observations">All</button>
		<button id="speciesNameFilterButton" class="btn" rel="tooltip"
			data-original-title="Show only unidentified observations">Unidentified</button>
	</div>
	<div id="observationFlagFilter" class="btn-group"
		style="float: right; margin-right: 5px; z-index: 10; position: absolute; margin-top: -30px; right: 0;">
		<input type="text" id="observationFlagFilter"
			value="${params.isFlagged}" style="display: none" />
		<button id="observationWithNoFlagFilterButton" class="btn"
			rel="tooltip" data-original-title="Show all observations">All</button>
		<button id="observationFlaggedButton" class="btn" rel="tooltip"
			data-original-title="Show only flagged observations">Flagged</button>
	</div>
</g:if>

<g:if test="${!hideAdvSearchBar}">
	<div id="advSearchContainer" class="sidebar_section" style="left: 0px; margin: 10px 0px;">
		<a data-toggle="collapse" data-parent="#advSearchContainer" href="#advSearchBox"><h5>
				<i class=" icon-search"></i>Advanced Search
			</h5> </a>
		<div id="advSearchBox" class="collapse">
			<search:advSearch />
		</div>
	</div>
</g:if>
