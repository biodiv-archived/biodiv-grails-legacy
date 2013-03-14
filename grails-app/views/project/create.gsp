

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
            %>
		<form action="${form_action}" method="POST" id="create-project"
			enctype="multipart/form-data">
			<div class="dialog">
				<table>
					<tbody>

						<tr class="prop">
							<td valign="top" class="name"><label for="title"><g:message
										code="project.title.label" default="Title" /></label></td>
							<td valign="top"
								class="value ${hasErrors(bean: projectInstance, field: 'title', 'errors')}">
								<g:textField name="title" value="${projectInstance?.title}" />
							</td>
						</tr>
						
							<tr class="prop">
							<td valign="top" class="name"><label for="summary"><g:message
										code="project.summary.label"
										default="Project Summary" /></label></td>
							<td valign="top"
								class="value ${hasErrors(bean: projectInstance, field: 'summary', 'errors')}">
								<g:textArea name="summary"
									value="${projectInstance?.summary}" rows="5" cols="40" />
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="direction"><g:message
										code="project.direction.label" default="Direction" /></label></td>
							<td valign="top"
								class="value ${hasErrors(bean: projectInstance, field: 'direction', 'errors')}">
								<g:select name="direction.id"
									from="${content.StrategicDirection.list()}" optionKey="id"
									value="${projectInstance?.direction?.id}"
									noSelection="['null': '']" />
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="granteeName"><g:message
										code="project.granteeName.label" default="Grantee Name" /></label></td>
							<td valign="top"
								class="value ${hasErrors(bean: projectInstance, field: 'granteeName', 'errors')}">
								<g:textField name="granteeName"
									value="${projectInstance?.granteeName}" />
							</td>
						</tr>


						<tr class="prop">
							<td valign="top" class="name"><label for="granteeURL"><g:message
										code="project.granteeURL.label" default="Grantee URL" /></label></td>
							<td valign="top"
								class="value ${hasErrors(bean: projectInstance, field: 'granteeURL', 'errors')}">
								<g:textField name="granteeURL"
									value="${projectInstance?.granteeURL}" />
							</td>
						</tr>


						<tr>
							<td valign="top" class="name"><label for="granteeLogo"><g:message
										code="project.granteeLogo.label" default="GranteeLogo" /></label></td>
							<td valign="top"
								class="value ${hasErrors(bean: projectInstance, field: 'granteeLogo', 'errors')}">
								<input type="file" name="granteeLogo" />
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="grantFrom"><g:message
										code="project.grantFrom.label" default="Grant From" /></label></td>
							<td valign="top"
								class="value ${hasErrors(bean: projectInstance, field: 'grantFrom', 'errors')}">
								<input name="grantFrom" type="text" id="grantFrom" class="date-popup"
									value="${projectInstance?.grantFrom?.format('dd/MM/yyyy')}"
									placeholder="Select date" />
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="grantTo"><g:message
										code="project.grantTo.label" default="Grant To" /></label></td>
							<td valign="top"
								class="value ${hasErrors(bean: projectInstance, field: 'grantTo', 'errors')}">
									<input name="grantTo" type="text" id="grantTo" class="date-popup"
									value="${projectInstance?.grantTo?.format('dd/MM/yyyy')}"
									placeholder="Select date" />
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="grantedAmount"><g:message
										code="project.grantedAmount.label" default="Granted Amount" /></label>
							</td>
							<td valign="top"
								class="value ${hasErrors(bean: projectInstance, field: 'grantedAmount', 'errors')}">
								<g:textField name="grantedAmount"
									value="${fieldValue(bean: projectInstance, field: 'grantedAmount')}" />
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="projectProposal"><g:message
										code="project.projectProposal.label"
										default="Project Proposal" /></label></td>
							<td valign="top"
								class="value ${hasErrors(bean: projectInstance, field: 'projectProposal', 'errors')}">
								<g:textArea name="projectProposal"
									value="${projectInstance?.projectProposal}" rows="5" cols="40" />
							</td>
						</tr>
						
						<tr>
							<td valign="top" class="name"><label for="proposalFiles"><g:message
										code="project.proposalFiles.label"
										default="Project Proposal Files" /></label></td>
							<td valign="top"
								class="value ${hasErrors(bean: projectInstance, field: 'proposalFiles', 'errors')}">
								<fileManager:uploader model="['name':'proposalFiles']"/>
								
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="projectReport"><g:message
										code="project.projectReport.label" default="Project Report" /></label>
							</td>
							<td valign="top"
								class="value ${hasErrors(bean: projectInstance, field: 'projectReport', 'errors')}">
								<g:textArea name="projectReport"
									value="${projectInstance?.projectReport}" rows="5" cols="40" />
							</td>
						</tr>

						<tr>
							<td valign="top" class="name"><label for="reportFiles"><g:message
										code="project.reportFiles.label"
										default="Project Report Files" /></label></td>
							<td valign="top"
								class="value ${hasErrors(bean: projectInstance, field: 'reportFiles', 'errors')}">
								<div class="file-upload">
									<%
											def canUploadFile = true //customsecurity.hasPermissionAsPerGroups([permission:org.springframework.security.acls.domain.BasePermission.WRITE]).toBoolean()
									%>
									<fileManager:uploader model="['name':'reportFiles']"/>
									

							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label
								for="dataContributionIntensity"><g:message
										code="project.dataContributionIntensity.label"
										default="Data Contribution Intensity" /></label></td>
							<td valign="top"
								class="value ${hasErrors(bean: projectInstance, field: 'dataContributionIntensity', 'errors')}">
								<g:textArea name="dataContributionIntensity"
									value="${projectInstance?.dataContributionIntensity}" rows="5"
									cols="40" />
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="analysis"><g:message
										code="project.analysis.label" default="Analysis" /></label></td>
							<td valign="top"
								class="value ${hasErrors(bean: projectInstance, field: 'analysis', 'errors')}">
								<g:textArea name="analysis" value="${projectInstance?.analysis}"
									rows="5" cols="40" />
							</td>
						</tr>

						<tr>
							<td valign="top" class="name"><label for=analysisFiles"><g:message
										code="project.analysisFiles.label" default="Analysis Files" /></label>
							</td>
							<td valign="top"
								class="value ${hasErrors(bean: projectInstance, field: 'analysisFiles', 'errors')}">
								
								        <fileManager:uploader model="['name':'analysisFiles']"/>
								
								
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="misc"><g:message
										code="project.misc.label" default="Misc" /></label></td>
							<td valign="top"
								class="value ${hasErrors(bean: projectInstance, field: 'misc', 'errors')}">
								<g:textArea name="misc" value="${projectInstance?.misc}"
									rows="5" cols="40" />
							</td>
						</tr>
						
						<tr class="prop">
							<td valign="top" class="name"><label for="tags"><g:message
										code="project.tags.label" default="Project Tags" /></label></td>
							<td valign="top"
								class="value ${hasErrors(bean: projectInstance, field: 'tags', 'errors')}">
								<ul id="tags" name="tags">
								<g:each in="${projectInstance.tags}" var="tag">
								<li>
									${tag}
								</li>
							</g:each>
						</ul>
							</td>
						</tr>

					</tbody>
				</table>
			
			</div>
			<div class="buttons">
				<span class="button"><g:submitButton name="create"
						class="save"
						value="${message(code: 'default.button.create.label', default: 'Create')}" /></span>
			</div>
		</form>
		
	</div>

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
