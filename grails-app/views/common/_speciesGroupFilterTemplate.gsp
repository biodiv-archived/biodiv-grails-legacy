<%@page import="species.Habitat.HabitatType"%>
<%@page import="species.utils.ImageType"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<style type="text/css">
.div_fir{
    margin: 5px 0px 0px 0px;
}
.trait_btn{

    margin: 0;
    padding: 0;
    height: 40px;

}
.svg_wrap{
    width: 50px;
    float: left;
}
.trait_label{ margin-left:50px;position:absolute;margin-top:10px; word-wrap: break-word;float:left;}

.traitFilter{  
	border: 1px solid #ccc;
  	padding: 5px;
 }
 .traitFilter h6{
 	margin:0px;
 	line-height: 12px;
 }
 .traitFilter .span2{
 	height:36px;
 }
 .ellipsis_trait {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
}
</style>
<script type="text/javascript">

$(document).ready(function(){
	$("#speciesGroupFilter").button();
	$('#speciesGroupFilter button[value="${params.sGroup}"]').addClass('active');
        $('#speciesGroupFilter button').tooltip({placement:'top'});
	
	
	$("#habitatFilter").button();
	$('#habitatFilter button[value="${params.habitat}"]').addClass('active');
	$('#habitatFilter button').tooltip({placement:'bottom'});

	$(".traitFilter").button();
	$(".traitFilter button").tooltip({placement:'bottom'});
    <g:each in="${params.trait}" var="t">
        $('.traitFilter button[data-tvid="${t.value}"][data-tid="${t.key}"]').addClass('active btn-success');
    </g:each>
});

</script>

<g:each in="${filters}" var="filter" >
<div class="traitFilter" data-toggle="buttons-radio">
	<h6>${filter.key}</h6>
	<g:render template="/trait/showTraitValuesListTemplate" model="['traitValues':filter.value]"/>
</div>
</g:each>

<g:if test="${!filters}">
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
</g:if>
<g:if test="${!filters && (params.controller != 'species') && !hideHabitatFilter}">
	<div id="habitatFilter" data-toggle="buttons-radio">
		<%def othersHabitat = species.Habitat.findByName(HabitatType.OTHERS.value())%>
		<g:each in="${species.Habitat.list()}" var="habitat" status="i">
			<g:if test="${habitat.id != othersHabitat.id }">
				<button class="btn habitats_sprites ${habitat.iconClass()}"
					id="${"habitat_" + habitat.id}" value="${habitat.id}"
					title="${habitat.name}"
					data-content="${message(code: 'habitat.definition.' + habitat.name)}"
					rel="tooltip" data-original-title="${g.message(code:'speciesgroupfilter.title.a')}"></button>
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
<%--	<div id="observationAllChecklistFilter" class="btn-group"--%>
<%--		style="float: right; margin-right: 5px; z-index: 10; position: absolute; margin-top: -65px; right: 250px;">--%>
<%--		<input type="text" id="observationAllChecklistFilter"--%>
<%--			value="${params.isChecklistOnly}" style="display: none" />--%>
<%--		<button id="observationAllButton" class="btn"--%>
<%--			rel="tooltip" data-original-title="${g.message(code:'speciesgroupfilter.title.show.all')}"><g:message code="default.all.label" /></button>--%>
<%--		<button id="observationChecklistOnlyButton" class="btn" rel="tooltip"--%>
<%--			data-original-title="${g.message(code:'speciesgroupfilter.title.show.only')}"><g:message code="default.checklist.label" /></button>--%>
<%--	</div>--%>

	<div style="height:30px;">
	<g:if test="${!params.isChecklistOnly}">
		<div id="observationMediaFilter" class="btn-group"
			style="float: right;">
			<input type="text" id="observationMediaFilter"
				value="${params.isMediaFilter}" style="display: none" />
			<button id="observationMediaAllFilterButton" class="btn"
				rel="tooltip" data-original-title="${g.message(code:'speciesgroupfilter.title.show.all')}"><g:message code="default.all.label" /></button>
			<button id="observationMediaOnlyFilterButton" class="btn" rel="tooltip"
				data-original-title="${g.message(code:'speciesgroupfilter.title.show.withmedia')}"><g:message code="button.withmedia" /></button>
		</div>

		<div id="speciesNameFilter" class="btn-group"
			style="float: right;">
			<input type="text" id="speciesNameFilter"
				value="${params.speciesName}" style="display: none" />
			<button id="speciesNameAllButton" class="btn" rel="tooltip"
				data-original-title="${g.message(code:'speciesgroupfilter.title.show.all')}"><g:message code="default.all.label" /></button>
			<button id="speciesNameFilterButton" class="btn" rel="tooltip"
				data-original-title="${g.message(code:'speciesgroupfilter.title.show.unidentified')}"><g:message code="button.unidentified" /></button>
		</div>
		
		<div id="observationFlagFilter" class="btn-group"
			style="float: right;">
			<input type="text" id="observationFlagFilter"
				value="${params.isFlagged}" style="display: none" />
			<button id="observationWithNoFlagFilterButton" class="btn"
				rel="tooltip" data-original-title="${g.message(code:'speciesgroupfilter.title.show.all')}"><g:message code="default.all.label" /></button>
			<button id="observationFlaggedButton" class="btn" rel="tooltip"
				data-original-title="${g.message(code:'speciesgroupfilter.title.show.flagged')}"><g:message code="button.flagged" /></button>
		</div>
	</g:if>
	<g:else>
		<div id="areaFilter" class="btn-group"
			style="float: right;">
			<input type="text" id="areaFilter"
				value="${params.areaFilter}" style="display: none" />
			<button id="allAreaButton" class="btn" rel="tooltip"
				data-original-title="${g.message(code:'speciesgroupfilter.title.show.allArea')}"><g:message code="default.all.label" /></button>
			<button id="localAreaButton" class="btn" rel="tooltip"
				data-original-title="${g.message(code:'speciesgroupfilter.title.show.localArea')}"><g:message code="default.local.label" /></button>
			<button id="regionAreaButton" class="btn" rel="tooltip"
				data-original-title="${g.message(code:'speciesgroupfilter.title.show.regionalArea')}"><g:message code="default.regional.label" /></button>
			<button id="countryAreaButton" class="btn" rel="tooltip"
				data-original-title="${g.message(code:'speciesgroupfilter.title.show.countryArea')}"><g:message code="default.country.label" /></button>
		</div>
	
	
	</g:else>
	</div>
</g:if>

