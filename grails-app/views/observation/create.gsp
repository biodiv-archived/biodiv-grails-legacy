<%@page import="species.utils.ImageType"%>
<%@page	import="org.springframework.web.context.request.RequestContextHolder"%>
<%@page import="species.License"%>
<%@page import="species.License.LicenseType"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<%@ page import="org.grails.taggable.Tag"%>
<%@ page import="species.utils.Utils"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

<meta name="layout" content="main" />
<script src="http://maps.google.com/maps/api/js?sensor=true"></script>
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
				<obv:showSubmenuTemplate model="['entityName':'Add an Observation']"/>
			

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
											<%def thumbnail = r.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix)%>
											<div class='figure' style="height: 200px; overflow: hidden;">
												<span> <img style="width: auto; height: auto;"
													src='${createLinkTo(file: thumbnail, base:grailsApplication.config.speciesPortal.observations.serverURL)}'
													class='geotagged_image' exif='true' /> </span>
											</div>

											<div class='metadata prop'
												style="position: relative; left: 5px; top: -30px;">
												<input name="file_${i}" type="hidden" value='${r.fileName}' />
												<div id="license_div_${i}" class="licence_div dropdown">

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
													
												</div>
											</div> <input id="license_${i}" type="hidden" name="license_${i}"></input>
											<div class="close_button"
												onclick="removeResource(event, ${i});$('#geotagged_images').trigger('update_map');"></div>

										</li>
										<g:set var="i" value="${i+1}" />
									</g:each>
									<li id="add_file" class="addedResource"
										onclick="$('#attachFiles').select()[0].click();return false;">
										<div class="progress">
											<div id="translucent_box"></div>
											<div id="progress_bar"></div>
											<div id="progress_msg"></div>
										</div></li>
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
				


				<div class="span12 super-section" style="clear: both;">
					<div class="section">
						<h3>Where did you find this observation?</h3>

						<div class="span6" style="margin:0px";>
						

							<div class="row control-group">
								<%
									def obvInfoFeeder = lastCreatedObv ? lastCreatedObv : observationInstance
	                            	def defaultPlaceName = (obvInfoFeeder) ? obvInfoFeeder.placeName : ""
	                            %>
	                            <label for="place_name" class="control-label"> <i class="icon-map-marker"></i><g:message
									code="observation.location.label"
									default="Location title" /> </label>
									
								<div class="controls textbox">
									<input id="place_name" type="text" name="place_name" class="input-block-level"
										value="${defaultPlaceName}"></input>
								</div>

							</div>

							<div class="row control-group">
								<%
	                              	def defaultAccuracy = (obvInfoFeeder?.locationAccuracy) ? obvInfoFeeder.locationAccuracy : "Approximate"
	                                def isAccurateChecked = (defaultAccuracy == "Accurate")? "checked" : ""
	                                def isApproxChecked = (defaultAccuracy == "Approximate")? "checked" : ""
	                            %>
	                             <label for="location_accuracy" class="control-label" style="padding:0px"><g:message
									code="observation.accuracy.label"
									default="Accuracy" /> </label>
									
	                            <div class="controls">                
	                                <input type="radio" name="location_accuracy" value="Accurate" ${isAccurateChecked} >Accurate 
	                                <input type="radio" name="location_accuracy" value="Approximate" ${isApproxChecked} >Approximate<br />
	                            </div>
	                        </div>
	
	                        <div class="row control-group">
	                        	<label for="location_accuracy" class="control-label" style="padding:0px"><g:message
									code="observation.geoprivacy.label"
									default="Geoprivacy" /> </label>
	                                
	                            <div class="controls">  
                						<input type="checkbox" class="input-block-level"
	                                        name="geo_privacy" value="geo_privacy" />
                						Hide precise location
	                            </div>
	                        </div>
	                        <hr>
	                        <div class="row control-group">
	                        	<label for="location_accuracy" class="control-label" style="padding:0px"><g:message
									code="observation.geocode.label"
									default="Geocode name" /> </label>
								<div class="controls">                
	                                <div class="location_picker_value" id="reverse_geocoded_name"></div>
	                                <input id="reverse_geocoded_name_field" type="hidden"  class="input-block-level"
	                                        name="reverse_geocoded_name" > </input>
	                            </div>
	                        </div>
                                <div><input id="use_dms" class="input-block-level" type="checkbox" name="use_dms" value="use_dms" />
                                    Use deg-min-sec format for lat/long
	                        </div>

	                        <div class="row control-group">
	                        	<label for="location_accuracy" class="control-label"><g:message
									code="observation.latitude.label"
									default="Latitude" /> </label>
	                            <div class="controls textbox">             
	                                <!-- div class="location_picker_value" id="latitude"></div>
	                                <input id="latitude_field" type="hidden" name="latitude"></input-->
	                                <input class="degree_field input-block-level" id="latitude_field" type="text" name="latitude"></input>
	                                <input class="dms_field" id="latitude_deg_field" type="text" name="latitude_deg" placeholder="deg"></input>
	                                <input class="dms_field" id="latitude_min_field" type="text" name="latitude_min" placeholder="min"></input>
	                                <input class="dms_field" id="latitude_sec_field" type="text" name="latitude_sec" placeholder="sec"></input>
	                                <input class="dms_field" id="latitude_direction_field" type="text" name="latitude_direction" placeholder="direction"></input>
	                            </div>
	                        </div>
	                        <div class="row control-group">
	                      	  <label for="location_accuracy" class="control-label"><g:message
									code="observation.longitude.label"
									default="Longitude" /> </label>
	                            <div class="controls textbox">               
	                                <!--div class="location_picker_value" id="longitude"></div>
	                                <input id="longitude_field" type="hidden" name="longitude"></input-->
	                                <input class="degree_field input-block-level" id="longitude_field" type="text" name="longitude"></input>
	                                <input class="dms_field" id="longitude_deg_field" type="text" name="longitude_deg" placeholder="deg"></input>
	                                <input class="dms_field" id="longitude_min_field" type="text" name="longitude_min" placeholder="min"></input>
	                                <input class="dms_field" id="longitude_sec_field" type="text" name="longitude_sec" placeholder="sec"></input>
	                                <input class="dms_field" id="longitude_direction_field" type="text" name="longitude_direction" placeholder="direction"></input>
	                            </div>
	                        </div>
	                  
	                </div>
	                	<div class=" span6 sidebar-section section" style="padding:0;width:430px;">
                                    <div class="map_search">
                                            <div id="geotagged_images" style="padding:10px;">
                                                    <div class="title" style="display: none">Use location
                                                            from geo-tagged image:</div>
                                                    <div class="msg" style="display: none">Select image if
                                                            you want to use location information embedded in it</div>
                                            </div>

                                            <div id="current_location" class="section-item">
                                                    <div class="location_picker_button"><a href="#" onclick="return false;">Use current location</a></div>
                                            </div>
                                            <input id="address" type="text" title="Find by place name"  class="input-block-level"
                                                    class="section-item" />
                                                                               


	                    	<div id="map_area">
	                        	<div id="map_canvas"></div>
	                    	</div>
	                    	</div>
	                    </div>
	                </div>
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
									<ckeditor:editor name="notes" height="200px" toolbar="editorToolbar">
										${observationInstance?.notes}
									</ckeditor:editor>
								</div>
							</div>

							<div class="span6 block sidebar-section" style="margin:0px 0px 20px -10px;">
								<h5><label><i
									class="icon-tags"></i>Tags <small><g:message code="observation.tags.message" default="" /></small></label>
								</h5>
								<div class="create_tags section-item" style="clear: both;">
									<ul id="tags" name="tags">
										<g:each in="${observationInstance.tags}" var="tag">
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
							style="float: right; margin-right: 5px;"> Cancel </a>
					</g:if>
					<g:else>
					<a href="${uGroup.createLink(controller:params.controller, action:'list')}" class="btn"
							style="float: right; margin-right: 5px;"> Cancel </a>
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
					
				</div>
				
				

            </form>
           <%
				def obvTmpFileName = observationInstance?.resource?.iterator()?.next()?.fileName
				def obvDir = obvTmpFileName ?  obvTmpFileName.substring(0, obvTmpFileName.lastIndexOf("/")) : ""
	       %>
            <form id="upload_resource" enctype="multipart/form-data"
				title="Add a photo for this observation"
                                method="post"
				class="${hasErrors(bean: observationInstance, field: 'resource', 'errors')}">

				<!-- TODO multiple attribute is HTML5. need to chk if this gracefully falls back to default in non compatible browsers -->
				<input type="file" id="attachFiles" name="resources"
					accept="image/*" multiple/> <span class="msg" style="float: right"></span>
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
                        <img id='image_{{=i}}' style="width:auto; height: auto;" src='{{=thumbnail}}' class='geotagged_image' exif='true'/> 
                </span>
	    </div>
				
	    <div class='metadata prop' style="position:relative; left: 5px; top:-30px;">
	            <input name="file_{{=i}}" type="hidden" value='{{=file}}'/>
                <div id="license_div_{{=i}}" class="licence_div dropdown">
                    <a id="selected_license_{{=i}}" class="btn dropdown-toggle btn-mini" data-toggle="dropdown">
                        <img src="${resource(dir:'images/license',file:'cc_by.png', absolute:true)}" title="Set a license for this image"/>
                        <b class="caret"></b>
                    </a>
                    <ul id="license_options_{{=i}}" class="dropdown-menu license_options">
                         <span>Choose a license</span>
                         <g:each in="${species.License.list()}" var="l">
                             <li class="license_option" onclick="$('#license_{{=i}}').val($.trim($(this).text()));$('#selected_license_{{=i}}').find('img:first').replaceWith($(this).html());">
                                 <img src="${resource(dir:'images/license',file:l?.name.getIconFilename()+'.png', absolute:true)}"/><span style="display:none;">${l?.name?.value}</span>
                             </li>
                         </g:each>
                     </ul>
           		</div>	
	    	    <input id="license_{{=i}}" type="hidden" name="license_{{=i}}"></input>
          
        	    <!--a href="#" onclick="removeResource(event);$('#geotagged_images').trigger('update_map');">Remove</a-->
		</div>
       	<div class="close_button" onclick="removeResource(event, {{=i}});$('#geotagged_images').trigger('update_map');"></div>
	</li>
	
