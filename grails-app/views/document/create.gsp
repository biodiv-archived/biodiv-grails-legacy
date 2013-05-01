

<%@ page import="content.eml.Document"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'document.label', default: 'Document')}" />
<title><g:message code="default.create.label"
		args="[entityName]" /></title>
<script src="http://maps.google.com/maps/api/js?sensor=true"></script>

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
</style>
</head>
<body>

	<div class="body" style="margin-left:20px;"> 
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
			
			%>

		<form id="documentForm" action="${form_action}" method="POST"
			class="form-horizontal">


	<div
						class="control-group ${hasErrors(bean: documentInstance, field: 'type', 'error')}">
						<label class="control-label" for="type"><g:message
								code="document.type.label" default="Type" /><span
							class="req">*</span></label>
						<div class="controls">
							<g:select name="type"
								from="${content.eml.Document$DocumentType?.values()}"
								keys="${content.eml.Document$DocumentType?.values()*.name()}"
								value="${documentInstance?.type?.name()}" />

						</div>

					</div>
					<div
						class="control-group ${hasErrors(bean: documentInstance, field: 'title', 'error')}">
						<label class="control-label" for="title"><g:message
								code="document.title.label" default="Title" /><span
							class="req">*</span></label>
						<div class="controls">

							<input type="text" class="input-xxlarge" name="title"
								value="${documentInstance?.title}" required />
						</div>

					</div>


			<g:render template="/UFile/uFile"
				model="['uFileInstance':documentInstance?.uFile]"></g:render>
				
				
		<div
			class="control-group ${hasErrors(bean: documentInstance, field: 'description', 'error')}">
			<label class="control-label" for="description"> Description </label>
			<div class="controls">
				<textarea rows='5' columns='10' name='document.description'
					> ${documentInstance?.description}</textarea>
			</div>

		</div>

		<div
			class="control-group ${hasErrors(bean: documentInstance, field: 'tags', 'error')}">
			<label class="control-label" for='tags'> <i class="icon-tags"></i>Tags
			</label>
			<div class="controls">
				<ul class='file-tags' id="${fileId}-tags" name="document.tags">
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


		<div
			class="control-group ${hasErrors(bean: documentInstance, field: 'contributors', 'error')}">
			<label class="control-label" for="contributors">Contributors</label>
			<div class="controls">
				<g:textField name="document.contributors"
					value="${documentInstance?.contributors }" />
			</div>
		</div>



		<div
			class="control-group ${hasErrors(bean: documentInstance, field: 'attribution', 'error')}">
			<label class="control-label" for="attribution">Attribution</label>
			<div class="controls">
				<g:textField name="document.attribution"
					value="${documentInstance?.attribution}" />
			</div>
		</div>
				
				
			<g:render template="coverage"
				model="['coverageInstance':documentInstance?.coverage]"></g:render>



			<uGroup:isUserGroupMember>
				<div class=" super-section" style="clear: both">
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
		       	
		        $("#documentForm").submit();
		        return false;
			});
			
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
