
<%@ page import="content.Project"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'project.label', default: 'Project')}" />
<title><g:message code="default.show.label" args="[entityName]" /></title>
<r:require modules="add_file" />

<style type="text/css">
.tag {
	background-color: #E0EAF1;
	border-bottom: 1px solid #b3cee1;
	border-right: 1px solid #b3cee1;
	padding: 3px 4px 3px 4px;
	margin: 2px 6px 2px 0;
	text-decoration: none;
	font-size: 90%;
	line-height: 2.4;
	white-space: nowrap;
}

.tags {
	margin-bottom: 10px;
	clear: both;
}
</style>
</head>
<body>



	<div class="body span8" style="padding-left: 20px;">

		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>



		<div class="page-header">
			<h1>
				${fieldValue(bean: projectInstance, field: "title")}
			</h1>

		</div>

		<% 
	def curr_id = projectInstance.id
	def prevProjectId =  Project.countByIdLessThan(curr_id)>0?Project.findAllByIdLessThan(curr_id)?.last()?.id:''
	def nextProjectId = Project.countByIdGreaterThan(curr_id)>0?Project.findByIdGreaterThan(curr_id)?.id:''
	
	 %>
		<div class="nav" style="width: 100%;">

			<a class="pull-left btn ${prevProjectId?:'disabled'}"
				href="${uGroup.createLink([action:"show", controller:"project",
									id:prevProjectId,  'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress])}">Prev
				Project</a> <a class="pull-right  btn ${nextProjectId?:'disabled'}"
				href="${uGroup.createLink([action:"show", controller:"project",
									id:nextProjectId,  'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress])}">Next
				Project</a> <a class="btn"
				href="${uGroup.createLink([action:'list', controller:'project'])}"
				style="text-align: center; display: block; width: 125px; margin: 0 auto;">List
				Projects</a>

		</div>


		<div id="strategic-direction" class="speciesField collapse in"
			style="border: 1px solid #735005; padding: 10px; margin-top: 20px; margin-bottom: 20px; border-radius: 5px; background-color: #fff1a8; color: #735005; margin-left: 10px;">

			<h3 style="font-size: 16px; border-bottom: 1px solid #735005;">Strategic
				Direction</h3>
			${projectInstance?.direction?.title.encodeAsHTML()}
			-
			${projectInstance?.direction?.strategy.encodeAsHTML()}

		</div>


		<div>
			<h4>Summary</h4>
			<p>
				${projectInstance?.summary}
			</p>
		</div>

		<g:if test="${projectInstance.tags}">

			<div class="tags">
				<b>Keywords : </b>
				<g:each in="${projectInstance.tags}" var="tag">
					<span class="tag"> ${tag}
					</span>
				</g:each>
			</div>
		</g:if>

		<div class="sidebar_section">
			<a href="speciesFieldHeader" data-toggle="collapse" href="#locations"><h5>Project
					Sites</h5></a>
			<div id="locations" class="speciesField collapse in">
				<table class="table table-hover" style="margin-left: 0px;">
					<thead>
						<tr>
							<th>Site Name</th>
							<th>Corridor</th>
						</tr>
					</thead>
					<tbody>
						<g:each in="${projectInstance.locations}" var="l">
							<tr>
								<td>
									${l.siteName}
								</td>
								<td>
									${l.corridor}
								</td>
							</tr>
						</g:each>

					</tbody>
				</table>
			</div>
		</div>

		<div class="sidebar_section">
			<a class="speciesFieldHeader" data-toggle="collapse"
				href="#grantee-details"><h5>Grantee Details</h5></a>
			<div id="grantee-details" class="speciesField collapse in">

				<fileManager:displayFile filePath="${ projectInstance?.granteeLogo}"
					fileName="${projectInstance?.granteeOrganization}"></fileManager:displayFile>

				<dl class="dl-horizontal">
					<dt>Organization</dt>
					<dd>
						${projectInstance?.granteeOrganization}
					</dd>

					<dt>Contact</dt>
					<dd>
						${projectInstance?.granteeContact}
					</dd>

					<dt>Email</dt>
					<dd>
						${projectInstance?.granteeEmail}
					</dd>
				</dl>


			</div>
		</div>


		<div class="sidebar_section">
			<a class="speciesFieldHeader" data-toggle="collapse"
				href="#project-details"><h5>Project Details</h5></a>
			<div id="project-details" class="speciesField collapse">
				<dl class="dl-horizontal">

					<dt>Grant Term</dt>
					<dd>
						${projectInstance?.grantFrom}
						-
						${projectInstance?.grantTo}
					</dd>


					<dt>Amount</dt>
					<dd>
						$
						${projectInstance?.grantedAmount}
					</dd>
			</div>
		</div>


		<g:if test="${projectInstance?.projectProposal}">
			<div class="sidebar_section">
				<a href="speciesFieldHeader" data-toggle="collapse" href="#proposal"><h5>Project
						Proposal</h5></a>
				<div id="proposal" class="speciesField collapse in">
					${projectInstance?.projectProposal}
				</div>
			</div>
		</g:if>



			<div class="sidebar_section">
				<a href="speciesFieldHeader" data-toggle="collapse" href="#report"><h5>Project
						Report</h5></a>
				<div id="report" class="speciesField collapse in">
					${projectInstance?.projectReport}
					
					<g:each in ="${projectInstance?.reportFiles}" var="reportFile">
	<p>				
<fileManager:displayIconName id="${reportFile?.id}" /></p>
</g:each>
				</div>
			</div>




		<g:if test="${projectInstance?.misc}">

			<div class="sidebar_section">
				<a href="speciesFieldHeader" data-toggle="collapse" href="#misc"><h5>Miscellaneous</h5></a>
				<div id="misc" class="speciesField collapse in">
					${projectInstance?.misc}
				</div>
			</div>
		</g:if>


		<div class="buttons">
			<g:form>
				<g:hiddenField name="id" value="${projectInstance?.id}" />
				<span class="button"><g:actionSubmit class="edit"
						action="edit"
						value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
				<span class="button"><g:actionSubmit class="delete"
						action="delete"
						value="${message(code: 'default.button.delete.label', default: 'Delete')}"
						onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
			</g:form>
		</div>
	</div>
	<g:render template="/project/projectSidebar" />

</body>
</html>
