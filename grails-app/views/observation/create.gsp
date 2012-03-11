<%@page import="org.springframework.web.context.request.RequestContextHolder"%>
<%@page import="species.License"%>
<%@page import="species.License.LicenseType"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.groups.SpeciesGroup"%>


<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<div id="fb-root"></div>

<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'observation.label', default: 'Observation')}" />
<title><g:message code="default.create.label"
		args="[entityName]" /></title>


<link rel="stylesheet"
	href="${resource(dir:'css',file:'jquery-ui.css', absolute:true)}"
	type="text/css" media="all" />
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
	<div class="container_16 big_wrapper">
            
            <div class="grid_16">
        	<h1>
		    <!--g:message code="default.create.label" args="[entityName]" /-->
		    Add an observation
		</h1>

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
                    style="position: relative; float: left; z-index: 2; left: 20px; top: 360px;">
                    <!-- TODO multiple attribute is HTML5. need to chk if this gracefully falls back to default in non compatible browsers -->
                    <input type="button" class="red" id="upload_button"
                            value="Add photo">
                    <div
                            style="overflow: hidden; width: 100px; height: 49px; position: absolute; left: 0px; top: 0px;">
                            <input type="file" id="attachFiles" name="resources"
                                    multiple="multiple" accept="image/*" />
                    </div>
                    <span class="msg" style="float: right"></span>
            </form>

 
            <form id="addObservation" action="${createLink(action:'save')}"
	        method="POST">

            <div class="container_16 super-section" style="clear:both;">    
                <div class="grid_16 section bold_section" style="position:relative;overflow:visible;">
                    <h3>What did you observe?</h3>
                        <div class="row">
                            <label for="group"><g:message
                                            code="observation.group.label" default="Group" />
                            </label> <!--select name="group_id" class="ui-widget-content ui-corner-all">
                                    <g:each in="${species.groups.SpeciesGroup.list()}" var="g">
                                            <g:if
                                                    test="${!g.name.equals(grailsApplication.config.speciesPortal.group.ALL)}">
                                                    <option value="${g.id}"
                                                            ${(g.id == observationInstance?.group?.id)?'selected':''}>
                                                            ${g.name}
                                                    </option>
                                            </g:if>
                                    </g:each>
                            </select-->
                            <div id="groups_div" class="bold_dropdown" style="z-index:3;">
                            <div id="selected_group" class="selected_value"><img src="${resource(dir:'images/group_icons',file:'All.png', absolute:true)}"/><span class="display_value">Select group</span></div>
                            <div id="group_options" style="background-color:#fbfbfb;box-shadow:0 8px 6px -6px black; border-radius: 0 5px 5px 5px;display:none;">
                                    <ul>
                                    <g:each in="${species.groups.SpeciesGroup.list()}" var="g">
                                            <g:if
                                                    test="${!g.name.equals(grailsApplication.config.speciesPortal.group.ALL)}">
                                                    <li class="group_option" style="display:inline-block;padding:5px;" value="${g.id}"><div style="width:160px;"><img src="${createLinkTo(dir: 'images', file: g.icon()?.fileName?.trim(), absolute:true)}"/><span class="display_value">${g.name}</span></div>
                                                    </li>
                                            </g:if>
                                    </g:each>
                                    </ul>
                            </div>
                            </div>
                            <input id="group_id" type="hidden" name="group_id"></input>
                        </div>

                        <div class="row">
                                <label>Habitat</label>
                            <div id="habitat_list">
                                <!--select class="ui-widget-content">
                                        <option>None</option>
                                        <option>Forest</option>
                                        <option>Savanna</option>
                                        <option>Shrubland</option>
                                        <option>Grassland</option>
                                        <option>Wetlands</option>
                                        <option>Rocky Areas</option>
                                        <option>Caves and Subterranean Habitats</option>
                                        <option>Desert</option>
                                        <option>Marine</option>
                                        <option>Artificial - Terrestrial</option>
                                        <option>Artificial - Aquatic</option>
                                        <option>Introduced Vegetation</option>
                                        <option>Other</option>
                                        <option>Unknown</option>
                                </select-->	
                            
                                <div id="habitat_div" class="bold_dropdown" style="z-index:2;">
                                    <div id="selected_habitat" class="selected_value"><img src="${resource(dir:'images/group_icons',file:'All.png', absolute:true)}"/><span class="display_value">Select habitat</span></div>
                                        <div id="habitat_options" style="background-color:#fbfbfb;box-shadow:0 8px 6px -6px black; border-radius: 0 5px 5px 5px;display:none;">                                       <ul>
                                            	<g:each in="${species.Habitat.list()}" var="h">
                                            		<li class="habitat_option"><img src="${resource(dir:'images/group_icons',file:'All.png', absolute:true)}"/><span class="display_value">${h.name}</span></li>
                                    			</g:each>
                                        	</ul>
                                        </div>
                                    </div>
                                </div>	
								<input id="habitat" type="hidden" name="habitat"></input>
                            </div>

                        <div class="row">
                            <label for="recommendationVote"><g:message
                                            code="observation.recommendationVote.label"
                                            default="Species name" />
                            </label>
                            <g:hasErrors bean="${recommendationVoteInstance}">
                                    <div class="errors">
                                            <g:renderErrors bean="${observationInstance}" as="list" />
                                    </div>
                            </g:hasErrors>

                            <reco:create />
                        </div>



                        <div class="row">
                            <label for="observedOn"><g:message
                                        code="observation.observedOn.label" default="Observed on" />
                            </label> <input type="text" id="observedOn">
                        </div>
                        
                    </div>


                <div class="grid_16 section" style="padding-top:50px;">
                   <div class="resources">
                        <ul id="imagesList" class="thumbwrap"
                                style='list-style: none; margin-left: 0px;background:url("${resource(dir:'images',file:'add-photo.png', absolute:true)}")'>
                                <g:set var="i" value="0" />
                                <g:each in="${observationInstance?.resource}" var="r">
                                        <li class="addedResource" style="float:left; width:220px; display:inline-block; margin:2px;position:relative; padding:10px;">
                                                <%def thumbnail = r.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix)%>
                                                <div class='figure'
                                                        style='max-height: 220px; max-width: 160px;'>
                                                        <span> <img
                                                                style="width:160px;" src='${createLinkTo(file: thumbnail, base:grailsApplication.config.speciesPortal.observations.serverURL)}'
                                                                class='geotagged_image' exif='true' /> </span>
                                                </div>


                                                <div class='metadata prop'>
                                                        <input name="file.${i}" type="hidden" value='${r.fileName}' />
                                                        <!--label class="name grid_2">Title </label><input
                                                                name="title.${i}" type="text" size='18'
                                                                class='value ui-corner-all' value='${r.description}' /><br /-->
                                                        <!--label class="name grid_2">License </label--> <!--select
                                                                name="license.${i}" class="value ui-corner-all">
                                                                <g:each in="${species.License.list()}" var="l">
                                                                        <option value="${l.name.value()}"
                                                                                ${(l == r.licenses.iterator().next())?'selected':''}>
                                                                                ${l?.name.value()}
                                                                        </option>
                                                                </g:each>
                                                        </select> <br /-->
                                                           <div id="license_div" style="z-index:2;">
                                                                    <div id="selected_license" class="selected_value"><img src="${resource(dir:'images/group_icons',file:'All.png', absolute:true)}"/><span class="display_value">Copyright</span></div>
                                                                        <div id="license_options" style="background-color:#fbfbfb;box-shadow:0 8px 6px -6px black; border-radius: 0 5px 5px 5px;display:none;">                                       <ul>
                                                                                <g:each in="${species.License.list()}" var="l">
                                                                                        <li class="license_option"><img src="${resource(dir:'images/group_icons',file:'All.png', absolute:true)}"/><span class="display_value">${l?.name.value()}</span></li>
                                                                                        </g:each>
                                                                                </ul>
                                                                        </div>
                                                                    </div>
                                                                </div>	
								<input id="license" type="hidden" name="license"></input>
                                                            </div>


                                                </div> <a href="#" class="resourceRemove">Remove</a></li>
                                        <g:set var="i" value="${i+1}" />
                                </g:each>

                        </ul>

                                   </div>
                                   
                </div>
            </div>

            <div class="container_16 super-section">    
                <div class="grid_8 section" style="clear:both">
                    <h3>Where did you find this observation?</h3>
               
                     <div style="position:relative; left:20px; padding:20px 10px; margin-bottom:10px; background-color:#f7f7f7; border-radius:5px 0 0 5px;">   
                     <input id="address" type="text" title="Find by place name" class="section-item"/>
                     <div id="current_location" class="section-item" style="float:right">
                        <a href="#" onclick="return false;">Use current location</a>
                     </div>
                     <div id="geotagged_images" class="section-item">
                        <div class="title" style="display:none">Use location from geo-tagged image:</div>  	
                        <div class="msg" style="display:none">Select image if you want to use location information embedded in it</div>  	
                    </div>
                    </div>



                    <div class="row">
                        <label>Location title</label> <input id="place_name" type="text"
                                    name="place_name"></input>
                    </div>
                    <div class="row">
                            <label>Accuracy</label> <input type="radio"
                                    name="location_accuracy" value="Accurate">Accurate <input
                                    type="radio" name="location_accuracy" value="Approximate"
                                    checked>Approximate<br />
                    </div>

                    <div class="row" style="margin-bottom:20px;">
                            <label>Hide precise location?</label> <input type="checkbox"
                                    name="geo_privacy" value="geo_privacy" />Hide<br />
                    </div>
                    <hr>
                    <div class="row" style="margin-top:20px;">
                            <label>Geocode name</label>
                            <div class="location_picker_value" id="reverse_geocoded_name"></div>
                            <input id="reverse_geocoded_name_field" type="hidden"
                                    name="reverse_geocoded_name"></input>
                    </div>
                    <div class="row">
                            <label>Latitude</label>
                            <div class="location_picker_value" id="latitude"></div>
                            <input id="latitude_field" type="hidden" name="latitude"></input>
                    </div>
                    <div class="row">
                            <label>Longitude</label>
                            <div class="location_picker_value" id="longitude"></div>
                            <input id="longitude_field" type="hidden" name="longitude"></input>
                    </div>
              
                </div>
                <div class="grid_8 section" style="margin:30px 10px 10px; background-color:#f7f7f7; border-radius:5px;">
                   
                    <div id="map_area">
                        <div id="map_canvas"></div>
                    </div>

                                                            

                </div>
            </div>    
            <div class="container_16 super-section">    
                <div class="grid_8 section" style="clear:both">
                    <h3>Describe your observation!</h3>
                    <!--label for="notes"><g:message code="observation.notes.label" default="Notes" /></label-->
                    <label style="text-align:left;padding-left:10px;width:auto;">Notes</label> (Max: 400 characters)<br/>
                    <div class="section-item">
                    <g:textArea name="notes" value="${observationInstance?.notes}"
                                                    class="text ui-corner-all" />
                    </div>
                </div>
                <div class="grid_8 section" style="border-radius:5px; background-color:#c4cccf; position: relative; top: 100px;">
                    <label style="text-align:left;padding-left:10px;width:auto;">Tags</label><br/>
                    <div class="create_tags section-item">
                        <ul name="tags">
                            <g:each in="${observationInstance.tags}">
                                <li>${it}</li>
                            </g:each>
                        </ul>
                    </div>

                </div>
            </div>    
	    

	        <span> <input class="button button-red" type="submit" name="Add Observation" value="Add Observation" /> </span>
	    </form>

        </div>

		<!--====== Template ======-->
		<script id="metadataTmpl" type="text/x-jquery-tmpl">
	<li class="addedResource" style="width:220px; display:inline-block; margin:2px;position:relative;padding:2px;background-color:#ffffff;">
		<div class='figure' style='max-height: 165px; max-width: 220px; overflow:hidden;'>
			<span> 
				<img style="width:220px;" src='{{=thumbnail}}' class='geotagged_image' exif='true'/> 
			</span>
		</div>
				
		<div class='metadata prop' style="position:relative; top:15px;">
			<input name="file_{{=i}}" type="hidden" value='{{=file}}'/>
			<!--label class="name grid_2">Title </label><input name="title_{{=i}}" type="text" size='18' class='value ui-corner-all' value='{{=title}}'/><br/-->
			
			<!--label class="name grid_2">License </label-->
			<!--select name="license_{{=i}}" class="value ui-corner-all" >
				<g:each in="${species.License.list()}" var="l">
					<option value="${l.name.value()}" ${(l.name.value().equals(LicenseType.CC_BY.value()))?'selected':''}>${l?.name.value()}</option>
				</g:each>							
			</select><br/-->
                            <div id="license_div_{{=i}}" style="z-index:2;cursor:pointer;">
                                    <div id="selected_license_{{=i}}" onclick="$(this).next().show();"><img src="${resource(dir:'images/license',file:'cc_by.png', absolute:true)}"/></div>
                                        <div id="license_options_{{=i}}" style="background-color:#fbfbfb;box-shadow:0 8px 6px -6px black; border-radius: 0 5px 5px 5px;display:none;position:absolute;z-index:7;width:160px;border-width:0 1px 1px 1px;">                                       <ul>
                                            	<g:each in="${species.License.list()}" var="l">

                                            		<li class="license_option" onclick="$('#license_{{=i}}').val($(this).text());$('#selected_license_{{=i}}').html($(this).html());$('#license_options_{{=i}}').hide();"><img src="${resource(dir:'images/license',file:l?.name.getIconFilename()+'.png', absolute:true)}"/><!--span class="display_value">${l?.name.value()}</span--></li>
                                    			</g:each>
                                        	</ul>
                                        </div>
                                    </div>
                                </div>	
								<input id="license_{{=i}}" type="hidden" name="license_{{=i}}"></input>
                            </div>


		</div>
                <br/>
		<div>
                <!--a href="#" onclick="removeResource(event);$('#geotagged_images').trigger('update_map');">Remove</a-->
                <div class="close_button" onclick="removeResource(event);$('#geotagged_images').trigger('update_map');"></div>
                </div>
	</li>
	
