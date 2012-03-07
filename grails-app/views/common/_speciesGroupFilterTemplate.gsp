<%@ page import="species.groups.SpeciesGroup"%>
<g:javascript src="jquery/jquery.url.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}" />
	
<g:javascript>

$(document).ready(function(){

	$( "#speciesGroupFilter" ).buttonset();
	$('#speciesGroupFilter label[value$="${params.sGroup}"]').each (function() {
			$(this).attr('aria-pressed', 'true').addClass('ui-state-hover').addClass('ui-state-active');
	});
	
	
	function updateGallery(target) {
		var a = $('<a href="'+target+'"></a>');
		var url = a.url();
		var params = url.param();
		var grp = ''; 
		$('#speciesGroupFilter label').each (function() {
			if($(this).attr('aria-pressed') === 'true') {
				grp += $(this).attr('value') + ',';
			}
		});
		
		grp = grp.replace(/\s*\,\s*$/,'');
		if(grp) {
			params['sGroup'] = grp;//$('#speciesGalleryFilter option:selected').val().toString();
		}
		
		console.log("group == " + params['sGroup']);
		 
		var carousel = jQuery('#carousel_${carousel_id}').data('jcarousel');
		reloadCarousel(carousel, "speciesGroup", params['sGroup']);
	}
	
	$('#speciesGroupFilter input').change(function(){
		updateGallery(window.location.pathname + window.location.search);
		return false;
	});
	
	//Ref: http://stackoverflow.com/questions/1421584/how-can-i-simulate-a-click-to-an-anchor-tag/1421968#1421968
	function fakeClick(event, anchorObj) {
	  if (anchorObj.click) {
	    anchorObj.click()
	  } else if(document.createEvent) {
	    if(event.target !== anchorObj) {
	      var evt = document.createEvent("MouseEvents"); 
	      evt.initMouseEvent("click", true, true, window, 
	          0, 0, 0, 0, 0, false, false, false, false, 0, null); 
	      var allowDefault = anchorObj.dispatchEvent(evt);
	      // you can check allowDefault for false to see if
	      // any handler called evt.preventDefault().
	      // Firefox will *not* redirect to anchorObj.href
	      // for you. However every other browser will.
	    }
	  }
	}
	
	$('li.poor_species_content').hover(function(){
		$(this).children('.poor_species_content').slideDown(200);
	}, function(){
		$(this).children('.poor_species_content').slideUp(200);
	}
	);
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
