

<%@ page import="content.Project"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'project.label', default: 'Project')}" />
<title><g:message code="default.create.label"
		args="[entityName]" /></title>
<r:require modules="add_file" />
<uploader:head />
</head>
<body>
	<div class="nav">
		<span class="menuButton"><a class="home"
			href="${createLink(uri: '/')}"><g:message
					code="default.home.label" /></a></span> <span class="menuButton"><g:link
				class="list" action="list">
				<g:message code="default.list.label" args="[entityName]" />
			</g:link></span>
	</div>
	<div class="body">
		<h1>
			<g:message code="default.create.label" args="[entityName]" />
		</h1>
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${projectInstance}">
			<div class="errors">
				<g:renderErrors bean="${projectInstance}" as="list" />
			</div>
		</g:hasErrors>
		<%
                def form_action = uGroup.createLink(action:'save', controller:'project', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
				def form_button_name = "Create Project"
				def form_button_val = "Create Project"
				if(params.action == 'edit' || params.action == 'update'){
					form_action = uGroup.createLink(action:'update', controller:'project', id:projectInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
					 form_button_name = "Update Project"
					form_button_val = "Update Project"
				}
			
            %>
		<form action="${form_action}" method="POST" id="create-project"
			class="project-form form-horizontal" enctype="multipart/form-data">
			<div class="dialog">


				<div class="super-section">
					<div class="section">

						<div
							class="control-group ${hasErrors(bean: projectInstance, field: 'direction', 'error')}">
							<label class="control-label" for="direction"><g:message
									code="project.direction.label" default="Direction" /></label>

							<div class="controls">
								<g:select name="direction.id"
									from="${content.StrategicDirection.list()}" optionKey="id"
									value="${projectInstance?.direction?.id}"
									noSelection="['null': '']" />
							</div>
						</div>

						<div
							class="control-group ${hasErrors(bean: projectInstance, field: 'title', 'error')}">
							<label class="control-label" for="title"><g:message
									code="project.title.label" default="Title" /></label>
							<div class="controls">

								<g:textField class="input-xxlarge" name="title"
									value="${projectInstance?.title}" />
							</div>

						</div>

						<div
							class="row control-group ${hasErrors(bean: projectInstance, field: 'summary', 'error')}">

							<label class="control-label" for="summary"><g:message
									code="project.summary.label" default="Project Summary" /></label>

							<div class="controls">


								<ckeditor:config var="toolbar_editorToolbar">
									[
    									[ 'Bold', 'Italic' ]
									]
									</ckeditor:config>
								<ckeditor:editor name="summary" height="200px"
									toolbar="editorToolbar">
									${projectInstance?.summary}
								</ckeditor:editor>
							</div>
						</div>

						<div
							class="control-group ${hasErrors(bean: projectInstance, field: 'tags', 'error')}">

							<label class="control-label" for="tags"><g:message
									code="project.tags.label" default="Project Tags" /></label>

							<div class="controls">

								<ul id="tags" name="tags">
									<g:each in="${projectInstance.tags}" var="tag">
										<li>
											${tag}
										</li>
									</g:each>
								</ul>
							</div>
						</div>
					</div>
				</div>

				<div class="super-section">
					<a data-toggle="collapse" href="#locationsDiv">
						<h5>Location</h5>
					</a>
					<div id="locationsDiv" class="section in collapse">
						<g:render template="locations"
							model="['projectInstance':projectInstance]" />

					</div>

				</div>

				<div class="super-section">

					<a data-toggle="collapse" href="#granteeDetails">
						<h5>Grantee Details</h5>
					</a>
					<div id="granteeDetails" class="section in collapse">
						<div>
							<div
								class="control-group ${hasErrors(bean: projectInstance, field: 'granteeName', 'error')}">
								<label class="control-label" for="granteeName"><g:message
										code="project.granteeName.label" default="Grantee Name" /></label>

								<div class="controls">

									<g:textField name="granteeName"
										value="${projectInstance?.granteeName}" />
								</div>
							</div>


							<div
								class="control-group ${hasErrors(bean: projectInstance, field: 'granteeURL', 'error')}">
								<label class="control-label" for="granteeURL"><g:message
										code="project.granteeURL.label" default="Grantee URL" /></label>

								<div class="controls">

									<g:textField name="granteeURL"
										value="${projectInstance?.granteeURL}" />
								</div>
							</div>


							<div
								class="control-group ${hasErrors(bean: projectInstance, field: 'granteeLogo', 'error')}">

								<label class="control-label" for="granteeLogo"><g:message
										code="project.granteeLogo.label" default="GranteeLogo" /></label> <input
									type="file" name="granteeLogo" />
							</div>
						</div>
						<div>
							<div
								class="control-group ${hasErrors(bean: projectInstance, field: 'granteeFrom', 'error')}">

								<label class="control-label" for="grantFrom"><g:message
										code="project.grantFrom.label" default="Grant From" /></label>
								<div class="controls">

									<input name="grantFrom" type="text" id="grantFrom"
										class="date-popup"
										value="${projectInstance?.grantFrom?.format('dd/MM/yyyy')}"
										placeholder="Select date" />
								</div>
							</div>

							<div
								class="control-group ${hasErrors(bean: projectInstance, field: 'granteeTo', 'error')}">
								<label class="control-label" for="grantTo"><g:message
										code="project.grantTo.label" default="Grant To" /></label>
								<div class="controls">

									<input name="grantTo" type="text" id="grantTo"
										class="date-popup"
										value="${projectInstance?.grantTo?.format('dd/MM/yyyy')}"
										placeholder="Select date" />
								</div>
							</div>


							<div
								class="control-group ${hasErrors(bean: projectInstance, field: 'granteeAmount', 'error')}">

								<label class="control-label" for="grantedAmount"><g:message
										code="project.grantedAmount.label" default="Granted Amount" /></label>
								<div class="controls">

									<g:textField name="grantedAmount"
										value="${fieldValue(bean: projectInstance, field: 'grantedAmount')}" />
								</div>

							</div>
						</div>

					</div>
				</div>
				<div class="super-section">
					<div class="section">

						<div
							class="control-group ${hasErrors(bean: projectInstance, field: 'projectProposal', 'error')}">

							<label class="control-label" for="projectProposal"><g:message
									code="project.projectProposal.label" default="Project Proposal" /></label>

							<div class="controls">

								<g:textArea name="projectProposal"
									value="${projectInstance?.projectProposal}" rows="5" cols="40" />
							</div>
						</div>


						<div
							class="control-group ${hasErrors(bean: projectInstance, field: 'proposalFiles', 'error')}">

							<label class="control-label" for="proposalFiles"><g:message
									code="project.proposalFiles.label"
									default="Project Proposal Files" /></label>

							<div class="controls">

								<fileManager:uploader model="['name':'proposalFiles']" />
							</div>
						</div>


						<div
							class="control-group ${hasErrors(bean: projectInstance, field: 'projectReport', 'error')}">

							<label class="control-label" for="projectReport"><g:message
									code="project.projectReport.label" default="Project Report" /></label>
							<div class="controls">

								<g:textArea name="projectReport"
									value="${projectInstance?.projectReport}" rows="5" cols="40" />
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

								<fileManager:uploader model="['name':'reportFiles']" />
							</div>


							<div
								class="control-group ${hasErrors(bean: projectInstance, field: 'dataContributionIntensity', 'error')}">

								<label class="control-label" for="dataContributionIntensity"><g:message
										code="project.dataContributionIntensity.label"
										default="Data Contribution Intensity" /></label>

								<div class="controls">

									<g:textArea name="dataContributionIntensity"
										value="${projectInstance?.dataContributionIntensity}" rows="5"
										cols="40" />
								</div>
							</div>


							<div
								class="control-group ${hasErrors(bean: projectInstance, field: 'analysis', 'error')}">
								<label class="control-label" for=analysis"><g:message
										code="project.analysis.label" default="Analysis" /> </label>

								<div class="controls">
									<g:textArea name="analysis"
										value="${projectInstance?.analysis}" rows="5" cols="40" />
								</div>
							</div>

							<div
								class="control-group ${hasErrors(bean: projectInstance, field: 'analysisFiles', 'error')}">

								<label class="control-label" for=analysisFiles"><g:message
										code="project.analysisFiles.label" default="Analysis Files" /></label>
								<div class="controls file-upload">


									<fileManager:uploader model="['name':'analysisFiles']" />
								</div>


							</div>

							<div
								class="control-group ${hasErrors(bean: projectInstance, field: 'misc', 'error')}">
								<label class="control-label" for="misc"><g:message
										code="project.misc.label" default="Misc" /></label>
								<div class="controls">


									<g:textArea name="misc" value="${projectInstance?.misc}"
										rows="5" cols="40" />
								</div>
							</div>

						</div>


					</div>

				</div>

				<div class="form-actions">
					<button type="submit" class="btn btn-primary">${form_button_name}</button>
					<button class="btn">Cancel</button>
				</div>
		</form>


	</div>
	<g:render template='location' model="['i':'_clone','hidden':true]" />


	<r:script>
        $(document).ready(function(){ 
             $("#tags").tagit({
        	select:true, 
        	allowSpaces:true, 
        	placeholderText:'Add some tags',
        	fieldName: 'tags', 
        	autocomplete:{
        		source: '/project/tags'
        	}, 
        	triggerKeys:['enter', 'comma', 'tab'], 
        	maxLength:30
        });
		$(".tagit-hiddenSelect").css('display','none');
		

		
        });
        
        $( ".date-popup" ).datepicker({ 
			changeMonth: true,
			changeYear: true,
			dateFormat: 'dd/mm/yy' 
	});


        </r:script>


</body>
</html>
