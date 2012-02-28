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
	<div class="container_16">
		<div class="observation grid_16 big_wrapper">
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


			<form id="upload_resource" enctype="multipart/form-data"
				style="position: relative; float: right; right: 40px; top: 60px; z-index: 2">
				<!-- TODO multiple attribute is HTML5. need to chk if this gracefully falls back to default in non compatible browsers -->
				<input type="button" class="red" id="upload_button"
					value="Add photo">
				<div
					style="overflow: hidden; width: 200px; height: 49px; position: absolute; left: 0px; top: 0px;">
					<input type="file" id="attachFiles" name="resources"
						multiple="multiple" accept="image/*" />
				</div>
				<span class="msg" style="float: left"></span>
			</form>

			<form id="addObservation" action="${createLink(action:'save')}"
				method="POST">

				<div class="section">
					<h3>What did you observe?</h3>
					<label for="group"><g:message
							code="observation.group.label" default="Group" />
					</label> <select name="group_id" class="ui-widget-content ui-corner-all">
						<g:each in="${species.groups.SpeciesGroup.list()}" var="g">
							<g:if
								test="${!g.name.equals(grailsApplication.config.speciesPortal.group.ALL)}">
								<option value="${g.id}"
									${(g.id == observationInstance?.group?.id)?'selected':''}>
									${g.name}
								</option>
							</g:if>
						</g:each>
					</select> <br /> <label for="recommendationVote"><g:message
							code="observation.recommendationVote.label"
							default="Species name" />
					</label>
					<g:hasErrors bean="${recommendationVoteInstance}">
						<div class="errors">
							<g:renderErrors bean="${observationInstance}" as="list" />
						</div>
					</g:hasErrors>

					<reco:create />

					<label for="observedOn"><g:message
							code="observation.observedOn.label" default="Observed on" />
					</label> <input type="text" id="observedOn">
					
					<br/>
					Tags:
					<div class="create_tags">
						<ul name="tags">
							<g:each in="${observationInstance.tags}">
								<li>${it}</li>
							</g:each>
    					</ul>
  					</div>

					<div class="resources">
						<ul id="imagesList" class="thumbwrap"
							style='list-style: none; margin-left: 0px;'>
							<g:set var="i" value="0" />
							<g:each in="${observationInstance?.resource}" var="r">
								<li class="addedResource grid_4">
									<%def thumbnail = r.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix)%>

									<div class='figure'
										style='max-height: 220px; max-width: 200px; float: left; padding-right: 10px;'>
										<span> <img
											src='${createLinkTo(file: thumbnail, base:grailsApplication.config.speciesPortal.observations.serverURL)}'
											class='geotagged_image' exif='true' /> </span>
									</div>


									<div class='metadata prop'>
										<input name="file.${i}" type="hidden" value='${r.fileName}' />
										<label class="name grid_2">Title </label><input
											name="title.${i}" type="text" size='18'
											class='value ui-corner-all' value='${r.description}' /><br />
										<label class="name grid_2">License </label> <select
											name="license.${i}" class="value ui-corner-all">
											<g:each in="${species.License.list()}" var="l">
												<option value="${l.name.value()}"
													${(l == r.licenses.iterator().next())?'selected':''}>
													${l?.name.value()}
												</option>
											</g:each>
										</select> <br />

									</div> <a href="#" class="resourceRemove">Remove</a></li>
								<g:set var="i" value="${i+1}" />
							</g:each>
						</ul>
					</div>


				</div>
				<div class="section">
					<h3>Where did you find this observation?</h3>
					<div id="location_picker">
						<div id="selection_box" class="grid_14">
							<div id="side_bar" class="grid_7">
								<input id="address" type="text" size="70"
									title="Find by place name" />
								<div id="current_location" class="location_picker_button">
									<div style="padding: 10px">Use current location</div>
								</div>
								<div id="geotagged_images"></div>
							</div>

							<div id="map_area">
								<div id="map_canvas"></div>
							</div>
						</div>

						<div id="result_box">
							<div class="row">
								<label>Place name</label> <input id="place_name" type="text"
									name="place_name"></input>
							</div>
							<div class="row">
								<label>Reverse geocoded name</label>
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
							<div class="row">
								<label>Accuracy</label> <input type="radio"
									name="location_accuracy" value="Accurate">Accurate <input
									type="radio" name="location_accuracy" value="Approximate"
									checked>Approximate<br />
							</div>

							<div class="row">
								<label>Hide precise location?</label> <input type="checkbox"
									name="geo_privacy" value="geo_privacy" />Hide<br />
							</div>
						</div>
					</div>

				</div>

				<div class="section">
					<h3>Notes</h3>
					<!--label for="notes"><g:message code="observation.notes.label" default="Notes" /></label-->
					<br /> (Max: 400 characters)
					<g:textArea name="notes" value="${observationInstance?.notes}"
						class="text ui-corner-all" />

				</div>

				<div class="dialog">


					<span> <input class="button button-red" type="submit"
						name="Add Observation" value="Add Observation" /> </span>
			</form>
		</div>



		<!--====== Template ======-->
		<script id="metadataTmpl" type="text/x-jquery-tmpl">
	<li class="addedResource grid_4">
		<div class='figure' style='max-height: 220px; max-width: 200px;float: left;padding-right:10px;'>
			<span> 
				<img src='{{=thumbnail}}' class='geotagged_image' exif='true'/> 
			</span>
		</div>
				
		<div class='metadata prop'>
			<input name="file_{{=i}}" type="hidden" value='{{=file}}'/>
			<label class="name grid_2">Title </label><input name="title_{{=i}}" type="text" size='18' class='value ui-corner-all' value='{{=title}}'/><br/>
			
			<label class="name grid_2">License </label>
			<select name="license_{{=i}}" class="value ui-corner-all" >
				<g:each in="${species.License.list()}" var="l">
					<option value="${l.name.value()}" ${(l.name.value().equals(LicenseType.CC_BY.value()))?'selected':''}>${l?.name.value()}</option>
				</g:each>							
			</select><br/>

		</div>
                <br/>
		<div>
                <!--a href="#" onclick="removeResource(event);$('#geotagged_images').trigger('update_map');">Remove</a-->
                <div class="close_button" onclick="removeResource(event);$('#geotagged_images').trigger('update_map');"></div>
                </div>
	</li>
	
