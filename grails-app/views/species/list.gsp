<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>
<%@ page import="species.Species"%>
<%@ page import="species.groups.SpeciesGroup"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html" />
<meta name="layout" content="main" />

<g:javascript src="jquery/jquery.url.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}" />

<g:set var="entityName"
	value="${message(code: 'species.label', default: 'Species')}" />
<title>Species List</title>
<g:javascript>

$(document).ready(function(){

	$( "#speciesGroupFilter" ).buttonset();
	$('#speciesGroupFilter label[value$="${params.sGroup}"]').each (function() {
			$(this).attr('aria-pressed', 'true').addClass('ui-state-hover').addClass('ui-state-active');
	});
	
	
	function updateGallery(target, limit, offset) {
		var a = $('<a href="'+target+'"></a>');
		var url = a.url();
		var href = url.attr('path');
		var params = url.param();
		params['sort'] = $('#speciesGallerySort option:selected').val()
		//params['order'] = $('#speciesGalleryOrder option:selected').val()
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
		
		if(limit != undefined) {
			params['max'] = limit.toString();
		}
		if(offset != undefined) {
			params['offset'] = offset.toString();
		}
		var recursiveDecoded = decodeURIComponent($.param(params));
		window.location = href+'?'+recursiveDecoded;
	}
	
	$(".paginateButtons a").click(function() {
		updateGallery($(this).attr('href'));
		return false;
	});
	
	$('#speciesGallerySort').change(function(){
		updateGallery(window.location.pathname + window.location.search, ${params.limit?:51}, 0);
		return false;
	});
	
	$('#speciesGroupFilter input').change(function(){
		updateGallery(window.location.pathname + window.location.search, ${params.limit?:51}, 0);
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
});
</g:javascript>
</head>
<body>
	<div class="container_16">

		<div class="paginateButtons grid_16">
			<center>
				<g:paginateOnAlphabet total="${speciesInstanceTotal}" />
			</center>
		</div>
		<br />
		<br />


		<div class="toolbar grid_16" >
			<div id="speciesGroupFilter" style="float: left;">
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
								id="specuesGroupFilter${i}" value="${sGroup.id }" style="display:none" />
							<label for="specuesGroupFilter${i}" value="${sGroup.id}"><img
								class="group_icon"
								src="${createLinkTo(dir: 'images', file: sGroup.icon()?.fileName?.trim(), absolute:true)}"
								title="${sGroup.name}" /> </label>
						</g:else>
					</g:each>
					<%sGroup = SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.OTHERS)%>
					<input type="radio" name="specuesGroupFilter"
						id="specuesGroupFilter20" value="${othersIds}" /> <label
						for="specuesGroupFilter20" value="${othersIds}"><img
						class="group_icon"
						src="${createLinkTo(dir: 'images', file: sGroup.icon()?.fileName?.trim(), absolute:true)}"
						title="${sGroup.name}" /> </label>
				</center>
			</div>

			<div style="float: right;">
				Sort by <select name="speciesGallerySort" id="speciesGallerySort"
					class="value ui-corner-all">
					<option value="title"
						${params.sort?.equals('title')?'selected':'' }>Title</option>
					<option value="percentOfInfo"
						${params.sort?.equals('percentOfInfo')?'selected':''  }>Richness</option>
					<!-- option value="createdOn" ${params.sort?.equals('createdOn')?'selected':''  }>Recently Added</option>
				<option value="updatedOn" ${params.sort?.equals('updatedOn')?'selected':''  }>Recently Updated</option-->
				</select>
				<!-- Filter by group <select name="speciesGalleryFilter"
				id="speciesGalleryFilter" multiple="multiple"
				class="value ui-corner-all">
				<g:each in="${SpeciesGroup.list()}" var="sGroup">
					<option value="${sGroup.id}"
						${params.sGroup.equals(sGroup)?'selected':''  }>
						${sGroup.name}
					</option>
				</g:each>
			</select> -->
			</div>
		</div>
		
		<br /><br/><br/>
		
		<div class="grid_16" align="center">
			<g:set var="columnSize"
				value="${Math.ceil(speciesInstanceList.size()/3)}" />
			<g:each in="${speciesInstanceList}" status="i" var="speciesInstance">

				<g:if test="${i%columnSize == 0}">
					<ul class="speciesList thumbwrap grid_5"
						style="list-style: none; text-align: left">
				</g:if>
				<li class="grid_5"><g:link action="show"
						id="${speciesInstance.id}">

						<g:set var="mainImage" value="${speciesInstance.mainImage()}" />
						<%def thumbnailPath = mainImage?.fileName?.replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.galleryThumbnail.suffix)%>

						<img class="icon" style="float: right;"
							src="${createLinkTo( base:grailsApplication.config.speciesPortal.resources.serverURL,
											file: thumbnailPath)}"
							title=" ${speciesInstance.taxonConcept.name}" />

						<p class="caption">
							${speciesInstance.taxonConcept.italicisedForm}
						</p>
					</g:link>
				</li>
				<g:if test="${(i+1)%columnSize == 0}">
					</ul>
				</g:if>
			</g:each>
			</ul>
		</div>
		<br />

		<div class="paginateButtons  grid_16">
			<center>
				<g:paginate total="${speciesInstanceTotal}"
					params="['startsWith':params.startsWith]" max="50" maxsteps="10" />
			</center>
		</div>
		<br />

		<div class="paginateButtons grid_16">
			<center>
				<g:paginateOnAlphabet total="${speciesInstanceTotal}" />
			</center>
		</div>

	</div>

</body>
</html>
