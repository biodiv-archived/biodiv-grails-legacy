

<%@ page import="content.eml.Document"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'document.label', default: 'Document')}" />
<title><g:message code="default.create.label"
		args="[entityName]" /></title>

<r:require modules="add_file" />
<uploader:head />

<style>
.control-group.error  .help-inline {
	padding-top: 15px
}

input.dms_field {
	width: 19%;
	display: none;
}

.sidebar-section {
	width: 450px;
	margin: 0px 0px 20px -10px;
	float: right;
}

[class*="cke"] {
	max-width:100%;
}
</style>
</head>
<body>

	<div class="body" style="margin-left: 20px;">
		<h1>
			<g:message code="default.create.label" args="[entityName]" />
		</h1>
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${documentInstance}">
			<div class="errors">
				<g:renderErrors bean="${documentInstance}" as="list" />
			</div>
		</g:hasErrors>

		<% 
				def form_action = uGroup.createLink(action:'save', controller:'document', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
				def form_title = "Create Document"
				def form_button_name = "Create Document"
				def form_button_val = "Create Document"
				if(params.action == 'edit' || params.action == 'update'){
					form_action = uGroup.createLink(action:'update', controller:'document', id:documentInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
					 form_button_name = "Update Document"
					form_button_val = "Update Document"
					form_title = "Update Document"
					
				}
				
				String uploadDir = "documents/"+ "document-"+UUID.randomUUID().toString()	
			%>

		<form id="documentForm" action="${form_action}" method="POST"
			class="form-horizontal">

			<div class="span12 super-section" style="margin-left: 0px;">
				<div class="section">

					<div
						class="control-group ${hasErrors(bean: documentInstance, field: 'type', 'error')}">
						<label class="control-label" for="type"><g:message
								code="document.type.label" default="Type" /><span class="req">*</span></label>
						<div class="controls">
							<g:select name="type"
								from="${content.eml.Document$DocumentType?.values()}"
								keys="${content.eml.Document$DocumentType?.values()*.value()}"
								value="${documentInstance?.type?.value()}" />

						</div>

					</div>
					<div
						class="control-group ${hasErrors(bean: documentInstance, field: 'title', 'error')}">
						<label class="control-label" for="title"><g:message
								code="document.title.label" default="Title" /><span class="req">*</span></label>
						<div class="controls">

							<input type="text" class="input-block-level" name="title"
								value="${documentInstance?.title}" required />
						</div>

					</div>


					<div
						class="control-group ${hasErrors(bean: documentInstance.uFile, field: 'path', 'error')}">
						<label class="control-label" for="file"> Resource <span
							class="req">*</span></label>
						<div class="controls" style="inline-block">
							<div class="span2" style="margin-left:0px;">
								<g:render template='/UFile/docUpload'
									model="['name': 'ufilepath', 'path': documentInstance?.uFile?.path, 'size':documentInstance?.uFile?.size,'fileParams':['uploadDir':uploadDir]]" />
									
							</div>
							<div class="span1">(OR) 
							</div>
							<div class="span6 control-group" style="width:480px;">
							<label class="control-label" for="uri" style="width:40px;">URL</label>
							<div class="controls" style="margin-left:55px;">
								<input type="text" class="input-block-level"  name="uri" placeholder="Enter URL for the resource" 									value="${documentInstance?.uri}"  />
							</div>
							</div>
							
						</div>
					</div>


					<div
						class="control-group ${hasErrors(bean: documentInstance, field: 'contributors', 'error')}">
						<label class="control-label" for="contributors">Contributors</label>
						<div class="controls">
							<g:textField name="contributors" class="input-block-level"
								value="${documentInstance?.contributors }" />
						</div>
					</div>



					<div
						class="control-group ${hasErrors(bean: documentInstance, field: 'attribution', 'error')}">
						<label class="control-label" for="attribution">Attribution</label>
						<div class="controls">
							<g:textField name="attribution" class="input-block-level"
								value="${documentInstance?.attribution}" />
						</div>
					</div>		

					<div
						class="control-group ${hasErrors(bean: documentInstance, field: 'license', 'error')}">
						<label class="control-label" for="License"> License </label>

						<div class="controls">

							<div id="uFile.license" class="licence_div dropdown">

								<a id="selected_license_${i}"
									class="btn dropdown-toggle btn-mini" data-toggle="dropdown">
									<img
									src="${documentInstance.license?documentInstance.license.name.getIconFilename()+'.png':resource(dir:'images/license',file:'cc_by.png', absolute:true)}"
									title="Set a license for this file" /> <b class="caret"></b>
								</a>

								<ul id="license_options_${i}"
									class="dropdown-menu license_options">
									<span>Choose a license</span>
									<g:each in="${species.License.list()}" var="l">
										<li class="license_option"
											onclick="$('#license_${i}').val($.trim($(this).text()));$('#selected_license_${i}').find('img:first').replaceWith($(this).html());">
											<img
											src="${resource(dir:'images/license',file:l?.name?.getIconFilename()+'.png', absolute:true)}" /><span
											style="display: none;"> ${l?.name?.value}
										</span>
										</li>
									</g:each>
								</ul>
							</div>
						</div>
					</div>
					
					<hr>

					<div
						class="control-group ${hasErrors(bean: documentInstance, field: 'description', 'error')}">
						<label class="control-label" for="description">
							Description </label>
						<div class="controls">


							<ckeditor:config var="toolbar_editorToolbar">
									[
    									[ 'Bold', 'Italic' ]
									]
									</ckeditor:config>
							<ckeditor:editor name="description" height="200px"
								toolbar="editorToolbar">
								${documentInstance?.description}
							</ckeditor:editor>

						</div>

					</div>

					<div
						class="control-group ${hasErrors(bean: documentInstance, field: 'tags', 'error')}">
						<label class="control-label" for='tags'> <i
							class="icon-tags"></i>Tags
						</label>
						<div class="controls">
							<ul class='file-tags' id="${fileId}-tags" name="tags">
								<g:if test='${documentInstance}'>
									<g:each in="${documentInstance?.tags}" var="tag">
										<li>
											${tag}
										</li>
									</g:each>
								</g:if>
							</ul>
						</div>
					</div>



				</div>
			</div>

			<g:render template="coverage"
				model="['coverageInstance':documentInstance?.coverage]"></g:render>



			<uGroup:isUserGroupMember>
				<div class="span12 super-section" style="clear: both; margin-left:0px;">
					<div class="section" style="position: relative; overflow: visible;">
						<h3>Post to User Groups</h3>
						<div>
							<%
									def docActionMarkerClass = (params.action == 'create' || params.action == 'save')? 'create' : '' 
								%>
							<div id="userGroups" class="${docActionMarkerClass}"
								name="userGroups" style="list-style: none; clear: both;">
								<uGroup:getCurrentUserUserGroups
									model="['documentInstance':documentInstance]" />
							</div>
						</div>
					</div>
				</div>
			</uGroup:isUserGroupMember>



			<div class="" style="margin-top: 20px; margin-bottom: 40px;">

				<g:if test="${documentInstance?.id}">
					<a
						href="${uGroup.createLink(controller:'document', action:'show', id:documentInstance.id)}"
						class="btn" style="float: right; margin-right: 30px;"> Cancel
					</a>
				</g:if>
				<g:else>
					<a
						href="${uGroup.createLink(controller:'UFile', action:'browser')}"
						class="btn" style="float: right; margin-right: 30px;"> Cancel
					</a>
				</g:else>

				<g:if test="${documentInstance?.id}">
					<div class="btn btn-danger"
						style="float: right; margin-right: 5px;">
						<a
							href="${uGroup.createLink(controller:'document', action:'flagDeleted', id:documentInstance.id)}"
							onclick="return confirm('${message(code: 'default.document.delete.confirm.message', default: 'This document will be deleted. Are you sure ?')}');">Delete
							Document </a>
					</div>
				</g:if>
				<button id="documentFormSubmit" type="submit"
					class="btn btn-primary" style="float: right; margin-right: 30px;">Save</button>
			</div>

		</form>
	</div>

	<r:script>
	
	$(document).ready(function() {
		$('#use_dms').click(function(){
            if ($('#use_dms').is(':checked')) {
                $('.dms_field').fadeIn();
                $('.degree_field').hide();
            } else {
                $('.dms_field').hide();
                $('.degree_field').fadeIn();
            }
    });
    
    			$("#documentFormSubmit").click(function(){
				var speciesGroups = getSelectedGroup();
		        var habitats = getSelectedHabitat();
		        
		       	$.each(speciesGroups, function(index){
		       		var input = $("<input>").attr("type", "hidden").attr("name", "speciesGroup."+index).val(this);
					$('#documentForm').append($(input));	
		       	})
		        
		       	$.each(habitats, function(index){
		       		var input = $("<input>").attr("type", "hidden").attr("name", "habitat."+index).val(this);
					$('#documentForm').append($(input));	
		       	})
		       					$("#userGroupsList").val(getSelectedUserGroups());	       	
		       	
		        $("#documentForm").submit();
		        return false;
			});
			
			
		$('input:radio[name=groupsWithSharingNotAllowed]').click(function() {
		    var previousValue = $(this).attr('previousValue');
    
    		if(previousValue == 'true'){
        		$(this).attr('checked', false)
    		}
    
    		$(this).attr('previousValue', $(this).attr('checked'));
		});
		
		function getSelectedUserGroups() {
		    var userGroups = []; 
		    $('.userGroups button[class~="btn-success"]').each (function() {
	            userGroups.push($(this).attr('value'));
		    });
		    return userGroups;	
		}
		
			
		function getSelectedGroup() {
			    var grp = []; 
			    $('#speciesGroupFilter button').each (function() {
			            if($(this).hasClass('active')) {
			                    grp.push($(this).attr('value'));
			            }
			    });
			    return grp;	
			} 
			    
			function getSelectedHabitat() {
			    var hbt = []; 
			    $('#habitatFilter button').each (function() {
			            if($(this).hasClass('active')) {
			                    hbt.push($(this).attr('value'));
			            }
			    });
			    return hbt;	
			}
			<%
				documentInstance?.coverage?.speciesGroups.each {
					out << "jQuery('#group_${it.id}').addClass('active');";
				}
				documentInstance?.coverage?.habitats.each {
					out << "jQuery('#habitat_${it.id}').addClass('active');";
				}
			%>
			
						});
    
        </r:script>
</body>
</html>
