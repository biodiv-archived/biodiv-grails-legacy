<%@ page import="species.groups.SpeciesGroup"%>
<g:javascript src="jquery/jquery.url.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}" />
	
<g:javascript>

$(document).ready(function(){

	
});
</g:javascript>
<div id="speciesGroupFilter" style="clear:both">
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
