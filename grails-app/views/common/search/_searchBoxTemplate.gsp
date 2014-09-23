<%@page import="species.utils.Utils"%>
<%--<button id="searchToggle" class="btn btn-link" type="button" style="${((queryParams?.query)?:((queryParams?.q)?:params.query))?'display:none;':''}"><i class="icon-search"></i></button>--%>

<div id='searchToggleBox' class="input-append">
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
		
		<button id="search" class="btn btn-link" type="button"><i class="icon-search icon-gray"></i></button>
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
<script type="text/javascript">
$(document).ready(function() {
	window.params = {
                'controller': "${params.controller}",
                'actionForBulkCreate': "${params.action}",
                'offset':"${params.offset}",
                'isGalleryUpdate':'true',
                'resDeleteUrl' : "${uGroup.createLink(controller:'resource', action: 'deleteUsersResourceById')}",
                'getSpeciesFieldMedia' : "${createLink(controller:'species',  action:'getSpeciesFieldMedia')}",
                "queryParamsMax":"${queryParams?.max}",
                'getProcessedImageUrl' : "${createLink(controller:'observation',  action:'getProcessedImageStatus')}",
		'speciesName':"${params.speciesName }",
		'isFlagged':"${params.isFlagged?.toBoolean()?.toString()}",
		'nameTermsUrl': "${uGroup.createLink(controller:'search', action: 'nameTerms')}",
		'noImageUrl' : "${createLinkTo(file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}",
		'dropDownIconUrl' : "${resource(dir:'images', file:'dropdown_active.gif',absolute:'true')}",
		'IBPDomainUrl':"${Utils.getIBPServerDomain()}",
		'searchController' : "${controller}",
		'carousel':{maxHeight:150, maxWidth:150},
                'imagesPath': "${resource(dir:'images', absolute:true)}",
                'locationsUrl': "${uGroup.createLink(controller:'observation', action: 'locations')}",
                'defaultMarkerIcon':"${resource(dir:'js/Leaflet/dist/images', file:'')}",
                'isChecklistOnly':"${params.isChecklistOnly?.toBoolean()?.toString()}",
                'obvListPage' : "${uGroup.createLink(controller:'observation', action:'list','userGroup':userGroup, absolute:true)}",
                'species':{
                    'url':"${uGroup.createLink('controller':'species', action:'show', 'userGroup':userGroupInstance)}",
                    'updateUrl':"${uGroup.createLink(controller:'species', action:'update')}",
                    'deleteUrl':"${uGroup.createLink(controller:'species', action:'delete')}"
                },
                'loginUrl':"${createLink(controller:'login','userGroup':userGroupInstance)}",
                'isLoggedInUrl' : "${createLink(controller:'SUser', action:'isLoggedIn','userGroup':userGroupInstance)}",
                'userTermsUrl' : "${createLink(controller:'SUser', action: 'terms','userGroup':userGroupInstance)}",
                'requestPermissionFormUrl' : "${uGroup.createLink(controller:'species', action: 'requestPermission','userGroup':userGroupInstance)}",
                'inviteFormUrl' : "${uGroup.createLink(controller:'species', action: 'invite','userGroup':userGroupInstance)}",
                'saveModifiedSpecies' : "${uGroup.createLink(controller:'species', action:'saveModifiedSpeciesFile','userGroup':userGroupInstance) }",
                'uploadSpecies' : "${uGroup.createLink(action:'upload', controller:'species', 'userGroup':userGroupInstance)}",
                'downloadFile': "${uGroup.createLink(action:'downloadSpeciesFile', controller:'UFile', 'userGroup':userGroupInstance)}",
                'getDataColumnsDB':  "${uGroup.createLink(action:'getDataColumns', controller:'species', 'userGroup':userGroupInstance)}",
                'getLicenseFromDB' :  "${uGroup.createLink(action:'getLicenseList', controller:'species', 'userGroup':userGroupInstance)}",
                'getAudienceFromDB' :  "${uGroup.createLink(action:'getAudienceList', controller:'species', 'userGroup':userGroupInstance)}",
                'content':{
                    'url':"${uGroup.createLink('controller':'content')}"
                },
                'observation':{
                    listUrl:"${uGroup.createLink(controller:'observation', action: 'listJSON', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}",
                    occurrencesUrl:"${uGroup.createLink(controller:'observation', action: 'occurrences', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}",
                    relatedObservationsUrl:"${uGroup.createLink(controller:'observation', action: 'related', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}",
                    uploadUrl:"${g.createLink(controller:'observation', action:'upload_resource')}",
                    distinctRecoListUrl:"${uGroup.createLink(controller:'observation', action: 'distinctReco', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress, params:[actionType:params.action])}",
                    speciesGroupCountListUrl:"${uGroup.createLink(controller:'observation', action: 'speciesGroupCount', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress, params:[actionType:params.action])}",

                },
                'recommendation': {
                    'getRecos' : "${uGroup.createLink(controller:'recommendation', action:'getRecos', userGroup:userGroupInstance)}",
                    'suggest' : "${uGroup.createLink(controller:'recommendation', action: 'suggest', userGroup:userGroupInstance)}"
                },
                'action': {
                    'inGroupsUrl':"${uGroup.createLink(controller:'action', action: 'inGroups', userGroup:userGroupInstance)}"
                },
                'map': {
                    'domain':document.domain,
                    'geoserverHost':document.domain,
                    'serverURL':"${grailsApplication.config.speciesPortal.maps.serverURL}"
                },
                'ck':{
                    
                },
                'taxon': {
                    'classification': {
                        'listUrl':"${uGroup.createLink(controller:'taxon', action:'listHierarchy', userGroupWebaddress:params.webaddress)}",
                        'createUrl':"${uGroup.createLink(controller:'taxon', action:'create', userGroupWebaddress:params.webaddress)}",
                        'updateUrl':"${uGroup.createLink(controller:'taxon', action:'update', userGroupWebaddress:params.webaddress)}",
                        'deleteUrl':"${uGroup.createLink(controller:'taxon', action:'delete', userGroupWebaddress:params.webaddress)}"
                    }
                }
                
	}
	$("#userGroupSelectFilter").val("${(queryParams && queryParams.uGroup)?queryParams.uGroup:(params.webaddress?'THIS_GROUP':'ALL')}");
});
        
</script>
