

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
	max-width: 100%;
}
</style>
</head>
<body>


	<% 
				def form_action = uGroup.createLink(action:'save', controller:'document', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
				def form_title = "Create Document"
				def form_button_name = "Add Document"
				def form_button_val = "Add Document"
				if(params.action == 'edit' || params.action == 'update'){
					form_action = uGroup.createLink(action:'update', controller:'document', id:documentInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
					 form_button_name = "Update Document"
					form_button_val = "Update Document"
					form_title = "Update Document"
					
				}
				
				String uploadDir = "documents/"+ "document-"+UUID.randomUUID().toString()	
			%>
	<div class="span12 observation_create">
		<g:render template="/document/documentSubMenuTemplate"
			model="['entityName': form_title]" />
		<uGroup:rightSidebar />



		<form id="documentForm" action="${form_action}" method="POST"
				onsubmit="document.getElementById('documentFormSubmit').disabled = 1;"
		
			class="form-horizontal">

			<div class="span12 super-section" style="margin-left: 0px;">
				<div class="section">

					<div
						class="control-group ${hasErrors(bean: documentInstance, field: 'type', 'error')}">
						<label class="control-label" for="type"><g:message
								code="document.type.label" default="Type" /><span class="req">*</span></label>
						<div class="controls">
							<g:select name="type" class="input-block-level" placeholder="Select document type"
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

							<input type="text" class="input-block-level" name="title" placeholder="Enter the title for the document"
								value="${documentInstance?.title}" required />

							<div class="help-inline">
								<g:hasErrors bean="${documentInstance}" field="title">
									<g:message code="default.blank.message" args="['Title']" />
								</g:hasErrors>
							</div>
						</div>

					</div>


					<div
						class="control-group ${hasErrors(bean: documentInstance, field: 'uFile', 'error')}">
						<label class="control-label" for="file"> Resource <span
							class="req">*</span></label>
						<div class="controls" style="">
							<div class="span2" style="margin-left: 0px;">
								<g:render template='/UFile/docUpload'
									model="['name': 'ufilepath', 'path': documentInstance?.uFile?.path, 'size':documentInstance?.uFile?.size,'fileParams':['uploadDir':uploadDir]]" />

							</div>
							<div class="span1">(OR)</div>
							<div class="span6 control-group ${hasErrors(bean: documentInstance, field: 'uri', 'error')}" style="width: 480px;">
								<label class="control-label" for="uri" style="width: 40px;">URL</label>
								<div class="controls" style="margin-left: 55px;">
									<input type="text" class="input-block-level" name="uri"
										placeholder="Enter URL for the resource"
										value="${documentInstance?.uri}" />
								</div>
							</div>
							<div class="help-inline">
								<g:hasErrors bean="${documentInstance}" field="uFile">
									<g:message code="fileOrUrl.validator.invalid" />
								</g:hasErrors>
							</div>
						</div>
					</div>


					<div
						class="control-group ${hasErrors(bean: documentInstance, field: 'contributors', 'error')}">
						<label class="control-label" for="contributors">Contributors</label>
						<div class="controls">
							<g:textField name="contributors" class="input-block-level"
								value="${documentInstance?.contributors }" placeholder="Enter the contirbutors for the document"/>
						</div>
					</div>



					<div
						class="control-group ${hasErrors(bean: documentInstance, field: 'attribution', 'error')}">
						<label class="control-label" for="attribution">Attribution</label>
						<div class="controls">
							<g:textField name="attribution" class="input-block-level" placeholder="Enter the attribution to be given for this document"
								value="${documentInstance?.attribution}" />
						</div>
					</div>

					<div
						class="control-group ${hasErrors(bean: documentInstance, field: 'license', 'error')}">
						<label class="control-label" for="License"> License </label>

						<div class="controls">

							<div id="licenseDiv" class="licence_div dropdown">

								<a id="selected_license"
									class="btn dropdown-toggle btn-mini" data-toggle="dropdown">
									<img
									src="${documentInstance.license?resource(dir:'images/license',file:documentInstance.license.name.getIconFilename()+'.png'):resource(dir:'images/license',file:'cc_by.png', absolute:true)}"
									title="Set a license for this file" /> <b class="caret"></b>
								</a>

								<ul id="license_options"
									class="dropdown-menu license_options">
									<span>Choose a license</span>
									<g:each in="${species.License.list()}" var="l">
										<li class="license_option"
											onclick="$('#license').val($.trim($(this).text()));$('#selected_license').find('img:first').replaceWith($(this).html());">
											<img
											src="${resource(dir:'images/license',file:l?.name?.getIconFilename()+'.png', absolute:true)}" /><span
											style="display: none;"> ${l?.name?.value}
										</span>
										</li>
									</g:each>
								</ul>
								
								<input id="license" type="hidden" name="licenseName" value="${documentInstance.license?.name?.value()}"></input>
								
							</div>
						</div>
					</div>

					<hr>

					<div
						class="control-group ${hasErrors(bean: documentInstance, field: 'description', 'error')}">
						<label class="control-label" for="description">Description
						</label>
						<div class="controls">
	
                                                        <textarea id="description" name="description" placeholder="Write a small description about the document.">${documentInstance?.description}</textarea>
                                                        
                                                        <script type='text/javascript'>
                                                                CKEDITOR.plugins.addExternal( 'confighelper', '${request.contextPath}/js/ckeditor/plugins/confighelper/' );
                                                                
                                                                var config = { extraPlugins: 'confighelper', toolbar:'EditorToolbar', toolbar_EditorToolbar:[[ 'Bold', 'Italic' ]]};
                                                                CKEDITOR.replace('description', config);
                                                        </script>
                                                        <div class="help-inline">
                                                                <g:hasErrors bean="${userGroupInstance}" field="description">
                                                                        <g:eachError bean="${userGroupInstance}" field="description">
                                                                                <li><g:message error="${it}" /></li>
                                                                        </g:eachError>
                                                                </g:hasErrors>
                                                        </div>

						</div>

					</div>

					<div
						class="control-group ${hasErrors(bean: documentInstance, field: 'tags', 'error')}">
						<label class="control-label" for='tags'> <i
							class="icon-tags"></i>Tags
						</label>
						<div class="controls">
							<ul class='file-tags' id="tags" name="tags">
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
				<div class="span12 super-section"
					style="clear: both; margin-left: 0px;">
					<div class="section" style="position: relative; overflow: visible;">
						<h3>Post to User Groups</h3>
						<div>
							<%
									def docActionMarkerClass = (params.action == 'create' || params.action == 'save')? 'create' : '' 
								%>
							<div id="userGroups" class="${docActionMarkerClass}"
								name="userGroups" style="list-style: none; clear: both;">
								<uGroup:getCurrentUserUserGroups
									model="['observationInstance':documentInstance]" />
							</div>
						</div>
					</div>
				</div>
			</uGroup:isUserGroupMember>



			<div class="span12" style="margin-top: 20px; margin-bottom: 40px;">

				<g:if test="${documentInstance?.id}">
					<a
						href="${uGroup.createLink(controller:'document', action:'show', id:documentInstance.id)}"
						class="btn" style="float: right; margin-right: 30px;"> Cancel
					</a>
				</g:if>
				<g:else>
					<a
						href="${uGroup.createLink(controller:'document', action:'browser')}"
						class="btn" style="float: right; margin-right: 30px;"> Cancel
					</a>
				</g:else>

				<button id="documentFormSubmit" type="submit"
					class="btn btn-primary" style="float: right; margin-right: 5px;">
					${form_button_name}
				</button>
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
			
			
	
             $("#tags").tagit({
        	select:true, 
        	allowSpaces:true, 
        	placeholderText:'Add some tags',
        	fieldName: 'tags', 
        	autocomplete:{
        		source: '/document/tags'
        	}, 
        	triggerKeys:['enter', 'comma', 'tab'], 
        	maxLength:30
        });
		$(".tagit-hiddenSelect").css('display','none');
			
			
			
						});
						

		
    
        </r:script>
</body>
</html>