</script>

		<g:javascript>
	
        var mouse_inside_groups_div = false;        
        var mouse_inside_habitat_div = false;        
	
	$(document).ready(function(){

		$('#attachFiles').change(function(e){
  			$('#upload_resource').submit().find("span.msg").html("Uploading... Please wait...");
		});
       	
     	//TODO:not geting called verify....
     	$("#attachFiles").ajaxStart(function(){
			var offset = $(this).offset();  				
   			$("#spinner").css({left:offset.left+$(this).width(), top:offset.top-6}).show();
   			return false;
 		});  
     		
     	$('#upload_resource').ajaxForm({ 
			url:'${createLink(controller:'observation', action:'upload_resource', params:['jsessionid':RequestContextHolder.currentRequestAttributes().getSessionId()])}',
			dataType: 'xml',//could not parse json wih this form plugin 
			clearForm: true,
			resetForm: true,
			type: 'POST',
			 
			beforeSubmit: function(formData, jqForm, options) {
				return true;
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
				
				var html = $( "#metadataTmpl" ).render( images );
				var metadataEle = $(html)
				metadataEle.each(function() {
					$('.geotagged_image', this).load(function(){
						update_geotagged_images_list($(this));		
					});
				})
				$( "#imagesList" ).append (metadataEle);
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

        var currDate = new Date();
        var prettyDate =(currDate.getMonth()+1) + '/' + currDate.getDate() + '/' +  currDate.getFullYear();
        $("#observedOn").val(prettyDate);

     	$("ul[name='tags']").tagit({select:true, tagSource: "${g.createLink(action: 'tags')}"});

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
                $("#selected_group").html($(this).html());
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
                $("#habitat").val($(this).text());
                $("#selected_habitat").html($(this).html());
                $("#habitat_options").hide();
                $("#selected_habitat").css({'background-color':'#e5e5e5', 'border-bottom-color':'#aeaeae'});
        });
        

	});

	function removeResource(event) {
		$(event.target).parent().parent('.addedResource').remove();
	}
	
	$( "#observedOn" ).datepicker({
			showOn: "both",
			buttonImage: "/biodiv/images/calendar.gif",
			buttonImageOnly: true
			
	});
	
</g:javascript>
</body>
</html>