</script>

		<g:javascript>
	
	
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
			url:'${createLink(controller:'observation', action:'upload_resource')}',
			dataType: 'xml',//could not parse json wih this form plugin 
			clearForm: true,
			resetForm: true,
			type: 'POST',
			data: {uid:'${springSecurityService.getCurrentUser().id}', name:'${springSecurityService.getCurrentUser().username}'}, 
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
					console.log(xhr);
					console.log(ajaxOptions);
					console.log(thrownError);
					$('#upload_resource').find("span.msg").html("Uploading... Please wait...");
					var messageNode = $(".message .resources") 
					var response = $.parseJSON(xhr.responseText);
					if(messageNode.length == 0 ) {
						$("#upload_resource").prepend('<div class="message">'+response.error+'</div>');
					} else {
						messageNode.append(response.error);
					}
            } 
     	});  

        var currDate = new Date();
        var prettyDate =(currDate.getMonth()+1) + '/' + currDate.getDate() + '/' +  currDate.getFullYear();
        $("#observedOn").val(prettyDate);

     	$("ul[name='tags']").tagit({select:true, tagSource: "${g.createLink(action: 'tags')}"});
	});
		
	function removeResource(event) {
		$(event.target).parent().parent('.addedResource').remove();
	}
	
	$( "#observedOn" ).datepicker({
			showOn: "both",
			buttonImage: "/biodiv/images/calendar.gif",
			buttonImageOnly: true,
                        changeMonth: true,
			changeYear: true
			
	});
	
</g:javascript>
</body>
</html>

