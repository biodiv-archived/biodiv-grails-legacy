<%@page import="species.License"%>
<%@page import="species.License.LicenseType"%>
<%@ page import="species.participation.Observation"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<div id="fb-root"></div>

<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'observation.label', default: 'Observation')}" />
<title><g:message code="default.create.label"
		args="[entityName]" /></title>

<link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'css',file:'jquery.rating.css', absolute:true)}" />

<g:javascript src="jsrender.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}"></g:javascript>

</head>
<body>
	<div class="container_16">
		<div class="observation grid_16">
			<h1>
				<g:message code="default.create.label" args="[entityName]" />
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

			<form id="upload_resource" enctype="multipart/form-data">
				<!-- TODO multiple attribute is HTML5. need to chk if this gracefully falls back to default in non compatible browsers -->
				<input type="file" id="attachFiles" name="resources" multiple="multiple"  accept="image/*"/>
				<span class="msg"></span> 
			</form>


			<form id="addObservation" action="${createLink(action:'save')}"
				method="POST">
				<div class="resources">
					<ul id="imagesList" class="thumbwrap"
						style='list-style: none; margin-left: 0px;'>
						<g:set var="i" value="0" />
						<g:each in="${observationInstance?.resource}" var="r">
							<li class="addedResource grid_16">
								<%def thumbnail = r.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix)%>

								<div class='figure'
									style='max-height: 220px; max-width: 200px; float: left; padding-right:10px;'>
									<span> <img
										src='${createLinkTo(file: thumbnail, base:grailsApplication.config.speciesPortal.observations.serverURL)}' />
									</span>
								</div>

								<div class='metadata prop'>
									<input name="file.${i}" type="hidden" value='${r.fileName}' /> <label class="name grid_1">Title </label><input
										name="title.${i}" type="text" size='50'
										class='value ui-corner-all' value='${r.description}' /><br /> <label class="name grid_1">License </label> 
										<select name="license.${i}" class="value ui-corner-all">
											<g:each in="${species.License.list()}" var="l">
											<option value="${l.name.value()}"
												${(l == r.licenses.iterator().next())?'selected':''}>
												${l?.name.value()}
											</option>
										</g:each>
									</select>
										<br />

									<div class="rating">
										<input class="star" type="radio" name="rrating.${i}" value="1"
											title="Worst" /> <input class="star" type="radio"
											name="rrating.${i}" value="2" title="Bad" /> <input
											class="star" type="radio" name="rrating.${i}" value="3"
											title="OK" /> <input class="star" type="radio"
											name="rrating.${i}" value="4" title="Good" /> <input
											class="star" type="radio" name="rrating.${i}" value="5"
											title="Best" />
									</div>
								</div>
								<a href="#" class="resourceRemove">Remove</a>
							</li>
						<g:set var="i" value="${i+1}" />
						</g:each>
					</ul>
				</div>
				<br/>
				<div class="dialog grid_16">
					<table>
						<tbody>

							<tr class="prop">
								<td valign="top" class="name"><label for="observedOn"><g:message
											code="observation.observedOn.label" default="Observed On" />
								</label>
								</td>
								<td valign="top"
									class="value ${hasErrors(bean: observationInstance, field: 'observedOn', 'errors')}">
									<g:datePicker name="observedOn" precision="day"
										value="${observationInstance?.observedOn}"
										class="ui-widget-content ui-corner-all" />
								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name"><label for="recommendationVote"><g:message
											code="observation.recommendationVote.label" default="Recommendation" /> </label>
								</td>
								<td valign="top"
									class="value ${hasErrors(bean: recommendationVoteInstance, field: 'recommendation', 'errors')}">
									<g:hasErrors bean="${recommendationVoteInstance}">
										<div class="errors">
											<g:renderErrors bean="${observationInstance}" as="list" />
										</div>
									</g:hasErrors>
									
									<reco:create/>
								</td>
							</tr>
							
							<tr class="prop">
								<td valign="top" class="name"><label for="group"><g:message
											code="observation.group.label" default="Group" /> </label>
								</td>
								<td valign="top"
									class="value ${hasErrors(bean: observationInstance, field: 'group', 'errors')}">
									<g:select name="group.id" from="${species.SpeciesGroup.list()}"
										optionKey="id" optionValue="name"
										value="${observationInstance?.group?.id}" class="ui-widget-content ui-corner-all"/></td>
							</tr>
							
							<tr class="prop">
								<td valign="top" class="name"><label for="notes"><g:message
											code="observation.notes.label" default="Notes" /> </label><br />
									(Max: 400 characters)</td>
								<td valign="top"
									class="value ${hasErrors(bean: observationInstance, field: 'notes', 'errors')}">
									<g:textArea name="notes" value="${observationInstance?.notes}"
										class="text ui-corner-all" />
								</td>
							</tr>

						</tbody>
					</table>
					
				</div>

				<span class="button"> <input type="submit"
					name="Add Observation" value="Add Observation" /> </span>
			</form>
		</div>
	</div>


	<!--====== Template ======-->
	<script id="metadataTmpl" type="text/x-jquery-tmpl">
	<li class="addedResource grid_16">
		<div class='figure' style='max-height: 220px; max-width: 200px;float: left;padding-right:10px;'>
			<span> 
				<img src='{{=thumbnail}}' /> 
			</span>
		</div>
				
		<div class='metadata prop'>
			<input name="file.{{=i}}" type="hidden" value='{{=file}}'/>
			<label class="name grid_1">Title </label><input name="title.{{=i}}" type="text" size='50' class='value ui-corner-all' value='{{=title}}'/><br/>
			
			<label class="name grid_1">License </label>
			<select name="license.{{=i}}" class="value ui-corner-all" >
				<g:each in="${species.License.list()}" var="l">
					<option value="${l.name.value()}" ${(l.name.value().equals(LicenseType.CC_BY.value()))?'selected':''}>${l?.name.value()}</option>
				</g:each>							
			</select><br/>
			
			<div class="rating">
				<input class="star" type="radio" name="rrating.{{=i}}" value="1"
								title="Worst" /> <input class="star" type="radio"
								name="rrating.{{=i}}" value="2" title="Bad" /> <input
								class="star" type="radio" name="rrating.{{=i}}" value="3"
								title="OK" /> <input class="star" type="radio"
								name="rrating.{{=i}}" value="4" title="Good" /> <input
								class="star" type="radio" name="rrating.{{=i}}" value="5"
								title="Best" />
			</div>
		</div>
		<a href="#" onclick="removeResource(event)">Remove</a>
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
					$('.star', this).rating({
						callback: function(value, link){
							//alert(value);
							//$(this.form).ajaxSubmit();
						}
					});
				})
				
				$( "#imagesList" ).append (metadataEle);
			}, error:function (xhr, ajaxOptions, thrownError){
					var messageNode = $(".message .resources") 
					if(messageNode.length == 0 ) {
						$("#upload_resource").prepend('<div class="message">'+xhr.responseText+'</div>');
					} else {
						messageNode.text(xhr.responseText);
					}                  
            } 
     	});  
     	
	});
		
	function removeResource(event) {
		$(event.target).parent().remove();
	}
	
</g:javascript>
</body>
</html>

