<%@page import="species.Resource.ResourceType"%>
<%@page import="species.Resource"%>
<%@page import="species.utils.ImageType"%>
<%@page	import="org.springframework.web.context.request.RequestContextHolder"%>
<%@page import="species.License"%>
<%@page import="species.License.LicenseType"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<%@ page import="org.grails.taggable.Tag"%>
<%@ page import="species.utils.Utils"%>
<%@page import="species.Resource.ResourceType"%>
<%@page import="java.util.Arrays"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

<meta name="layout" content="main" />
<r:require modules="observations_create"/>

<g:set var="entityName"
	value="${message(code: 'observation.label', default: 'Observation')}" />
<title><g:message code="default.create.label"
		args="[entityName]" />
</title>

<style>
.btn-group.open .dropdown-menu {
	top: 43px;
}

.group_option a {
	text-align: left;
	line-height:33px;
}

.btn-large .caret {
	margin-top: 15px;
	position: absolute;
	right: 10px;
}

.btn-group .btn-large.dropdown-toggle {
	width: 300px;
	height:44px;
	text-align: left;
	line-height:33px;
	padding:5px;
}

.textbox input{
	text-align: left;
	/*width: 290px;*/
	height:34px;
	padding:5px;
}

.form-horizontal .control-label {
	padding-top: 15px;
}

.block {
	border-radius: 5px;
	background-color: #a6dfc8; 
	margin: 0px;
}

.block label {
	float: left; 
	text-align: left; 
	padding: 10px; 
	width: auto;
}

.block small{
    color: #444444;
}

#help-identify {
	height: 0;
    left: 139px;
    padding: 0;
    position: relative;
    top: -25px;
}

.left-indent {
	margin-left:100px;
}
.control-group.error  .help-inline {
	padding-top : 15px
}

.cke_skin_kama .cke_editor {
display: table !important;
}

input.dms_field {
	width: 19%;
	display: none;       
}
.btn .combobox-clear {
    margin-top: 12px;
}
#ui-datepicker-div {
	width: 16%;
}

