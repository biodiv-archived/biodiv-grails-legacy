<%@page import="species.utils.Utils"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>


<div id='searchToggleBox' class="input-append" style="z-index:1">
	<form method="get"
		action="${uGroup.createLink(controller:'search', action:'select', userGroup:userGroupInstance) }"
		id="searchbox" class="navbar-search" style="float: none;">
		
		<input type="text" name="query" id="searchTextField" style="width:400px;"
			value="${((queryParams?.query)?:((queryParams?.q)?:params.query))?.encodeAsHTML()}"
			class="search-query span3" placeholder="${g.message(code:'default.search')}" />
		<button id="search" class="btn btn-link" type="button"><i class="icon-search icon-gray"></i></button>
	</form>

<div id="nameSuggestionsMain" class="dropdown span3" style="left:-20px;">
			<a class="dropdown-toggle" role="button" data-toggle="dropdown"
			data-target="#" href="#"></a>
		</div>	
</div>
<input type="hidden" id="userLanguage" value="${userLanguage?.id}" />
<script type="text/javascript">
$(document).ready(function() {
        window.params = {  
        'requestExportUrl' : "${uGroup.createLink(controller:'observation', action:'requestExport', userGroupWebaddress:params.webaddress)}",
        'controller': "${params.controller}",
        'actionForBulkCreate': "${params.action}",
        'offset':"${params.offset}",
        'isGalleryUpdate':'true',
        'isMediaFilter' : '${params.isMediaFilter}',
        'resDeleteUrl' : "${uGroup.createLink(controller:'resource', action: 'deleteUsersResourceById')}",
        'getSpeciesFieldMedia' : "${createLink(controller:'species',  action:'getSpeciesFieldMedia')}",
        "queryParamsMax":"${queryParams?.max}",
        'getProcessedImageUrl' : "${createLink(controller:'observation',  action:'getProcessedImageStatus')}",
        'curation':{
        'getNamesFromTaxonUrl' : "${uGroup.createLink('controller':'namelist', action:'getNamesFromTaxon')}",
        'getNameDetailsUrl' : "${uGroup.createLink('controller':'namelist', action:'getNameDetails')}",
        'searchExternalDbUrl' : "${uGroup.createLink('controller':'namelist', action:'searchExternalDb')}",
        'getExternalDbDetailsUrl' : "${uGroup.createLink('controller':'namelist', action:'getExternalDbDetails')}",
        'searchIBPURL' : "${uGroup.createLink('controller':'namelist', action:'searchIBP')}",
        'getOrphanRecoNamesURL' : "${uGroup.createLink('controller':'namelist', action:'getOrphanRecoNames')}",
        'curateNameURL' : "${uGroup.createLink(controller:'namelist', action:'curateName')}",
        'saveAcceptedNameURL' : "${uGroup.createLink(controller:'namelist', action:'saveAcceptedName')}",
        'editSpeciesPageURL' : "${uGroup.createLink(controller:'species', action:'editSpeciesPage')}"

        },
        'speciesName':"${params.speciesName }",
        'isFlagged':"${params.isFlagged?.toBoolean()?.toString()}",
        'nameTermsUrl': "${uGroup.createLink(controller:'search', action: 'nameTerms')}",
        'noImageUrl' : "${createLinkTo(file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}",
        'spinnerURL' : "${assetPath(src:'/all/spinner.gif', absolute:true)}",
        'dropDownIconUrl' : "${assetPath(src:'/all/dropdown_active.gif', absolute:true)}",
        'IBPDomainUrl':"${Utils.getIBPServerDomain()}",
        'searchController' : "${controller}",
        'carousel':{maxHeight:150, maxWidth:150},
        'imagesPath': "${assetPath(src:'/all/images', absolute:true)}",
        'locationsUrl': "${uGroup.createLink(controller:'observation', action: 'locations')}",
        'defaultMarkerIcon':"${assetPath(src:'/all/images', absolute:true)}",
        'isChecklistOnly':"${params.isChecklistOnly?.toBoolean()?.toString()}",
        'obvListPage' : "${uGroup.createLink(controller:'observation', action:'list','userGroup':userGroupInstance, absolute:true)}",
        'obvShowPage' : "${uGroup.createLink(controller:'observation', action:'show','userGroup':userGroupInstance, absolute:true)}",
        'species':{
            'url':"${uGroup.createLink('controller':'species', action:'show', 'userGroup':userGroupInstance)}",
            'listUrl':"${uGroup.createLink('controller':'species', action:'list', 'userGroup':userGroupInstance)}",
            'saveUrl':"${uGroup.createLink(controller:'species', action:'save')}",
            'updateUrl':"${uGroup.createLink(controller:'species', action:'update')}",
            'deleteUrl':"${uGroup.createLink(controller:'species', action:'delete')}",
            'hasPermissionToCreateSpeciesPageUrl':"${uGroup.createLink(controller:'species', action:'hasPermissionToCreateSpeciesPage')}"
        },
        'loginUrl':"${createLink(controller:'login','userGroup':userGroupInstance)}",
        'isLoggedInUrl' : "${uGroup.createLink(controller:'user', action:'isLoggedIn')}",
        'login' : {
googleOAuthSuccessUrl : "/oauth/google/success",

        },
        'userTermsUrl' : "${uGroup.createLink(controller:'user', action: 'terms')}",
        'requestPermissionFormUrl' : "${uGroup.createLink(controller:'species', action: 'requestPermission','userGroup':userGroupInstance)}",
        'inviteFormUrl' : "${uGroup.createLink(controller:'species', action: 'invite','userGroup':userGroupInstance)}",
        'inviteAddFormUrl' : "${uGroup.createLink(controller:'namelist', action: 'addPermission','userGroup':userGroupInstance)}",
        'saveModifiedSpecies' : "${uGroup.createLink(controller:'species', action:'saveModifiedSpeciesFile','userGroup':userGroupInstance) }",
        'generateNamesReportURL' : "${uGroup.createLink(controller:'species', action:'generateNamesReport','userGroup':userGroupInstance) }",
        'uploadSpecies' : "${uGroup.createLink(action:'upload', controller:'species', 'userGroup':userGroupInstance)}",
        'uploadNamesURL' : "${uGroup.createLink(action:'uploadNames', controller:'species', 'userGroup':userGroupInstance)}",
        'downloadFile': "${uGroup.createLink(action:'downloadSpeciesFile', controller:'UFile', 'userGroup':userGroupInstance)}",
        'getDataColumnsDB':  "${uGroup.createLink(action:'getDataColumns', controller:'species', 'userGroup':userGroupInstance)}",
        'getLicenseFromDB' :  "${uGroup.createLink(action:'getLicenseList', controller:'species', 'userGroup':userGroupInstance)}",
        'getAudienceFromDB' :  "${uGroup.createLink(action:'getAudienceList', controller:'species', 'userGroup':userGroupInstance)}",
        'content':{
            'url':"${uGroup.createLink('controller':'content')}"
        },
        'observation':{
listUrl:"${uGroup.createLink(controller:'observation', action: 'list', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}",
        occurrencesUrl:"${uGroup.createLink(controller:'observation', action: 'occurrences', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}",
        relatedObservationsUrl:"${uGroup.createLink(controller:'observation', action: 'related', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}",
        uploadUrl:"${g.createLink(controller:'observation', action:'upload_resource')}",
        distinctRecoListUrl:"${uGroup.createLink(controller:'observation', action: 'distinctReco', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress, params:[actionType:params.action])}",
        distinctIdentifiedRecoListUrl:"${uGroup.createLink(controller:'observation', action: 'distinctIdentifiedReco', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress, params:[actionType:params.action,userGroup:userGroupInstance])}",
        speciesGroupCountListUrl:"${uGroup.createLink(controller:'observation', action: 'speciesGroupCount', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress, params:[actionType:params.action])}",
        'addRecommendationVoteURL' : "${uGroup.createLink(controller:'observation', action:'addRecommendationVote', 'userGroup':userGroupInstance )}",
        'serverURL':"${grailsApplication.config.speciesPortal.observations.serverURL}",
        'customFieldsUrl':"${uGroup.createLink(controller:'observation', action: 'customFields', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}",
        'commentsUrl':"${uGroup.createLink(controller:'observation', action: 'comments', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}",

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

            },
            'searchUrl':"${uGroup.createLink(controller:'taxon', action:'search', userGroupWebaddress:params.webaddress)}",
            'nodesUrl':"${uGroup.createLink(controller:'taxon', action:'nodes', userGroupWebaddress:params.webaddress)}"
        },
        'userGroup': {
            'joinUsUrl' : "${uGroup.createLink(controller:'userGroup', action:'joinUs') }",
            'leaveUrl' : "${uGroup.createLink(controller:'userGroup', action:'leaveUs') }",
            'requestMembershipUrl' : "${uGroup.createLink(controller:'userGroup', action:'requestMembership') }"
        },
        'document':{
            'listUrl':"${uGroup.createLink('controller':'document', action:'list', 'userGroup':userGroupInstance)}",
        },
        'comment':{
            'getAllNewerComments': "${uGroup.createLink(controller:'comment', action:'getAllNewerComments')}"
        },
        'dataset':{
            'deleteUrl':"${uGroup.createLink(controller:'dataset', action:'delete')}"
        },
        'trait' : {
            'matchingSpeciesListUrl':"${uGroup.createLink(controller:'trait', action: 'matchingSpecies', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress, params:[actionType:params.action])}",
            'listUrl':"${uGroup.createLink(controller:'trait', action: 'list', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"
        },
        'fact' : {
            'updateFactUrl' : "${uGroup.createLink(controller:'fact', action:'update', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"
        }
        <sUser:isAdmin>
        ,
            'isAdmin':true
        </sUser:isAdmin>
 
        }

        window.i8ln = {
        "text" : {
                "featured" : "${g.message(code:'text.featured.on')}",
                "in_group" : "${g.message(code:'text.in.group')}",
                "post"     : "${g.message(code:'text.post')}",
                "posting"  : "${g.message(code:'text.posting')}",
                "more"     : "${g.message(code:'text.more')}",
                "hide"     : "${g.message(code:'text.hide')}"        
        },
        "button" :{
                "delete" : "${g.message(code:'default.button.delete.label')}",
                "edit"   : "${g.message(code:'default.button.edit.label')}"
        },
        "species" : {
            "abstracteditabletype" : {         

                "del": "${g.message(code:'delete.content')}", 
                "re":"${g.message(code:'resubmit.form')}",
                "sub":"${g.message(code:'resubmit.login')}",
                "un":"${g.message(code:'service.later')}",
                "er":"${g.message(code:'fix.errors')}",
                "type":"${g.message(code:'placeholder.name.email')}"
                
            },
            "ajaxLogin" : {
                "ewp": "${g.message(code:'while.processing')}"    
            }, 
            "parseUtil" : {
                "csd":"${g.message(code:'loading.data')}",  
                "eol":"${g.message(code:'line.error')}",
                "ic":"${g.message(code:'item.count')}",
                "mhc":"${g.message(code:'header.count.match')}",
                "max":"${g.message(code:'maximum.of')}",
                "head":"${g.message(code:'exclute.header')}",
                "snm":"${g.message(code:'name.scientific')}",
                "cnm":"${g.message(code:'name.common')}",
                "cnu":"${g.message(code:'name.common.another')}",
                "snu":"${g.message(code:'name.scientific.another')}",
                "sno":"${g.message(code:'serial.number')}",
                "med":"${g.message(code:'default.media.label')}",
                "comment":"${g.message(code:'alert.comment.delete')}"


            },
            "specie" : {
                "oc":"${g.message(code:'species.occurrence')}",
                "obs":"${g.message(code:'species.observation')}",
                "ckl":"${g.message(code:'species.checklist')}",
                "sdel": "${g.message(code:'species.deleted.sure')}",
                "eem":"${g.message(code:'species.exit.edit')}",
                "adon":"${g.message(code:'title.value.add')}",
                "sad":"${g.message(code:'species.success.data')}",
                "bdel":"${g.message(code:'button.delete')}",
                "bedi":"${g.message(code:'button.edit')}",
                "bsav":"${g.message(code:'button.save')}",
                "bcanc":"${g.message(code:'button.cancel')}",
                "bupdate":"${g.message(code:'suser.edit.update')}",
                "bcmnt":"${g.message(code:'button.update.comment')}",
                "reload":"${g.message(code:'link.press.reload')}",
                "unf":"${g.message(code:'followtemp.unfollow')}",
                "flo":"${g.message(code:'followtemp.follow')}",                 
                "ops":"${g.message(code:'observations.species.group')}"
                
                
            } ,
            "speciesPermission" : {
                "ius":"${g.message(code:'image.edited.uploaded')}",
                  "pul":"${g.message(code:'image.pulled.reload')}"  
                              },  
           
           "util" : {
               "sem":"${g.message(code:'send.email')}",
               "mor":"${g.message(code:'link.moree')}",
               "rles":"${g.message(code:'link.read.less')}"
            }
        },
        "observation" : {
            "addResource" : {
                "md": "${g.message(code:'info.media.deleted')}",
                "fr":"${g.message(code:'info.field.required')}",
                "youtube":"${g.message(code:'link.youtube.watch')}",
                "ayoutube":"${g.message(code:'link.ayoutube.audio')}",
                "upload":"${g.message(code:'info.upload.wait')}",   

                "uploading":"${g.message(code:'info.uploading')}"

            },

            "bulkObvCreate" :{
                "up":"${g.message(code:'uploading.progress.submit')}",
                "agree":"${g.message(code:'agree.terms.submit')}",

                "error":"${g.message(code:'errror.refresh')}",   
                 "suc":"${g.message(code:'error.message.success')}",
                 "submit":"${g.message(code:'errors.submission')}",
                 "failedNumericFieldValidation":"${g.message(code:'errors.failedNumericFieldValidation')}",
                 "failedMandatoryFieldValidation":"${g.message(code:'errors.failedMandatoryFieldValidation')}"    
                 
            },

            "create" :{
                "mark":"${g.message(code:'marked.verify')}",
                "add":"${g.message(code:'add.media.info')}",

                "req":"${g.message(code:'require.this.field')}",

                "ld": "${g.message(code:'load.names')}",

                "sn":"${g.message(code:'scientific.common.name')}",
                "same":"${g.message(code:'mentioned.same.name')}",

                "in":"${g.message(code:'name.incorrect')}",

                "valid":"${g.message(code:'error.validating')}",

                "nc":"${g.message(code:'new.column')}"

            },
            "show" :{

                "lock":"${g.message(code:'species.locked')}" 
            },
            "upload" :{
                "again":"${g.message(code:'other.try.again')}",                       
                "statu":"${g.message(code:'upload.progres.visit')}",                       
                "lic":"${g.message(code:'provide.license')}"
            },
            "maps" :{

                "newlayer":"${g.message(code:'maps.new.layers')}",
                "allayer" :"${g.message(code:'maps.all.layers')}",
                "bytheme" :"${g.message(code:'maps.by.theme')}",
                "listtheme" :"${g.message(code:'maps.list.themes')}",
                "show"    :'${g.message(code:'maps.show.layers')}',
                "madd"    :"${g.message(code:'maps.add')}",
                "mremove" :"${g.message(code:'maps.remove')}",
                "mzoom"   :"${g.message(code:'maps.zoom')}",
                "mrecord" :"${g.message(code:'maps.all.records')}",
                "moccur"  :"${g.message(code:'maps.occurence.records')}",
                "msearch" :"${g.message(code:'default.search')}",
                "noselect":"${g.message(code:'maps.no.selected')}",
                "nofeatureselect":"${g.message(code:'maps.no.featured.select')}",
                "reset":"${g.message(code:'maps.reset')}",
                "det":"${g.message(code:'maps.details')}",
                "ln":"${g.message(code:'maps.link')}",
                "but_1":"${g.message(code:'maps.legend.button1')}",
                "but_2":"${g.message(code:'maps.legend.button2')}",
                "but_3":"${g.message(code:'maps.legend.button3')}",
                "but_4":"${g.message(code:'maps.legend.button4')}",
                "resAll":"${g.message(code:'maps.resetall')}"

            }      


        }


    }

	$("#userGroupSelectFilter").val("${(queryParams && queryParams.uGroup)?queryParams.uGroup:(params.webaddress?'THIS_GROUP':'ALL')}");

    $('#advSearchBox.dropdown-menu input, #advSearchBox.dropdown-menu label, #advSearchBox.dropdown-menu select').click(function(e) {
            console.log('disable advSearch dropdown-menu close action');
            e.stopPropagation();
    });
});
       
$(document).ready(function(){
    $(document).on('click','.clickcontent',function(){
        $(this).next().slideToggle('slow');
   });
});

	function setDefaultGroup(){
		var defId = "#group_" + "${SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.ALL).id}"
		$(defId).click();
	}
	function setDefaultHabitat(){
		var defId = "#habitat_" + "${Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id}"
		$(defId).click();
	}
	$(document).ready(function() {
			initRelativeTime("${uGroup.createLink(controller:'activityFeed', action:'getServerTime')}");
	});

</script>
