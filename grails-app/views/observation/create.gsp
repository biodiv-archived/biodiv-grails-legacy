<%@page import="species.utils.ImageType"%>
<%@page import="org.springframework.web.context.request.RequestContextHolder"%>
<%@page import="species.License"%>
<%@page import="species.License.LicenseType"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<%@ page import="org.grails.taggable.Tag"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<div id="fb-root"></div>

<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'observation.label', default: 'Observation')}" />
<title><g:message code="default.create.label"
		args="[entityName]" />
</title>

<link rel="stylesheet"
	href="${resource(dir:'css',file:'location_picker.css', absolute:true)}"
	type="text/css" media="all" />
<link rel="stylesheet"
	href="${resource(dir:'css',file:'tagit/tagit-custom.css', absolute:true)}"
	type="text/css" media="all" />

<script src="http://maps.google.com/maps/api/js?sensor=true"></script>
<g:javascript src="jquery/jquery.exif.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}"></g:javascript>
<g:javascript src="jquery/jquery.watermark.min.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}"></g:javascript>
<g:javascript src="location/location-picker.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}"></g:javascript>

<g:javascript src="jsrender.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}"></g:javascript>

<g:javascript src="tagit.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}"></g:javascript>


</head>
<body>
	<div class="container outer-wrapper">

            <div class="row">
		<div class="span12">
                    <div class="page-header">
			<h1>
				<!--g:message code="default.create.label" args="[entityName]" /-->
				Add an observation
			</h1>
                    </div>
			<g:if test="${flash.message}">
				<div class="message">
					${flash.message}
                        </div>
                </g:if>

                <g:hasErrors bean="${observationInstance}">
                	<div class="errors">
                    	<g:renderErrors bean="${observationInstance}" as="list" />
					</div>
                </g:hasErrors>
            </div>

            <form id="upload_resource" enctype="multipart/form-data"
                    style="visibility:hidden; position: relative; float: left; z-index: 2; left: 30px; top: 2300px;" title="Add a photo for this observation"
                    class="${hasErrors(bean: observationInstance, field: 'resource', 'errors')}">

                    <!-- TODO multiple attribute is HTML5. need to chk if this gracefully falls back to default in non compatible browsers -->
                    <input type="file" id="attachFiles" name="resources" multiple="multiple" accept="image/*" />
                    <span class="msg" style="float: right"></span>
            </form>
            
            <%
				def form_id = "addObservation"
				def form_action = createLink(action:'save')
				def form_button_name = "Add Observation"
				def form_button_val = "Add Observation"
				if(params.action == 'edit' || params.action == 'update'){
					form_id = "updateObservation"
					form_action = createLink(action:'update', id:observationInstance.id)
				 	form_button_name = "Update Observation"
					form_button_val = "Update Observation"
				}
			
			%>
			 <form id="${form_id}" action="${form_action}" method="POST">

            <div class="span12 super-section" style="clear:both;">    
                <div class="span11 section bold_section" style="position:relative;overflow:visible;">
                    <h3>What did you observe?</h3>
                        <div class="row">
                            <div class="span4">
                                <label for="group"><g:message
                                                code="observation.group.label" default="Group" />
                                </label> 
                             </div>   
                             <div class="span7">
                                <div id="groups_div" class="bold_dropdown" style="z-index:3;">
                                    <%
                                        def defaultGroupId = observationInstance?.group?.id
                                        def defaultGroupIconFileName = (defaultGroupId)? SpeciesGroup.read(defaultGroupId).icon(ImageType.VERY_SMALL)?.fileName?.trim() : SpeciesGroup.findByName('All').icon(ImageType.VERY_SMALL)?.fileName?.trim()
                                        def defaultGroupValue = (defaultGroupId) ? SpeciesGroup.read(defaultGroupId).name : "Select group"
                                        %>
                                        <div id="selected_group" class="btn dropdown-toggle selected_value ${hasErrors(bean: observationInstance, field: 'group', 'errors')}" data-toggle="dropdown">
                                                                            <img class="group_icon" 
                                                                                    src="${createLinkTo(dir: 'images', file: defaultGroupIconFileName, absolute:true)}"/>
                                                                    <span class="display_value">${defaultGroupValue}</span><span class="caret"></span>
                                                                    </div>
                                        
                                        <div id="group_options" class="dropdown-menu">
                                                <ul>
                                                    <g:each in="${species.groups.SpeciesGroup.list()}" var="g">
                                                        <g:if
                                                                test="${!g.name.equals(grailsApplication.config.speciesPortal.group.ALL)}">
                                                                <li class="group_option" style="display:inline-block;padding:5px;" value="${g.id}">
                                                                            <img class="group_icon"
                                                                            	src="${createLinkTo(dir: 'images', file: g.icon(ImageType.VERY_SMALL)?.fileName?.trim(), absolute:true)}"/>
                                                                            <span class="display_value">${g.name}</span>
                                                                </li>
                                                        </g:if>
                                                    </g:each>
                                                </ul>
                                        </div>
                                </div>
                                <input id="group_id" type="hidden" name="group_id"  value="${observationInstance?.group?.id}"></input>
                            </div>
                        </div>

                        <div class="row">
                            <div class="span4">
                                <label>Habitat</label>
                            </div>    
                            <div class="span7">
                                <div id="habitat_list">
                                    <div id="habitat_div" class="bold_dropdown" style="z-index:2;">
                                    <%
                                                                            def defaultHabitatId = observationInstance?.habitat?.id
																			def defaultHabitat = Habitat.read(defaultHabitatId);
                                                                            def defaultHabitatIconFileName = (defaultHabitatId)? defaultHabitat.icon(ImageType.VERY_SMALL)?.fileName?.trim() : Habitat.findByName('All').icon(ImageType.VERY_SMALL)?.fileName?.trim()
                                                                            def defaultHabitatValue = (defaultHabitatId) ? defaultHabitat.name : "Select habitat"
                                                                    %>
                                        <div id="selected_habitat" class="btn dropdown-toggle selected_value ${hasErrors(bean: observationInstance, field: 'habitat', 'errors')}" data-toggle="dropdown">
                                        	<img class="group_icon" src="${createLinkTo(dir: 'images', file:defaultHabitatIconFileName, absolute:true)}"/>
                                        	
                                        <span class="display_value">${defaultHabitatValue}</span><span class="caret"></span></div>
                                            <div id="habitat_options" class="dropdown-menu">                                       <ul>
                                                    <g:each in="${species.Habitat.list()}" var="h">
                                                            <li class="habitat_option" value="${h.id}" >
                                                            	<img class="group_icon" src="${createLinkTo(dir: 'images', file:h.icon(ImageType.VERY_SMALL)?.fileName?.trim(), absolute:true)}"/>
                                                            	<span class="display_value">${h.name}</span></li>
                                                            </g:each>
                                                    </ul>
                                            </div>
                                        </div>
                                    </div>	
                                                                    <input id="habitat_id" type="hidden" name="habitat_id"  value="${observationInstance?.habitat?.id}"></input>
                                </div>
                            </div>

                        <div class="row">
                            <div class="span4">
                                <label for="recommendationVote"><g:message
                                                code="observation.recommendationVote.label"
                                                default="Species name" />
                                </label>
                            </div>
                            <div class="span7">
                                <g:hasErrors bean="${recommendationVoteInstance}">
                                        <div class="errors">
                                                <g:renderErrors bean="${observationInstance}" as="list" />
                                        </div>
                                </g:hasErrors>

                                <reco:create />
                            </div>
                        </div>

                        <div class="row">
                            <div class="span4">
                                <label for="observedOn"><i class="icon-calendar"></i><g:message
                                            code="observation.observedOn.label" default="Observed on" />
                                </label>
                            </div>
                            <div class="span7">
                                <input name="observedOn" type="date" id="observedOn" value="${observationInstance?.observedOn?.format('MM/dd/yyyy')}"/>
                            </div>
                        </div>
                        
                    </div>

                <div class="span11 section">
                <i class="icon-picture"></i>
                    <div class="resources">
                        <ul id="imagesList" class="thumbwrap thumbnails"
                                style='list-style: none; margin-left: 0px;'>
                                <g:set var="i" value="1" />
                                <g:each in="${observationInstance?.resource}" var="r">
                                        <li class="addedResource thumbnail">
                                                <%def thumbnail = r.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix)%>
                                                <div class='figure' style="height: 200px; overflow: hidden;">
                                                        <span> 
                                                            <img
                                                                style="width:100%; height:auto;" src='${createLinkTo(file: thumbnail, base:grailsApplication.config.speciesPortal.observations.serverURL)}'
                                                                class='geotagged_image' exif='true' /> 
                                                        </span>
                                                </div>

                                                <div class='metadata prop' style="position:relative; left: 5px; top:-40px;">
                                                        <input name="file_${i}" type="hidden" value='${r.fileName}' />
                                                        <div id="license_div_${i}" class="licence_div btn-group" style="z-index:2;cursor:pointer;">

                                                            <div id="selected_license_{i}" onclick="$(this).next().show();" class="btn dropdown-toggle" data-toggle="dropdown">
                                                                <img src="${resource(dir:'images/license',file:'cc_by.png', absolute:true)}" title="Set a license for this image"/>
                                                                <span class="caret"></span>
                                                            </div>
                                                            <div id="license_options_{i}" class="license_options">
                                                                <ul class="dropdown-menu">
                                                                    <g:each in="${species.License.list()}" var="l">
                                                                        <li class="license_option" onclick="$('#license_{{=i}}').val($(this).text());$('#selected_license_{{=i}}').html($(this).html());$('#license_options_{{=i}}').hide();">
                                                                            <img src="${resource(dir:'images/license',file:'cc_by.png', absolute:true)}"/>
                                                                        </li>
                                                                    </g:each>
                                                                </ul>
                                                            </div>
                                                        </div>
                                                </div>	
						<input id="license_${i}" type="hidden" name="license_${i}" ></input>
                                                <div class="close_button" onclick="removeResource(event);$('#geotagged_images').trigger('update_map');"></div>
                                                
                                            </li>
                                    <g:set var="i" value="${i+1}" />
                                    </g:each>
                                    <li id="add_file" class="addedResource" onclick="$('#attachFiles').select()[0].click();return false;">
                                        <div class="progress">
                                            <div id="translucent_box"></div >
                                            <div id="progress_bar"></div >
                                            <div id="progress_msg"></div >
                                        </div>
                                    </li>
                        </ul>
                    </div>
                </div>
                                   
                </div>
            </div>

            <div class="row">
            <div class="span12 super-section">    
                <div class="span11 section" style="clear:both">
                    <h3>Where did you find this observation?</h3>
                    
                    <div class="span6">
                         <div  class="map_search">   
                         <input id="address" type="text" title="Find by place name" class="section-item"/>
                         <div id="current_location" class="section-item" style="float:left">
                            <a href="#" onclick="return false;">Use current location</a>
                         </div>
                         <div id="geotagged_images" class="section-item">
                            <div class="title" style="display:none">Use location from geo-tagged image:</div>  	
                            <div class="msg" style="display:none">Select image if you want to use location information embedded in it</div>  	
                        </div>
                        </div>



                        <div class="row">
                        <%
                                                    def defaultPlaceName = (observationInstance) ? observationInstance.placeName : ""
                                            %>
                            <div class="span2">                
                                <label><i class="icon-map-marker"></i>Location title</label>
                            </div>
                            <div class="span4">                
                                <input id="place_name" type="text"
                                        name="place_name" value="${defaultPlaceName}" ></input>
                            </div>
                                        
                        </div>
                        <div class="row">
                        <%
                                                    def defaultAccuracy = (observationInstance?.locationAccuracy) ? observationInstance.locationAccuracy : "Approximate"
                                                    def isAccurateChecked = (defaultAccuracy == "Accurate")? "checked" : ""
                                                    def isApproxChecked = (defaultAccuracy == "Approximate")? "checked" : ""
                                            %>
                            <div class="span2">                
                                <label>Accuracy</label> 
                            </div>
                            <div class="span4">                
                                <input type="radio" name="location_accuracy" value="Accurate" ${isAccurateChecked} >Accurate 
                                <input type="radio" name="location_accuracy" value="Approximate" ${isApproxChecked} >Approximate<br />
                            </div>
                        </div>

                        <div class="row" style="margin-bottom:20px;">
                            <div class="span2">                
                                <label>Hide precise location?</label>
                            </div>
                                
                            <div class="span4">                
                                <input type="checkbox"
                                        name="geo_privacy" value="geo_privacy" />Hide
                            </div>
                        </div>
                        <hr>
                        <div class="row" style="margin-top:20px;">
                            <div class="span2">                
                                <label>Geocode name</label>
                            </div>
                            <div class="span4">                
                                <div class="location_picker_value" id="reverse_geocoded_name"></div>
                                <input id="reverse_geocoded_name_field" type="hidden"
                                        name="reverse_geocoded_name" value="${observationInstance?.reverseGeocodedName}" > </input>
                            </div>
                        </div>
                        <div class="row">
                            <div class="span2">                
                                <label>Latitude</label>
                            </div>
                            <div class="span4">                
                                <div class="location_picker_value" id="latitude"></div>
                                <input id="latitude_field" type="hidden" name="latitude" value="${observationInstance?.latitude}" ></input>
                            </div>
                        </div>
                        <div class="row">
                            <div class="span2">                
                                <label>Longitude</label>
                            </div>
                            <div class="span4">                
                                <div class="location_picker_value" id="longitude"></div>
                                <input id="longitude_field" type="hidden" name="longitude" value="${observationInstance?.longitude}"></input>
                            </div>
                        </div>
                  
                </div>
                <div class="span5 section map_section" style="padding:0; margin:0;">
                   
                    <div id="map_area">
                        <div id="map_canvas"></div>
                    </div>

                    </div>
                </div>
            </div>    
            
            </div>    
            <div class="row">
                <div class="span12 super-section">    
                    <div class="span11 section" style="clear:both">
                        <h3>Describe your observation!</h3>
                        <div class="span6">
                            <!--label for="notes"><g:message code="observation.notes.label" default="Notes" /></label-->
                            <label style="float:left;text-align:left;padding-left:10px;width:auto;"><i class="icon-pencil"></i>Notes</label> (Max: 400 characters)<br/>
                            <div class="section-item" style="margin-right:10px;">
                            <g:textArea name="notes" value="${observationInstance?.notes}"
                                                            class="text ui-corner-all" />
                            </div>
                        </div>

                    <div class="span5" style="border-radius:5px; background-color:#c4cccf; margin:0;">
                        <label style="float:left;text-align:left;padding:10px;width:auto;"><i class="icon-tags"></i>Tags</label>

                        <div class="create_tags section-item" style="clear:both;">
                            <ul name="tags">
                                <g:each in="${observationInstance?.tags}">
                                </g:each>
                            </ul>
                        </div>
                    </div>
                </div>    
            </div>
            <div class="span12">
                <input class="btn btn-primary btn-large" type="submit" name="${form_button_name}" value="${form_button_val}" style="margin-top:20px;margin-bottom:40px; float:right;"/>
            <g:if test="${observationInstance?.id}">
	        	<div class="btn btn-danger" style="float:right;">
                            <a href="${createLink(controller:'observation', action:'flagDeleted', id:observationInstance.id)}" onclick="return confirm('${message(code: 'default.observatoin.delete.confirm.message', default: 'This observation will be deleted. Are you sure ?')}');">Delete Observation </a>
                        </div>     
	    </g:if>
            </div>

                </form>

                </div>
            </div>
        </div>

		<!--====== Template ======-->
		<script id="metadataTmpl" type="text/x-jquery-tmpl">
	<li class="addedResource thumbnail">
	    <div class='figure' style='height: 200px; overflow:hidden;'>
                <span> 
                        <img style="width:100%; height: auto;" src='{{=thumbnail}}' class='geotagged_image' exif='true'/> 
                </span>
	    </div>
				
	    <div class='metadata prop' style="position:relative; left: 5px; top:-40px;">
	        <input name="file_{{=i}}" type="hidden" value='{{=file}}'/>
                <div id="license_div_{{=i}}" class="licence_div btn-group" style="z-index:2;cursor:pointer;">
                    <div id="selected_license_{{=i}}" onclick="$(this).next().show();" class="btn dropdown-toggle" data-toggle="dropdown">
                        <img src="${resource(dir:'images/license',file:'cc_by.png', absolute:true)}" title="Set a license for this image"/>
                        <span class="caret"></span>
                    </div>
                    <div id="license_options_{{=i}}" class="license_options">                                                                                                            <ul class="dropdown-menu">
                            <g:each in="${species.License.list()}" var="l">
                                <li class="license_option" onclick="$('#license_{{=i}}').val($(this).text());$('#selected_license_{{=i}}').html($(this).html());$('#license_options_{{=i}}').hide();">
                                    <img src="${resource(dir:'images/license',file:l?.name.getIconFilename()+'.png', absolute:true)}"/>
                                </li>
                            </g:each>
                        </ul>
                    </div>
                </div>
            </div>	
	    <input id="license_{{=i}}" type="hidden" name="license_{{=i}}"></input>
            
            <!--a href="#" onclick="removeResource(event);$('#geotagged_images').trigger('update_map');">Remove</a-->
            <div class="close_button" onclick="removeResource(event);$('#geotagged_images').trigger('update_map');"></div>
	</li>
	