.observation_create .super-section {
	margin-left:0px;
}
.sidebar-section {
	width: 450px;
	margin: 0px 0px 20px -10px;
	float: right;
}
</style>
</head>
<body>
		<div class="observation_create">
			<div class="span12">
				<obv:showSubmenuTemplate model="['entityName':(params.action == 'edit' || params.action == 'update')?'Edit Observation':'Add Observation']"/>
			

			<%
				def form_id = "addObservation"
				def form_action = uGroup.createLink(action:'save', controller:'observation', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
				def form_button_name = "Add Observation"
				def form_button_val = "Add Observation"
				if(params.action == 'edit' || params.action == 'update'){
					//form_id = "updateObservation"
					form_action = uGroup.createLink(action:'update', controller:'observation', id:observationInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
				 	form_button_name = "Update Observation"
					form_button_val = "Update Observation"
				}
			
			%>

			<form id="${form_id}" action="${form_action}" method="POST"
				class="form-horizontal">

				<div class="span12 super-section">
					<div class="section">
						<h3>What did you observe?</h3>


						<div>
							<i class="icon-picture"></i><span>Upload photos of a
								single observation and species</span>
					
					
							<div
								class="resources control-group ${hasErrors(bean: observationInstance, field: 'resource', 'error')}">
								<ul id="imagesList" class="thumbwrap thumbnails"
									style='list-style: none; margin-left: 0px;'>
									<g:set var="i" value="${1}" />
									<g:each in="${observationInstance?.resource}" var="r">
										<li class="addedResource thumbnail">
<%
def thumbnail = r.thumbnailUrl()?:null;
def imagePath = '';
if(r && thumbnail) {
	if(r.type == ResourceType.IMAGE) {
		imagePath = g.createLinkTo(base:grailsApplication.config.speciesPortal.observations.serverURL,	file: thumbnail)
	} else if(r.type == ResourceType.VIDEO){
		imagePath = g.createLinkTo(base:thumbnail,	file: '')
	}
}
%>
											
											<div class='figure' style="height: 200px; overflow: hidden;">
												<span> <img style="width: auto; height: auto;"
													src='${imagePath}'
													class='geotagged_image' exif='true' /> </span>
											</div>
											

											<div class='metadata prop'
												style="position: relative; top: -30px;">
												<input name="file_${i}" type="hidden" value='${r.fileName}' />
												<input name="url_${i}" type="hidden" value='${r.url}' />
												<input name="type_${i}" type="hidden" value='${r.type}'/>
                                                                                                <obv:rating model="['resource':r, class:'obvcreate', 'hideForm':true, index:i]"/>
												<g:if test="${r.type == ResourceType.IMAGE}">
												<div id="license_div_${i}" class="licence_div pull-left dropdown">

													<a id="selected_license_${i}"
														class="btn dropdown-toggle btn-mini"
														data-toggle="dropdown">
														
															<img
																src="${resource(dir:'images/license',file:r?.licenses?.asList().first()?.name?.getIconFilename()+'.png', absolute:true)}"
																title="Set a license for this image" />
														
														 <b class="caret"></b>
													</a>
													
														<ul id="license_options_${i}" class="dropdown-menu license_options">
															<span>Choose a license</span>
															<g:each in="${species.License.list()}" var="l">
																<li class="license_option"
																	onclick="$('#license_${i}').val($.trim($(this).text()));$('#selected_license_${i}').find('img:first').replaceWith($(this).html());">
																	<img
																	src="${resource(dir:'images/license',file:l?.name?.getIconFilename()+'.png', absolute:true)}" /><span style="display:none;">${l?.name?.value}</span> 
																</li>
															</g:each>
														</ul>
													<input id="license_${i}" type="hidden" name="license_${i}" value="${r?.licenses?.asList().first()?.name}"></input>
												</div>
												</g:if>
                                                                                                
											</div> 
											<div class="close_button"
												onclick="removeResource(event, ${i});$('#geotagged_images').trigger('update_map');"></div>

										</li>
										<g:set var="i" value="${i+1}" />
									</g:each>
									<li id="add_file" class="addedResource" style="z-index:10">
										<div id="add_file_container">
											<div id="add_image"></div> 
											<div style="text-align:center;">
												or
											</div> 
											<div id="add_video" class="editable"></div>
										</div>
										<div class="progress">
											<div id="translucent_box"></div>
											<div id="progress_bar"></div>
											<div id="progress_msg"></div>
										</div>
										
									</li>
								</ul>
								<div id="image-resources-msg" class="help-inline">
									<g:renderErrors bean="${observationInstance}" as="list"
										field="resource" />
								</div>
							</div>
						</div>
						<div class="span6" style="margin:0px";>
						<div class="row control-group ${hasErrors(bean: observationInstance, field: 'group', 'error')}">

							<label for="group" class="control-label"><g:message
									code="observation.group.label" default="Group" /> </label>

							<div class="controls">
								<div id="groups_div" class="btn-group" style="z-index: 3;">
									<%
                                        def defaultGroup = observationInstance?.group
                                        //def defaultGroupIconFileName = (defaultGroupId)? SpeciesGroup.read(defaultGroupId).icon(ImageType.VERY_SMALL)?.fileName?.trim() : SpeciesGroup.findByName('All').icon(ImageType.VERY_SMALL)?.fileName?.trim()
                                        def defaultGroupValue = (defaultGroup) ? defaultGroup.name : "Select group"
										def defaultIcon = (defaultGroup) ? defaultGroup.iconClass() : "all_gall_th"
                                        %>

									<button id="selected_group"
										class="btn btn-large dropdown-toggle" data-toggle="dropdown"
										data-target="#groups_div">
										<i class="display_value group_icon pull-left species_groups_sprites active ${defaultIcon}"></i> ${defaultGroupValue}
										<span class="caret"></span>
									</button>

									<ul id="group_options" class="dropdown-menu">

										<g:each in="${species.groups.SpeciesGroup.list()}" var="g">
											<g:if
												test="${!g.name.equals(grailsApplication.config.speciesPortal.group.ALL)}">
												<li class="group_option" value="${g.id}" title="${g.name}">
													<a>
														<i class="group_icon pull-left species_groups_sprites active ${g.iconClass()}"></i>
															${g.name}
												</a></li>
											</g:if>
										</g:each>
									</ul>


									<div class="help-inline">
										<g:hasErrors bean="${observationInstance}" field="group">
											<g:message code="observation.group.not_selected" />
										</g:hasErrors>
									</div>

								</div>
								<input id="group_id" type="hidden" name="group_id"
									value="${observationInstance?.group?.id}"></input>
							</div>
						</div>

						<div
							class="row control-group ${hasErrors(bean: observationInstance, field: 'habitat', 'error')}">

							<label class="control-label" for="habitat"><g:message
									code="observation.habitat.label" default="Habitat" /> </label>

							<div class="controls">
									<div id="habitat_div" class="btn-group" style="z-index: 2;">
										<%
                                            def defaultHabitat = observationInstance?.habitat;
                                            //def defaultHabitatIconFileName = (defaultHabitatId)? defaultHabitat.icon(ImageType.VERY_SMALL)?.fileName?.trim() : Habitat.findByName('All').icon(ImageType.VERY_SMALL)?.fileName?.trim()
                                            def defaultHabitatValue = (defaultHabitat) ? defaultHabitat.name : "Select habitat"
											def defaultHabitatIcon = (defaultHabitat) ? defaultHabitat.iconClass() : "all_gall_th"
                                        %>
                                        <button id="selected_habitat"
										class="btn btn-large dropdown-toggle" data-toggle="dropdown"
										data-target="#habitat_div">
											<i class="display_value group_icon pull-left habitats_sprites active ${defaultHabitatIcon}"></i> ${defaultHabitatValue}
											<span class="caret"></span>
									</button>
										
										<ul id="habitat_options" class="dropdown-menu">

											<g:each in="${species.Habitat.list()}" var="h">
											<g:if
												test="${!h.name.equals(grailsApplication.config.speciesPortal.group.ALL)}">
												<li class="habitat_option" value="${h.id}" title="${h.name}"><a>
												
												<i class="group_icon pull-left habitats_sprites active ${h.iconClass()}"></i>
												${h.name}</a>
												</li>
											</g:if>
											</g:each>
										</ul>


										<div class="help-inline">
											<g:hasErrors bean="${observationInstance}" field="habitat">
												<g:message code="observation.habitat.not_selected" />
											</g:hasErrors>
										</div>
									</div>
								</div>
								<input id="habitat_id" type="hidden" name="habitat_id"
									value="${observationInstance?.habitat?.id}"></input>
						</div>
						
						<div
							class="row control-group ${hasErrors(bean: observationInstance, field: 'observedOn', 'error')}">

							<label for="observedOn" class="control-label"><i
								class="icon-calendar"></i>
							<g:message code="observation.observedOn.label"
									default="Observed on" /></label>

							<div class="controls textbox">
								<input name="observedOn" type="text" id="observedOn" class="input-block-level"
									value="${observationInstance?.observedOn?.format('dd/MM/yyyy')}"
									placeholder="Select date of observation (dd/MM/yyyy)" />
								
								<div class="help-inline">
									<g:hasErrors bean="${observationInstance}" field="observedOn">
									<g:if test="${observationInstance.observedOn == null}">
										<g:message code="observation.observedOn.validator.invalid_date" />
									</g:if>
									<g:else>
										<g:message code="observation.observedOn.validator.future_date" />
									</g:else>
										
									</g:hasErrors>
								</div>

							</div>
						</div>
						</div>
						<div class="span6 sidebar-section" style="margin-top:-5px;">
						<g:if
							test="${observationInstance?.fetchSpeciesCall() == 'Unknown'}">
							<div id="help-identify" class="control-label">
								<label class="checkbox" style="text-align: left;"> <input
									type="checkbox" name="help_identify" /> Help identify </label>
							</div>
						</g:if>
							<reco:create />
						</div>

						</div>


					</div>
				
					<%
						def obvInfoFeeder = lastCreatedObv ? lastCreatedObv : observationInstance
           			%>
           			<div class="span12 super-section" style="clear: both;">
						<obv:showMapInput model="[observationInstance:observationInstance, obvInfoFeeder:obvInfoFeeder, locationHeading:'Where did you find this observation?']"></obv:showMapInput>
      				</div>
					<div class="span12 super-section"  style="clear: both">
						<div class="section" style="position: relative; overflow: visible;">
							<h3>Describe your observation!</h3>
							<div class="span6 block">
								<!--label for="notes"><g:message code="observation.notes.label" default="Notes" /></label-->
								<h5><label><i
									class="icon-pencil"></i>Notes <small><g:message code="observation.notes.message" default="Description" /></small></label><br />
								</h5><div class="section-item" style="margin-right: 10px;">
									<!-- g:textArea name="notes" rows="10" value=""
										class="text ui-corner-all" /-->

									<ckeditor:config var="toolbar_editorToolbar">
									[
    									[ 'Bold', 'Italic' ]
									]
									</ckeditor:config>
									<ckeditor:editor name="notes" height="53px" toolbar="editorToolbar">
										${observationInstance?.notes}
									</ckeditor:editor>
								</div>
							</div>
							<%
								def obvTags = observationInstance.tags
								if(params.action == 'save' && saveParams.tags){
									obvTags = Arrays.asList(saveParams.tags)
								}				
							%>
								
							<div class="span6 block sidebar-section" style="margin:0px 0px 20px -10px;">
								<h5><label><i
									class="icon-tags"></i>Tags <small><g:message code="observation.tags.message" default="" /></small></label>
								</h5>
								<div class="create_tags section-item" style="clear: both;">
									<ul id="tags">
										<g:each in="${obvTags}" var="tag">
											<li>${tag}</li>
										</g:each>
									</ul>
								</div>
							</div>
							
							
								
							<sUser:isFBUser>
								<div class="span6 sidebar-section block" style="margin-left:-10px;">
									<div class="create_tags" >
										<label class="checkbox" > <g:checkBox style="margin-left:0px;"
												name="postToFB" />
											Post to Facebook</label>
									</div>
								</div>
							</sUser:isFBUser>
							
							
							
					</div>
					</div>
				<uGroup:isUserGroupMember>
					<div class="span12 super-section"  style="clear: both">
						<div class="section" style="position: relative; overflow: visible;">
							<h3>Post to User Groups</h3>
							<div>
								<%
									def obvActionMarkerClass = (params.action == 'create' || params.action == 'save')? 'create' : '' 
								%>
								<div id="userGroups" class="${obvActionMarkerClass}" name="userGroups" style="list-style:none;clear:both;">
									<uGroup:getCurrentUserUserGroups model="['observationInstance':observationInstance]"/>
								</div>
							</div>
						</div>
					</div>
				</uGroup:isUserGroupMember>
				
				<div class="span12" style="margin-top: 20px; margin-bottom: 40px;">
					
					<g:if test="${observationInstance?.id}">
						<a href="${uGroup.createLink(controller:params.controller, action:'show', id:observationInstance.id)}" class="btn"
							style="float: right; margin-right: 30px;"> Cancel </a>
					</g:if>
					<g:else>
					<a href="${uGroup.createLink(controller:params.controller, action:'list')}" class="btn"
							style="float: right; margin-right: 30px;"> Cancel </a>
					</g:else>
					
					<g:if test="${observationInstance?.id}">
						<div class="btn btn-danger"
							style="float: right; margin-right: 5px;">
							<a
								href="${uGroup.createLink(controller:'observation', action:'flagDeleted', id:observationInstance.id)}"
								onclick="return confirm('${message(code: 'default.observatoin.delete.confirm.message', default: 'This observation will be deleted. Are you sure ?')}');">Delete
								Observation </a>
						</div>
					</g:if>
					<a id="addObservationSubmit" class="btn btn-primary"
						style="float: right; margin-right: 5px;"> ${form_button_val} </a>
				
						<div class="row control-group">
								<label class="checkbox" style="text-align: left;"> 
								 <g:checkBox style="margin-left:0px;"
									name="agreeTerms" value="${observationInstance.agreeTerms}"/>
								 <span class="policy-text"> By submitting this form, you agree that the photos or videos you are submitting are taken by you, or you have permission of the copyright holder to upload them on creative commons licenses. </span></label>
						</div>
					
				</div>
				
				

            </form>
           <%
				def obvTmpFileName = observationInstance?.resource?.iterator()?.next()?.fileName
				def obvDir = obvTmpFileName ?  obvTmpFileName.substring(0, obvTmpFileName.lastIndexOf("/")) : ""
	       %>


            	<form id="upload_resource" 
					title="Add a photo for this observation"
	                                method="post"
					class="${hasErrors(bean: observationInstance, field: 'resource', 'errors')}">
	
					<span class="msg" style="float: right"></span>
					<input id="videoUrl" type="hidden" name='videoUrl'value="" />
					<input type="hidden" name='obvDir' value="${obvDir}" />
				</form>
					
                </div>
            </div>
       </div>
		<!--====== Template ======-->
<script id="metadataTmpl" type="text/x-jquery-tmpl">
	<li class="addedResource thumbnail">
	    <div class='figure' style='height: 200px; overflow:hidden;'>
                <span> 
                        <img id='image_{{>i}}' style="width:auto; height: auto;" src='{{>thumbnail}}' class='geotagged_image' exif='true'/> 
                </span>
	    </div>
				
	    <div class='metadata prop' style="position:relative; top:-30px;">
	            <input name="file_{{>i}}" type="hidden" value='{{>file}}'/>
	            <input name="url_{{>i}}" type="hidden" value='{{>url}}'/>
				<input name="type_{{>i}}" type="hidden" value='{{>type}}'/>
                                 <%def r = new Resource();%>
                                        <obv:rating model="['resource':r, class:'obvcreate', 'hideForm':true, index:1]"/>

				{{if type == '${ResourceType.IMAGE}'}}
                <div id="license_div_{{>i}}" class="licence_div pull-left dropdown">
                    <a id="selected_license_{{>i}}" class="btn dropdown-toggle btn-mini" data-toggle="dropdown">
                        <img src="${resource(dir:'images/license',file:'cc_by.png', absolute:true)}" title="Set a license for this image"/>
                        <b class="caret"></b>
                    </a>
                    <ul id="license_options_{{>i}}" class="dropdown-menu license_options">
                         <span>Choose a license</span>
                         <g:each in="${species.License.list()}" var="l">
                             <li class="license_option" onclick="$('#license_{{>i}}').val($.trim($(this).text()));$('#selected_license_{{>i}}').find('img:first').replaceWith($(this).html());">
                                 <img src="${resource(dir:'images/license',file:l?.name.getIconFilename()+'.png', absolute:true)}"/><span style="display:none;">${l?.name?.value}</span>
                             </li>
                         </g:each>
                    </ul>
					<input id="license_{{>i}}" type="hidden" name="license_{{>i}}" value="CC BY"></input>
                  		</div>	
				{{/if}}
		</div>
       	<div class="close_button" onclick="removeResource(event, {{>i}});$('#geotagged_images').trigger('update_map');"></div>
	</li>
	
</script>
	
	<script type="text/javascript" src="//api.filepicker.io/v1/filepicker.js"></script>
	
	<r:script>
	
    var add_file_button = '<li id="add_file" class="addedResource" style="display:none;z-index:10;"><div id="add_file_container"><div id="add_image"></div><div id="add_video" class="editable"></div></div><div class="progress"><div id="translucent_box"></div><div id="progress_bar"></div ><div id="progress_msg"></div ></div></li>';

	$(document).ready(function(){
		$('.dropdown-toggle').dropdown();

		var filePick = function() {
			filepicker.pickMultiple({
			    mimetypes: ['image/*'],
			    maxSize: 104857600,
			    //debug:true,
			    services:['COMPUTER', 'FACEBOOK', 'FLICKR', 'PICASA', 'GOOGLE_DRIVE', 'DROPBOX'],
			  },
			  function(FPFiles){
			    $.each(FPFiles, function(){
				    $('<input>').attr({
				    type: 'hidden',
				    name: 'resources',
				    value:JSON.stringify(this)
					}).appendTo('#upload_resource');
				})
				$('#upload_resource').submit().find("span.msg").html("Uploading... Please wait...");
			  	$("#iemsg").html("Uploading... Please wait...");
			  	$(".progress").css('z-index',110);
			  	$('#progress_msg').html('Uploading ...');
			  },
			  function(FPError){
			    console.log(FPError.toString());
			  }
			);		
		}
		
		$('#add_image').bind('click', filePick);
		
		var addVideoOptions = {
		    type: 'text',
		    mode:'popup',
		    emptytext:'',
                    placement:'bottom',
		    url: function(params) {
   				var d = new $.Deferred;
   				if(!params.value) {
       				return d.reject('This field is required'); //returning error via deferred object
   				} else {
   					$('#videoUrl').val(params.value);
       				        $('#upload_resource').submit().find("span.msg").html("Uploading... Please wait...");
		  			$("#iemsg").html("Uploading... Please wait...");
		  			$(".progress").css('z-index',110);
		  			$('#progress_msg').html('Uploading ...');
		  			d.resolve();
       			}
       			return d.promise()  
       		},
       		validate : function(value) {
 					if($.trim(value) == '') {
       				return 'This field is required';
   				}
			},
		    title: 'Enter YouTube watch url like http://www.youtube.com/watch?v=v8HVWDrGr6o'
		}
                
/*                   // Google Picker API for the Google Docs import
                   function newPicker() {
                        google.load('picker', '1', {"callback" : createPicker});
                    }
                    // Create and render a Picker object for searching images.
                    function createPicker() {
                        var picker = new google.picker.PickerBuilder().
                        addView(google.picker.ViewId.YOUTUBE).
                        setCallback(pickerCallback).
                        build();
                        picker.setVisible(true);
                        //$(".picker-dialog-content").prepend("<div id='anyVideoUrl' class='editable'></div>");
                        //$('#anyVideoUrl').editable(addVideoOptions);
                    }

                    // A simple callback implementation.
                    function pickerCallback(data) {
                            var url = 'nothing';
                            if (data[google.picker.Response.ACTION] == google.picker.Action.PICKED) {
                                var doc = data[google.picker.Response.DOCUMENTS][0];
                                url = doc[google.picker.Document.URL];
                                if(url) {
                                    $('#videoUrl').val(url);
                                    $('#upload_resource').submit().find("span.msg").html("Uploading... Please wait...");
                                    $("#iemsg").html("Uploading... Please wait...");
                                    $(".progress").css('z-index',110);
                                    $('#progress_msg').html('Uploading ...');
                                }
                            }
                    }
*/
		$('#add_video').editable(addVideoOptions);
                //$('#add_video').click(function(){
                //    newPicker();                    
                //});
		
		$('#attachFiles').change(function(e){
  			$('#upload_resource').submit().find("span.msg").html("Uploading... Please wait...");
  			$("#iemsg").html("Uploading... Please wait...");
		});
       	
        function progressHandlingFunction(e){
            if(e.lengthComputable){
                var position = e.position || e.loaded;
                var total = e.totalSize || e.total;

                var percentVal = ((position/total)*100).toFixed(0) + '%';
                $('#progress_bar').width(percentVal)
                $('#translucent_box').width('100%')
                $(".progress").css('z-index',110);
                $('#progress_msg').html('Uploaded '+percentVal);
             }
        }

     	$('#upload_resource').ajaxForm({ 
			url:'${g.createLink(controller:'observation', action:'upload_resource')}',
			dataType: 'xml',//could not parse json wih this form plugin 
			clearForm: true,
			resetForm: true,
			type: 'POST',
			 
			beforeSubmit: function(formData, jqForm, options) {
				$("#addObservationSubmit").addClass('disabled');
				return true;
			}, 
            /*xhr: function() {  // custom xhr
                myXhr = $.ajaxSettings.xhr();
                if(myXhr.upload){ // check if upload property exists
                    myXhr.upload.addEventListener('progress', progressHandlingFunction, false); // for handling the progress of the upload
                }
                return myXhr;
            },*/
			success: function(responseXML, statusText, xhr, form) {
				$("#addObservationSubmit").removeClass('disabled');
				$(form).find("span.msg").html("");
				$(".progress").css('z-index',90);
				$('#progress_msg').html('');
				$("#iemsg").html("");
				//var rootDir = '${grailsApplication.config.speciesPortal.observations.serverURL}'
				var rootDir = '${Utils.getDomainServerUrlWithContext(request)}' + '/observations'
				var obvDir = $(responseXML).find('dir').text();
				var obvDirInput = $('#upload_resource input[name="obvDir"]');
				if(!obvDirInput.val()){
					$(obvDirInput).val(obvDir);
				}
				var images = []
				var metadata = $(".metadata");
				var i = 0;
				if(metadata.length > 0) {
					var file_id = $(metadata.get(-1)).children("input").first().attr("name");
					i = parseInt(file_id.substring(file_id.indexOf("_")+1));
				}
				$(responseXML).find('resources').find('res').each(function() {
					var fileName = $(this).attr('fileName');
					var type = $(this).attr('type');					
  					images.push({i:++i, file:obvDir + "/" + fileName, url:$(this).attr('url'), thumbnail:$(this).attr('thumbnail'), type:type, title:fileName});
				});
				
				var html = $( "#metadataTmpl" ).render( images );
				var metadataEle = $(html)
				metadataEle.each(function() {
					$('.geotagged_image', this).load(function(){
						update_geotagged_images_list($(this));		
					});
                                        var $ratingContainer = $(this).find('.star_obvcreate');
                                        rate($ratingContainer)
				})
				$( "#imagesList li:last" ).before (metadataEle);

/*                if (navigator.appName.indexOf('Microsoft') == -1) {
                    $( "#imagesList" ).append (add_file_button);
                }*/
                $( "#add_file" ).fadeIn(3000);
                $("#image-resources-msg").parent(".resources").removeClass("error");
                $("#image-resources-msg").html("");
				$("#upload_resource input[name='resources']").remove();
				$('#videoUrl').val('');
				$('#add_video').editable('setValue','', false);		
			}, error:function (xhr, ajaxOptions, thrownError){
					$("#addObservationSubmit").removeClass('disabled');
					$("#upload_resource input[name='resources']").remove();
					$('#videoUrl').val('');
					$(".progress").css('z-index',90);
					$('#add_video').editable('setValue','', false);
					//xhr.upload.removeEventListener( 'progress', progressHandlingFunction, false); 
					
					//successHandler is used when ajax login succedes
	            	var successHandler = this.success, errorHandler;
	            	handleError(xhr, ajaxOptions, thrownError, successHandler, function() {
						var response = $.parseJSON(xhr.responseText);
						if(response.error){
							$("#image-resources-msg").parent(".resources").addClass("error");
							$("#image-resources-msg").html(response.error);
						}
						
						var messageNode = $(".message .resources");
						if(messageNode.length == 0 ) {
							$("#upload_resource").prepend('<div class="message">'+(response?response.error:"Error")+'</div>');
						} else {
							messageNode.append(response?response.error:"Error");
						}
						
						
					});
           } 

           

     	});  

        $(".group_option").click(function(){
                $("#group_id").val($(this).val());
                var caret = "<span class='caret'></span>";
                $("#selected_group").html($(this).html() + caret);
                //$("#group_options").hide();
                $("#selected_group").css({'background-color':'#e5e5e5', 'border-bottom-color':'#aeaeae'});
                
        });
        
        $.each($('.star_obvcreate'), function(index, value){
            rate($(value));
        });

       
        $(".habitat_option").click(function(){
                $("#habitat_id").val($(this).val());
                var caret = "<span class='caret'></span>";
                $("#selected_habitat").html($(this).html() + caret);
                //$("#habitat_options").hide();
                $("#selected_habitat").css({'background-color':'#e5e5e5', 'border-bottom-color':'#aeaeae'});
        });
       
        $("#name").watermark("Suggest a species name");
        $("#place_name").watermark("Set a title for this location");
       
        $("#help-identify input").click(function(){
                if ($(this).is(':checked')){
                    $('.nameContainer input').val('');
                    $('.nameContainer input').attr('disabled', 'disabled');
                }else{
                    $('.nameContainer input').removeAttr('disabled');
                }
        });
        
 		//$(".tagit-input").watermark("Add some tags");
                $("#tags").tagit({
                        select:true, 
                        allowSpaces:true, 
                        placeholderText:'Add some tags',
                        fieldName: 'tags', 
                        autocomplete:{
                                source: '/observation/tags'
                        }, 
                        triggerKeys:['enter', 'comma', 'tab'], 
                        maxLength:30
                });
                
                $(".tagit-hiddenSelect").css('display','none');

 		 $("#addObservationSubmit").click(function(event){
 		 	if($(this).hasClass('disabled')) {
 		 		alert("Uploading is in progress. Please submit after it is over.");
 		 		event.preventDefault();
      			return false; 		 		
 		 	}
 		 	if (document.getElementById('agreeTerms').checked){
	 		 	$(this).addClass("disabled");
	        	/*var tags = $("ul[name='tags']").tagit("tags");
	        	$.each(tags, function(index){
	        		var input = $("<input>").attr("type", "hidden").attr("name", "tags."+index).val(this);
					$('#addObservation').append($(input));	
	        	})
	        	*/
				$("#userGroupsList").val(getSelectedUserGroups());	       	
	        	$("#addObservation").submit();        	
	        	return false;
			} else {
				alert("Please agree to the terms mentioned at the end of the form to submit the observation.")
			}
		});
		
		function getSelectedUserGroups() {
		    var userGroups = []; 
		    $('.userGroups button[class~="btn-success"]').each (function() {
	            userGroups.push($(this).attr('value'));
		    });
		    return userGroups;	
		}
		
		$('input:radio[name=groupsWithSharingNotAllowed]').click(function() {
		    var previousValue = $(this).attr('previousValue');
    
    		if(previousValue == 'true'){
        		$(this).attr('checked', false)
    		}
    
    		$(this).attr('previousValue', $(this).attr('checked'));
		});
		

      
		$('#use_dms').click(function(){
                if ($('#use_dms').is(':checked')) {
                    $('.dms_field').fadeIn();
                    $('.degree_field').hide();
                } else {
                    $('.dms_field').hide();
                    $('.degree_field').fadeIn();
                }

        });
        
        filepicker.setKey('AXCVl73JWSwe7mTPb2kXdz');
	});


	function removeResource(event, imageId) {
		var targ;
		if (!event) var event = window.event;
		if (event.target) targ = event.target;
		else if (event.srcElement) targ = event.srcElement; //for IE
		$(targ).parent('.addedResource').remove();
		$("#image_"+imageId).remove();
	}
	
	$( "#observedOn" ).datepicker({ 
			changeMonth: true,
			changeYear: true,
			format: 'dd/mm/yyyy' 
	});
	
</r:script>

</body>
</html>