</script>

	<r:script>
	
    var add_file_button = '<li id="add_file" class="addedResource" style="display:none;" onclick="$(\'#attachFiles\').select()[0].click();return false;"><div class="progress"><div id="translucent_box"></div><div id="progress_bar"></div ><div id="progress_msg"></div ></div></li>';

	
	$(document).ready(function(){
		$('.dropdown-toggle').dropdown();

        //hack: for fixing ie image upload
        if (navigator.appName.indexOf('Microsoft') != -1) {
            $('#upload_resource').css({'visibility':'visible'});
            $('#add_file').hide();
        } else {
            $('#upload_resource').css({'visibility':'hidden'});
            $('#add_file').show();
        }
		
		$('#attachFiles').change(function(e){
  			$('#upload_resource').submit().find("span.msg").html("Uploading... Please wait...");
		});
       	
        function progressHandlingFunction(e){
            if(e.lengthComputable){
                var position = e.position || e.loaded;
                var total = e.totalSize || e.total;

                var percentVal = ((position/total)*100).toFixed(0) + '%';
                $('#progress_bar').width(percentVal)
                $('#translucent_box').width('100%')
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
				return true;
			}, 
            xhr: function() {  // custom xhr
                myXhr = $.ajaxSettings.xhr();
                if(myXhr.upload){ // check if upload property exists
                    myXhr.upload.addEventListener('progress', progressHandlingFunction, false); // for handling the progress of the upload
                }
                return myXhr;
            },
			success: function(responseXML, statusText, xhr, form) {
				$(form).find("span.msg").html("");
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
				$(responseXML).find('resources').find('image').each(function() {
					var fileName = $(this).attr('fileName');
					var size = $(this).attr('size');
					var image = rootDir + obvDir + "/" + fileName.replace(/\.[a-zA-Z]{3,4}$/, "${grailsApplication.config.speciesPortal.resources.images.gallery.suffix}");
					var thumbnail = rootDir + obvDir + "/" + fileName.replace(/\.[a-zA-Z]{3,4}$/, "${grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix}");
  					images.push({i:++i, file:obvDir + "/" + fileName, thumbnail:thumbnail, title:fileName});
				});

                                $("#add_file").remove();
                                
				
				var html = $( "#metadataTmpl" ).render( images );
				var metadataEle = $(html)
				metadataEle.each(function() {
					$('.geotagged_image', this).load(function(){
						update_geotagged_images_list($(this));		
					});
				})
				$( "#imagesList" ).append (metadataEle);

                if (navigator.appName.indexOf('Microsoft') == -1) {
                    $( "#imagesList" ).append (add_file_button);
                }
                $( "#add_file" ).fadeIn(3000);
                $("#image-resources-msg").parent(".resources").removeClass("error");
                $("#image-resources-msg").html("");

			}, error:function (xhr, ajaxOptions, thrownError){
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

       
        $(".habitat_option").click(function(){
                $("#habitat_id").val($(this).val());
                var caret = "<span class='caret'></span>";
                $("#selected_habitat").html($(this).html() + caret);
                //$("#habitat_options").hide();
                $("#selected_habitat").css({'background-color':'#e5e5e5', 'border-bottom-color':'#aeaeae'});
        });
       
        $("#name").watermark("Suggest a species name");
        $("#place_name").watermark("Set a title for this location");
       
        if(${obvInfoFeeder?.latitude && obvInfoFeeder?.longitude}){
        	set_location(${obvInfoFeeder?.latitude}, ${obvInfoFeeder?.longitude});
        }
       
        $("#help-identify input").click(function(){
                if ($(this).is(':checked')){
                    $('.nameContainer input').val('');
                    $('.nameContainer input').attr('disabled', 'disabled');
                }else{
                    $('.nameContainer input').removeAttr('disabled');
                }
        });
        
 		$(".tagit-input").watermark("Add some tags");
        $("#tags").tagit({select:true,  tagSource: "${uGroup.createLink(controller:'observation', action: 'tags')}", triggerKeys:['enter', 'comma', 'tab'], maxLength:30});
		$(".tagit-hiddenSelect").css('display','none');

 		 $("#addObservationSubmit").click(function(){
 		 	$(this).addClass("disabled");
        	var tags = $("ul[name='tags']").tagit("tags");
        	$.each(tags, function(index){
        		var input = $("<input>").attr("type", "hidden").attr("name", "tags."+index).val(this);
				$('#addObservation').append($(input));	
        	})
        	
			$("#userGroupsList").val(getSelectedUserGroups());	       	
        	$("#addObservation").submit();        	
        	return false;
        
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
	});


	function removeResource(event, imageId) {
		$(event.target).parent('.addedResource').remove();
		$("#image_"+imageId).remove();
	}
	
	$( "#observedOn" ).datepicker({ 
			changeMonth: true,
			changeYear: true,
			dateFormat: 'dd/mm/yy' 
	});
	
</r:script>
</body>
</html>