</script>

		<g:javascript>
	
        var mouse_inside_groups_div = false;        
        var mouse_inside_habitat_div = false;        
        var add_file_button = '<li id="add_file" class="addedResource" style="display:none;" onclick="$(\'#attachFiles\').select()[0].click();return false;"><div class="progress"><div id="translucent_box"></div><div id="progress_bar"></div ><div id="progress_msg"></div ></div></li>';

	
	$(document).ready(function(){

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
			url:'${createLink(controller:'observation', action:'upload_resource')}',
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
				var rootDir = '${grailsApplication.config.speciesPortal.observations.serverURL}'
				var obvDir = $(responseXML).find('dir').text();
				var images = []
				var i = $(".metadata").length;
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
                $( "#imagesList" ).append (add_file_button);
                $( "#add_file" ).fadeIn(3000);

			}, error:function (xhr, ajaxOptions, thrownError){
					$('#upload_resource').find("span.msg").html("");
					var messageNode = $(".message .resources") 
					var response = $.parseJSON(xhr.responseText);					
					if(messageNode.length == 0 ) {
						$("#upload_resource").prepend('<div class="message">'+(response?response.error:"Error")+'</div>');
					} else {
						messageNode.append(response?response.error:"Error");
					}
                        } 
     	});  
		/*
		//"aaaaa" 
		//alert("${observationInstance?.observedOn?.getTime()}")
		var currDate = new Date();
		if(${observationInstance?.observedOn != null}){
			currDate = new Date(${observationInstance?.observedOn?.getTime()})
			console.log(currDate);
		}
        var prettyDate =(currDate.getMonth()+1) + '/' + currDate.getDate() + '/' +  currDate.getFullYear();
        $("#observedOn").val(prettyDate);
        //alert(prettyDate);
		*/
		var defaultInitialTags = ["'zz'", "'tt'"]
