<%@page import="species.Habitat.HabitatType"%>
<%@page import="species.utils.ImageType"%>
<%@ page import="species.groups.SpeciesGroup"%>
<g:javascript src="jquery/jquery.url.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}" />

<g:javascript>

$(document).ready(function(){

	$("#speciesGroupFilter").buttonset();
	$("#speciesGroupFilter label").each(function() {
		$(this).hover(function() {
			$(this).css('backgroundPosition', '0px -50px');
		}, function() {
			if($(this).attr('value') == '${params.sGroup}') {
				$(this).css('backgroundPosition', '0px -100px');
			} else {
				$(this).css('backgroundPosition', '0px 0px');
			}
		});
	})
		
	$('#speciesGroupFilter label[value$="${params.sGroup}"]').attr('aria-pressed', 'true').addClass('ui-state-hover').addClass('ui-state-active').css('backgroundPosition', '0px -100px');
	
	
	$("#habitatFilter").buttonset();
	$("#habitatFilter label").each(function() {
		$(this).hover(function() {
			$(this).css('backgroundPosition', '0px -50px');
		}, function() {
			if($(this).attr('value') == '${params.habitat}') {
				$(this).css('backgroundPosition', '0px -100px');
			} else {
				$(this).css('backgroundPosition', '0px 0px');
			}
		});
	})
	
	$('#habitatFilter label[value$="${params.habitat}"]').attr('aria-pressed', 'true').addClass('ui-state-hover').addClass('ui-state-active').css('backgroundPosition', '0px -100px');
	
	

});
</g:javascript>
<div id="speciesGroupFilter" class="filterBar" style="clear: both;">
	<!-- g:paginateOnSpeciesGroup/-->
	<%def othersGroup = SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.OTHERS)%>
	<g:each in="${SpeciesGroup.list() }" var="sGroup" status="i">
		<g:if test="${sGroup != othersGroup }">
			<input type="radio" name="specuesGroupFilter"
				id="speciesGroupFilter${i}" value="${sGroup.id }" />
			<label for="speciesGroupFilter${i}" value="${sGroup.id}" title="${sGroup.name}" class="group_icon"
						style="background: url('${createLinkTo(dir: 'images', file: sGroup.icon(ImageType.SMALL)?.fileName?.trim(), absolute:true)}') no-repeat; background-position: 0 0; width: 50px; height: 50px; margin-left: 6px;">				
			</label>
		</g:if>

	</g:each>

	<input type="radio" name="specuesGroupFilter" id="specuesGroupFilter20"
		value="${othersGroup.id}" />
	<label for="speciesGroupFilter20" value="${othersGroup.id}" title="${othersGroup.name}" class="group_icon"
						style="background: url('${createLinkTo(dir: 'images', file: othersGroup.icon(ImageType.SMALL)?.fileName?.trim(), absolute:true)}') no-repeat; background-position: 0 0; width: 50px; height: 50px; margin-left: 6px;">				
	</label>

</div>
<div id="habitatFilter" class="filterBar" style="clear: both">
	<%def othersHabitat = species.Habitat.findByName(HabitatType.OTHERS.value())%>
	<g:each in="${species.Habitat.list()}" var="habitat" status="i">
		<g:if test="${habitat.id != othersHabitat.id }">
			<input type="radio" name="habitatFilter" id="habitatFilter${i}"
				value="${habitat.id}" style="display: none" />
			<label for="habitatFilter${i}" value="${habitat.id}" title="${habitat.name}" class="group_icon"
							style="background: url('${createLinkTo(dir: 'images', file: habitat.icon(ImageType.SMALL)?.fileName?.trim(), absolute:true)}') no-repeat; background-position: 0 0; width: 50px; height: 50px; margin-left: 6px;">				
			</label>
		</g:if>
	</g:each>
	<input type="radio" name="habitatFilter" id="habitatFilter20"
			value="${othersHabitat.id}" style="display: none" />
		<label for="habitatFilter20" value="${othersHabitat.id}" title="${othersHabitat.name}" class="group_icon"
						style="background: url('${createLinkTo(dir: 'images', file: othersHabitat.icon(ImageType.SMALL)?.fileName?.trim(), absolute:true)}') no-repeat; background-position: 0 0; width: 50px; height: 50px; margin-left: 6px;">				
		</label>
</div>


