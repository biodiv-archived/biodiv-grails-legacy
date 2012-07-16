<%@ page import="species.groups.SpeciesGroup"%>
<g:javascript src="jquery/jquery.url.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}" />

<g:javascript>

$(document).ready(function(){
	$( "#speciesGroupFilter" ).buttonset();
		
	$('#speciesGroupFilter label[value$="${params.sGroup}"]').each (function() {
			$(this).attr('aria-pressed', 'true').addClass('ui-state-hover').addClass('ui-state-active');
	});
	
	$( "#habitatFilter" ).buttonset();
	
	$('#habitatFilter label[value$="${params.habitat}"]').each (function() {
			$(this).attr('aria-pressed', 'true').addClass('ui-state-hover').addClass('ui-state-active');
	});
		
});
</g:javascript>
<div id="speciesGroupFilter" class="filterBar"  style="clear: both">
	<center>
		<!-- g:paginateOnSpeciesGroup/-->
		<%def othersIds = "" %>
		<g:each in="${SpeciesGroup.list() }" var="sGroup" status="i">
			<g:if
				test="${sGroup.name.equals('Animals') || sGroup.name.equals('Arachnids') || sGroup.name.equals('Archaea') || sGroup.name.equals('Bacteria') || sGroup.name.equals('Chromista') || sGroup.name.equals('Viruses') || sGroup.name.equals('Kingdom Protozoa') || sGroup.name.equals('Mullusks') || sGroup.name.equals('Others')}">
				<%othersIds += sGroup.id+',' %>
			</g:if>
			<g:else>
				<input type="radio" name="specuesGroupFilter"
					id="specuesGroupFilter${i}" value="${sGroup.id }"
					style="display: none" />
				<label for="specuesGroupFilter${i}" value="${sGroup.id}"><img
					class="group_icon"
					src="${createLinkTo(dir: 'images', file: sGroup.icon()?.fileName?.trim(), absolute:true)}"
					title="${sGroup.name}" /> </label>
			</g:else>
		</g:each>
		<%sGroup = SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.OTHERS)%>
		<input type="radio" name="specuesGroupFilter"
			id="specuesGroupFilter20" value="${sGroup.id}" /> <label
			for="specuesGroupFilter20" value="${sGroup.id}"><img
			class="group_icon"
			src="${createLinkTo(dir: 'images', file: sGroup.icon()?.fileName?.trim(), absolute:true)}"
			title="${sGroup.name}" /> </label>
	</center>

</div>
<div id="habitatFilter" class="filterBar"  style="clear: both">
		<!-- g:paginateOnhabitat/-->
		<g:each in="${species.Habitat.list()}" var="habitat" status="i">
				<input type="radio" name="habitatFilter"
					id="habitatFilter${i}" value="${habitat.name}"
					style="display: none" />
				<label for="habitatFilter${i}" value="${habitat.name}"><img
					class="group_icon"
					src="${resource(dir:'images/group_icons',file:'All.png', absolute:true)}"
					title="${habitat.name}" /> </label>
		</g:each>
</div>

<div style="float: right;">
Sort by <select name="observationSort" id="observationSort"
		class="value ui-corner-all">
		<option value="createdOn" 
			${params.sort?.equals('createdOn')?'selected':''  }>Latest</option>
		<!-- option value="visitCount"
			${params.sort?.equals('visitCount')?'selected':'' }>Most Viewed</option -->
		</select>
</div>