<%@page import="species.Habitat.HabitatType"%>
<%@page import="species.utils.ImageType"%>
<%@ page import="species.groups.SpeciesGroup"%>
<g:javascript src="jquery/jquery.url.js"/>

<g:javascript>

$(document).ready(function(){
	$("#speciesGroupFilter").button();
	$("#speciesGroupFilter button").each(function() {
		$(this).hover(function() {
			$(this).css('backgroundPosition', '0px -32px');
		}, function() {
			if(!$(this).hasClass('active')) {
				$(this).css('backgroundPosition', '0px 0px');
			}else{
				$(this).css('backgroundPosition', '0px -64px');
			}
		});
	})
	$('#speciesGroupFilter button[value$="${params.sGroup}"]').addClass('active').css('backgroundPosition', '0px -64px');
    $('#speciesGroupFilter button').tooltip({placement:'top'});
	
	
	$("#habitatFilter").button();
	$("#habitatFilter button").each(function() {
		$(this).hover(function() {
			$(this).css('backgroundPosition', '0px -32px');
		}, function() {
			if(!$(this).hasClass('active')) {
				$(this).css('backgroundPosition', '0px 0px');
			}else{
				$(this).css('backgroundPosition', '0px -64px');
			}
		});
	})
	
	$('#habitatFilter button[value$="${params.habitat}"]').addClass('active').css('backgroundPosition', '0px -64px');
	$('#habitatFilter button').tooltip({placement:'bottom'});
		
});

</g:javascript>
<div class="class="btn-group">
<div id="speciesGroupFilter" 
	data-toggle="buttons-radio">
	<%def othersGroup = SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.OTHERS)%>
	<g:each in="${SpeciesGroup.list() }" var="sGroup" status="i">
		<g:if test="${sGroup != othersGroup }">
			<button class="btn" value="${sGroup.id}"
				title="${sGroup.name}"
				style="background-image: url('${createLinkTo(dir: 'images', file: sGroup.icon(ImageType.SMALL)?.fileName?.trim(), absolute:true)}'); background-position: 0 0; width: 32px; height: 32px; "></button>
		</g:if>

	</g:each>
	<button class="btn " value="${othersGroup.id}"
		title="${othersGroup.name}" 
		style="background-image: url('${createLinkTo(dir: 'images', file: othersGroup.icon(ImageType.SMALL)?.fileName?.trim(), absolute:true)}'); background-position: 0 0; width: 32px; height: 32px; "></button>
</div>



<div id="habitatFilter" data-toggle="buttons-radio">
	<%def othersHabitat = species.Habitat.findByName(HabitatType.OTHERS.value())%>
	<g:each in="${species.Habitat.list()}" var="habitat" status="i">
		<g:if test="${habitat.id != othersHabitat.id }">
			<button class="btn " value="${habitat.id}"
				title="${habitat.name}"
				style="background-image: url('${createLinkTo(dir: 'images', file: habitat.icon(ImageType.SMALL)?.fileName?.trim(), absolute:true)}'); background-position: 0 0; width: 32px; height: 32px; "
				data-content="${message(code: 'habitat.definition.' + habitat.name)}"
				rel="tooltip" data-original-title="A Title"></button>
		</g:if>
	</g:each>
	<button class="btn" value="${othersHabitat.id}"
		title="${othersHabitat.name}" class="group_icon"
		style="background-image: url('${createLinkTo(dir: 'images', file: othersHabitat.icon(ImageType.SMALL)?.fileName?.trim(), absolute:true)}'); background-position: 0 0; width: 32px; height: 32px; "
		data-content="${message(code: 'habitat.definition.' + othersHabitat.name)}"
		rel="tooltip"></button>
</div>


</div>
