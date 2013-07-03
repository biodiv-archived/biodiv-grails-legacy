<%@page import="species.utils.Utils"%>
<button id="searchToggle" class="btn btn-link" type="button" style="${((queryParams?.query)?:((queryParams?.q)?:params.query))?'display:none;':''}"><i class="icon-search"></i></button>

<div id='searchToggleBox' class="input-append" style="${((queryParams?.query)?:((queryParams?.q)?:params.query))?'':'display:none;'}">
	<form method="get"
		action="${uGroup.createLink(controller:controller, action:'search') }"
		id="searchbox" class="navbar-search" style="float: none;">
		<select id="userGroupSelectFilter" class="btn" name="uGroup" style="display:none;">	
			<option value="ALL"> Search in all groups </option>
			<g:if test="${params.webaddress }">
				<option value="THIS_GROUP"> Search within this group </option>
			</g:if>
		</select>
		
		<input type="text" name="query" id="searchTextField"
			value="${((queryParams?.query)?:((queryParams?.q)?:params.query))?.encodeAsHTML()}"
			class="search-query span3" placeholder="Search" />
		
		<button id="search" class="btn btn-link" type="button"><i class="icon-search"></i></button>
		<input type="hidden" name="fl" value="id" />
		<g:hiddenField name="category" value="${controller}" />
		
<%--		<g:hiddenField name="offset" value="0" />--%>
<%--		<g:hiddenField name="max" value="10" />--%>
<%--		<g:hiddenField id="searchBoxSort" name="sort" value="score" />--%>
<%--		<g:hiddenField name="hl" value="true" />--%>
<%--		<g:hiddenField name="hl.fl" value="message" />--%>
<%--		<g:hiddenField name="hl.snippets" value="3" />--%>

	</form>
<div id="nameSuggestionsMain" class="dropdown span3" style="left:-20px;">
			<a class="dropdown-toggle" role="button" data-toggle="dropdown"
			data-target="#" href="#"></a>
		</div>	
</div>
<g:javascript>
$(document).ready(function() {
	window.params = {
		'offset':"${params.offset}",
		'isGalleryUpdate':'true',	
		"queryParamsMax":"${queryParams?.max}",
		'speciesName':"${params.speciesName }",
		'isFlagged':"${params.isFlagged }",
		'nameTermsUrl': "${uGroup.createLink(controller:'search', action: 'nameTerms')}",
		'noImageUrl' : "${createLinkTo(file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}",
		'IBPDomainUrl':"${Utils.getIBPServerDomain()}",
		'searchController' : "${controller}",
		'carousel':{maxHeight:75, maxWidth:75},
                'observation': {},
                'imagesPath': "${resource(dir:'images', absolute:true)}",
                'locationsUrl': "${uGroup.createLink(controller:'observation', action: 'locations')}",
                'defaultMarkerIcon':"${resource(dir:'js/Leaflet/dist/images', file:'')}",
                'observation':{
                    listUrl:"${uGroup.createLink(controller:'observation', action: 'listJSON', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"
                }
	}
	$("#userGroupSelectFilter").val("${(queryParams && queryParams.uGroup)?queryParams.uGroup:(params.webaddress?'THIS_GROUP':'ALL')}");
});
</g:javascript>
