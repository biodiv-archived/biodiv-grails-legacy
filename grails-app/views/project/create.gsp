<%@page import="species.utils.Utils"%>

<%@ page import="content.Project"%>
<%@ page import="content.eml.Document.DocumentType"%>
<html>
<head>
<g:set var="title" value="Projects"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
<r:require modules="add_file" />
<uploader:head />
<style type="text/css">
.block {
    background-color:whitesmoke;
    position:relative;
    overflow:hidden;
    margin-bottom:5px;
    padding:5px 5px;
}

.block label {
    font-weight:bold;
}

.super-section label {
    font-weight:bold;
}

.section {
    position: relative;
    overflow: visible;
}

.super-section {
    clear:both;
    width:930px;
    margin-left:0px;
}
</style>
</head>
<body>

	<div class="span12 observation_create">

		<%
                def form_action = uGroup.createLink(action:'save', controller:'project', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
				def form_title = "Create Project"				
				def form_button_name = "Create Project"
				def form_button_val = "Create Project"
				if(params.action == 'edit' || params.action == 'update'){
					form_action = uGroup.createLink(action:'update', controller:'project', id:projectInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
					 form_button_name = "Update Project"
					form_button_val = "Update Project"
					form_title = "Update Project"
					
				}
				
				String uploadDir = "projects/"+ "project-"+UUID.randomUUID().toString()	
						
			
            %>
		<g:render template="/project/projectSubMenuTemplate"
			model="['entityName':form_title]" />
		<uGroup:rightSidebar />

		<form action="${form_action}" method="POST" id="projectForm"
			onsubmit="document.getElementById('projectFormSubmit').disabled = 1;"
			class="project-form form-horizontal" enctype="multipart/form-data">

				<input name="id" type="hidden" value="${projectInstance?.id}" /> <input
					name="uploadDir" type="hidden" value="${uploadDir}" />


				<div class="span12 super-section">
					<div class="section">

						<div
							class="control-group ${hasErrors(bean: projectInstance, field: 'direction', 'error')}">
							<label class="control-label" for="direction"><g:message
									code="project.direction.label" default="Strategic Direction" /></label>

							<div class="controls">
								<g:select name="direction.id" class="input-block-level" placeholder="Select strategic direction for this project"
									from="${content.StrategicDirection.list()}" optionKey="id"
									value="${projectInstance?.direction?.id}" />
							</div>
						</div>

						<div
							class="control-group ${hasErrors(bean: projectInstance, field: 'title', 'error')}">
							<label class="control-label" for="title"><g:message
									code="project.title.label" default="Project Title" /><span
								class="req">*</span></label>
							<div class="controls">

								<input type="text" class="input-block-level" name="title" placeholder="Enter the title for the project"
									value="${projectInstance?.title}" required />

								<div class="help-inline">
									<g:hasErrors bean="${projectInstance}" field="title">
										<g:message code="default.blank.message" args="['Title']" />
									</g:hasErrors>
								</div>
							</div>

						</div>

						<div
							class="control-group ${hasErrors(bean: projectInstance, field: 'summary', 'error')}">

							<label class="control-label" for="summary"><g:message
									code="project.summary.label" default="Summary of the Project" /></label>

                                                                    <div class="controls">

                                                                        <textarea id="summary" name="summary" class="input-block-level" style="height:200px;padding:4px 6px;" placeholder="Write a small summary about the project.">${projectInstance?.summary}</textarea>

                                                                        <script type='text/javascript'>
                                                                            CKEDITOR.plugins.addExternal( 'confighelper', '${request.contextPath}/js/ckeditor/plugins/confighelper/' );

                                                                            var image_config = { extraPlugins: 'confighelper', toolbar:'EditorToolbar', toolbar_EditorToolbar:[[ 'Bold', 'Italic', 'Image' ]]};
CKEDITOR.replace('summary', image_config);
</script>
                                                        </div>
						</div>

						<div
							class="control-group ${hasErrors(bean: projectInstance, field: 'tags', 'error')}">

							<label class="control-label" for="tags"><i
								class="icon-tags"></i> <g:message code="project.tags.label"
									default="Project Tags" /></label>

							<div class="controls">


								<ul id="tags" name="tags" style="margin-left:0px;">
									<g:each in="${projectInstance.tags}" var="tag">
										<li>
											${tag}
										</li>
									</g:each>
								</ul>
                                                                <div id="tags_ac" style="z-index:10"></div>
							</div>
						</div>
					</div>
				</div>

				<div class="span12 super-section">
					<h3>Location</h3>
					<div id="locationsDiv">
						<g:render template="locations"
							model="['projectInstance':projectInstance]" />

					</div>

				</div>

				<div class=" span12 super-section">

					<h3>Grantee Details</h3>
					<div class="span5 section" style="margin-left:0px">

						<div
							class="control-group ${hasErrors(bean: projectInstance, field: 'granteeOrganization', 'error')}">
							<label class="control-label" for="granteeOrganization"><g:message
									code="project.granteeOrganization.label"
									default="Grantee Organization" /></label>

							<div class="controls">

								<g:textField name="granteeOrganization" class="input-xlarge" placeholder="Enter the grantee organization details"
									value="${projectInstance?.granteeOrganization}" />
							</div>
						</div>


						<div
							class="control-group ${hasErrors(bean: projectInstance, field: 'granteeContact', 'error')}">
							<label class="control-label" for="granteeContact"><g:message
									code="project.granteeContact.label" default="Primary Contact" /></label>

							<div class="controls">

								<g:textField name="granteeContact" class="input-xlarge" placeholder="Enter the gratee contact details"
									value="${projectInstance?.granteeContact}" />
							</div>
						</div>
					</div>
					<div class="span5 section" style="margin-left;0px;">
						<div
							class="control-group ${hasErrors(bean: projectInstance, field: 'granteeEmail', 'error')}">
							<label class="control-label" for="granteeEmail"><g:message
									code="project.granteeEmail.label" default="Email" /></label>

							<div class="controls">
								<div class="input-prepend">
									<span class="add-on"><i class="icon-envelope"></i></span>

									<g:textField name="granteeEmail" class="input-xlarge" placeholder="Enter the gratee email address"
										value="${projectInstance?.granteeEmail}" />
									<div class="help-inline">
										<g:hasErrors bean="${projectInstance}" field="title">
											<g:message code="default.invalid.email.message" />
										</g:hasErrors>
									</div>
								</div>
							</div>
						</div>

						<div
							class="control-group ${hasErrors(bean: projectInstance, field: 'granteeLogo', 'error')}">

							<label class="control-label" for="granteeLogo"><g:message
									code="project.granteeLogo.label" default="Grantee Logo" /></label>

							<div class="controls">

								<g:render template='/UFile/imgUpload'
									model="['name': 'granteeLogo', 'path': projectInstance?.granteeLogo, 'fileParams':['uploadDir':uploadDir]]" />

							</div>
						</div>
					</div>
				</div>



				<div class="span12 super-section">
					<div id="projectDetails" class="section span6" style="margin-left:0px;">
					<h3>Project Details</h3>
						<div
							class="control-group ${hasErrors(bean: projectInstance, field: 'granteeFrom', 'error')}">

							<label class="control-label" for="grantFrom"><i
								class="icon-calendar"></i> <g:message
									code="project.grantFrom.label" default="Grant From" /></label>
							<div class="controls">
								<input name="grantFrom" type="text" id="grantFrom"
									class="date input-xlarge"
									value="${projectInstance?.grantFrom?.format('dd/MM/yyyy')}"
									placeholder="Select date" />
							</div>
						</div>

						<div
							class="control-group ${hasErrors(bean: projectInstance, field: 'granteeTo', 'error')}">
							<label class="control-label" for="grantTo"><i
								class="icon-calendar"></i> <g:message
									code="project.grantTo.label" default="Grant To" /></label>
							<div class="controls">
								<input name="grantTo" type="text" id="grantTo"
									class="date input-xlarge"
									value="${projectInstance?.grantTo?.format('dd/MM/yyyy')}"
									placeholder="Select date" />
							</div>
						</div>


						<div
							class="control-group ${hasErrors(bean: projectInstance, field: 'grantedAmount', 'error')}">

							<label class="control-label" for="grantedAmount"><g:message
									code="project.grantedAmount.label" default="Granted Amount" /></label>
							<div class="controls">
								<div class="input-prepend input-append">
									<span class="add-on">$</span>

									<g:textField name="grantedAmount" placeholder="Enter amount" class="input-xlarge" style="width:214px;"
										value="${projectInstance?.grantedAmount?projectInstance.grantedAmount:null}" />
									<span class="add-on">.00</span>

								</div>
							</div>

						</div>
					</div>

			</div>

			<div class="super-section">
				<h3>Project Proposal</h3>

				<div id="projectProposalSec">


					<div
						class="control-group ${hasErrors(bean: projectInstance, field: 'projectProposal', 'error')}">

						<label class="control-label" for="projectProposal"><g:message
								code="project.projectProposal.label" default="Project Proposal" /></label>

						<div class="controls" style="max-width: 100%;">

                                                    <textarea id="projectProposal" name="projectProposal" class="input-block-level" style="height:200px;" placeholder="Write a small description about the project proposal.">${projectInstance?.projectProposal}</textarea>

                                                    <script type='text/javascript'>
                                                        var config = { extraPlugins: 'confighelper', toolbar:'EditorToolbar', toolbar_EditorToolbar:[[ 'Bold', 'Italic']]};
CKEDITOR.replace('projectProposal', config);
</script>

						</div>
					</div>


					<div
						class="control-group ${hasErrors(bean: projectInstance, field: 'proposalFiles', 'error')}">

						<label class="control-label" for="proposalFiles"><g:message
								code="project.proposalFiles.label"
								default="Project Proposal Files" /></label>

						<div class="controls">

							<fileManager:uploader
								model="['name':'proposalFiles', 'docs':projectInstance?.fetchProposalFiles(), 'sourceHolder': projectInstance, 'fileParams':['uploadDir':uploadDir, 'type':DocumentType.Proposal.value]]" />
						</div>
					</div>
				</div>

			</div>

			<div class="super-section">

				<h3>Project Report</h3>
				<div id="projectReportSec" class="section">
					<div
						class="control-group ${hasErrors(bean: projectInstance, field: 'projectReport', 'error')}">

						<label class="control-label" for="projectReport"><g:message
								code="project.projectReport.label" default="Project Report" /></label>
						<div class="controls" style="max-width: 100%;">

                                                    <textarea id="projectReport" name="projectReport" class="input-block-level" style="height:200px;" placeholder="Write a small description about the project report.">${projectInstance?.projectReport}</textarea>

                                                    <script type='text/javascript'>
                                                        var config = { extraPlugins: 'confighelper', toolbar:'EditorToolbar', toolbar_EditorToolbar:[[ 'Bold', 'Italic']]};
CKEDITOR.replace('projectReport', config);
</script>
						</div>

					</div>

					<div
						class="control-group ${hasErrors(bean: projectInstance, field: 'reportFiles', 'error')}">

						<label class="control-label" for="reportFiles"><g:message
								code="project.reportFiles.label" default="Project Report Files" /></label>
						<div class=" controls file-upload">
							<%
											def canUploadFile = true //customsecurity.hasPermissionAsPerGroups([permission:org.springframework.security.acls.domain.BasePermission.WRITE]).toBoolean()
									%>

							<fileManager:uploader
								model="['name':'reportFiles', 'docs':projectInstance?.fetchReportFiles(), 'sourceHolder': projectInstance, 'fileParams':['uploadDir':uploadDir, 'type':DocumentType.Report.value]]" />
						</div>
					</div>
				</div>
			</div>


			<div class="super-section">
				<h3>Data Contribution</h3>
				<div id="data-links">
					<g:render template="dataLinks"
						model="['projectInstance':projectInstance]" />

				</div>

			</div>


			<div class="super-section">
				<h3>Miscellaneous</h3>
				<div id="miscSec" class="section in collapse">
					<div
						class="control-group ${hasErrors(bean: projectInstance, field: 'misc', 'error')}">
						<label class="control-label" for="misc"><g:message
								code="project.misc.label" default="Miscellaneous" /></label>
						<div class="controls">
                                                    <textarea id="misc" name="misc" class="input-block-level" style="height:200px;" placeholder="Any other miscellaneous details regarding the project can be give here.">${projectInstance?.misc}</textarea>

                                                    <script type='text/javascript'>
CKEDITOR.replace('misc', config);
</script>

						</div>
					</div>

					<div
						class="control-group ${hasErrors(bean: projectInstance, field: 'miscFiles', 'error')}">

						<label class="control-label" for=miscFiles"><g:message
								code="project.miscFiles.label" default="Miscellaneous Files" /></label>
						<div class="controls file-upload">


							<fileManager:uploader
								model="['name':'miscFiles', 'docs':projectInstance?.fetchMiscFiles(), 'sourceHolder': projectInstance, 'fileParams':['uploadDir':uploadDir, 'type':DocumentType.Miscellaneous.value]]" />
						</div>


					</div>
				</div>
			</div>
	</div>
	</div>




	<div class="span12" style="margin-top: 20px; margin-bottom: 40px;">

		<g:if test="${projectInstance?.id}">
			<a
				href="${uGroup.createLink(controller:'project', action:'show', id:projectInstance.id)}"
				class="btn btn-danger" style="float: right; margin-right: 30px;">
				Cancel </a>
		</g:if>
		<g:else>
			<a href="${uGroup.createLink(controller:'project', action:'list')}"
				class="btn btn-danger" style="float: right; margin-right: 30px;">
				Cancel </a>
		</g:else>

		<button type="submit" class="btn btn-primary" id="projectFormSubmit"
			style="float: right; margin-right: 5px;">
			${form_button_name}
		</button>
	</div>
	</form>


	</div>
	<g:render template='location' model="['i':'_clone','hidden':true]" />
	<g:render template='dataLink' model="['i':'_clone','hidden':true]" />



	<r:script>
	
				
		
        $(document).ready(function(){ 
             $("#tags").tagit({
        	select:true, 
        	allowSpaces:true, 
        	placeholderText:'Add some tags',
        	fieldName: 'tags', 
        	autocomplete:{
                    source: '/project/tags',
                    appendTo: "#tags_ac"
        	}, 
        	triggerKeys:['enter', 'comma', 'tab'], 
        	maxLength:30
            });
            $(".tagit-hiddenSelect").css('display','none');
            $( ".date" ).datepicker({ 
                changeMonth: true,
                changeYear: true,
                dateFormat: 'dd/mm/yy' 
            });


		
        });
        

        </r:script>


</body>
</html>
