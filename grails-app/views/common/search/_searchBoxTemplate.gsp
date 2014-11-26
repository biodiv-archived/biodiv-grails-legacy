<%@page import="species.utils.Utils"%>

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
                },
                'userGroup': {
                    'joinUsUrl' : "${uGroup.createLink(controller:'userGroup', action:'joinUs') }",
		            'leaveUrl' : "${uGroup.createLink(controller:'userGroup', action:'leaveUs') }",
                    'requestMembershipUrl' : "${uGroup.createLink(controller:'userGroup', action:'requestMembership') }"
                }
                
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
                "er":"${g.message(code:'fix.errors')}"
                
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
                 "submit":"${g.message(code:'errors.submission')}"
                 
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

</script>