<%--		if(${observationInstance?.tags != null}){--%>
<%--			defaultInitialTags = ${observationInstance.tags}--%>
<%--		}--%>
<%--		alert("test");--%>
<%--		alert(defaultInitialTags);--%>
		//$("ul[name='tags']").tagit({select:true, initialTags:defaultInitialTags, tagSource: "${g.createLink(action: 'tags')}"});
		$("ul[name='tags']").tagit({select:true,  tagSource: "${g.createLink(action: 'tags')}"});
		//$("ul[name='tags']").tagit();

        $("#selected_group").click(function(){
            $("#group_options").show();
            $(this).css({'background-color':'#fbfbfb', 'border-bottom-color':'#fbfbfb'});
        });

        $("#group_options").hover(function(){
                mouse_inside_groups_div = true;
                }, function(){
                mouse_inside_groups_div = false;
                });


        $("body").mouseup(function(){
                if(!mouse_inside_groups_div){
                    $("#group_options").hide();
                    $("#selected_group").css({'background-color':'#e5e5e5', 'border-bottom-color':'#aeaeae'});
                }
                });
		
        $(".group_option").click(function(){
                $("#group_id").val($(this).val());
                var caret = "<span class='caret'></span>";
                $("#selected_group").html($(this).html() + caret);
                $("#group_options").hide();
                $("#selected_group").css({'background-color':'#e5e5e5', 'border-bottom-color':'#aeaeae'});
        });

        $("#selected_habitat").click(function(){
            $("#habitat_options").show();
            $(this).css({'background-color':'#fbfbfb', 'border-bottom-color':'#fbfbfb'});
        });

        $("#habitat_options").hover(function(){
                mouse_inside_habitat_div = true;
                }, function(){
                mouse_inside_habitat_div = false;
                });


        $("body").mouseup(function(){
                if(!mouse_inside_habitat_div){
                    $("#habitat_options").hide();
                    $("#selected_habitat").css({'background-color':'#e5e5e5', 'border-bottom-color':'#aeaeae'});
                }
                });
		
        $(".habitat_option").click(function(){
                $("#habitat_id").val($(this).val());
                var caret = "<span class='caret'></span>";
                $("#selected_habitat").html($(this).html() + caret);
                $("#habitat_options").hide();
                $("#selected_habitat").css({'background-color':'#e5e5e5', 'border-bottom-color':'#aeaeae'});
        });
       
        $("#name").watermark("Recommend a species name");
        $("#place_name").watermark("Set a title for this location");
        $(".tagit-input").watermark("Add some tags");
        
        if(${observationInstance?.latitude && observationInstance?.longitude}){
        	set_location(${observationInstance?.latitude}, ${observationInstance?.longitude});
        }

	});

	function removeResource(event) {
		$(event.target).parent('.addedResource').remove();
	}
	
	$( "#observedOn" ).datepicker();
	
</g:javascript>
</body>
</html>

