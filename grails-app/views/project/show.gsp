
<%@ page import="content.Project"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'project.label', default: 'Project')}" />
<title><g:message code="default.show.label" args="[entityName]" /></title>
<r:require modules="content_view" />

<style type="text/css">
.tag {
	background-color: #E0EAF1;
	border-bottom: 1px solid #b3cee1;
	border-right: 1px solid #b3cee1;
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

.textarea-value {
	background-color: whitesmoke;
	margin: 10px;
	padding-left: 10px;
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


		<g:if test="${projectInstance?.direction}">
			<div id="strategic-direction" class="speciesField collapse in"
				style="border: 1px solid #735005; padding: 10px; margin-top: 20px; margin-bottom: 20px; border-radius: 5px; background-color: #fff1a8; color: #735005; margin-left: 10px;">

				<h3 style="font-size: 16px; border-bottom: 1px solid #735005;">Strategic
					Direction</h3>
				${projectInstance?.direction?.title.encodeAsHTML()}
				-
				${projectInstance?.direction?.strategy.encodeAsHTML()}

			</div>
		</g:if>

		<g:if test="${projectInstance?.summary}">

			<div>
				<h4>Summary</h4>
				<p>
					${projectInstance?.summary}
				</p>
			</div>
		</g:if>

		<g:if test="${projectInstance.tags}">
			<b>Keywords : </b>

			<g:render template="/project/showTagsList"
				model="['instance': projectInstance, 'controller': 'project', 'action':'list']" />

		</g:if>

		<g:if test="${projectInstance.locations.size()}">
			<div class="sidebar_section">
				<a href="speciesFieldHeader" data-toggle="collapse"
					href="#locations"><h5>Project Sites</h5></a>
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
		</g:if>
		<div class="sidebar_section">
			<a class="speciesFieldHeader" data-toggle="collapse"
				href="#grantee-details"><h5>Grantee Details</h5></a>
			<div id="grantee-details" class="speciesField collapse in">


				<g:if test="${projectInstance?.granteeLogo}">
					<fileManager:displayFile
						filePath="${ projectInstance?.granteeLogo}"
						fileName="${projectInstance?.granteeOrganization}"></fileManager:displayFile>

				</g:if>
				<table>
					<tr>
						<td class="prop"><span class="grid_3 name">Organization</td>
						<td class="linktext">
							${projectInstance?.granteeOrganization}
						</td>
					</tr>
				</table>
			</div>
		</div>


		<div class="sidebar_section">
			<a data-toggle="collapse" href="#project-details"><h5>Project
					Details</h5></a>
			<div id="project-details" class="speciesField in collapse">
				<table>
					<tr>
						<td class="prop"><span class="grid_3 name">Grant Term</span></td>
						<td class="linktext">
							${projectInstance?.grantFrom} - ${projectInstance?.grantTo}
						</td>
					</tr>

					<tr>
						<td class="prop"><span class="grid_3 name">Amount</td>
						<td class="linktext">$ ${projectInstance?.grantedAmount}
						</td>
					</tr>
				</table>
			</div>
		</div>


		<g:if
			test="${projectInstance?.projectProposal || projectInstance?.proposalFiles}">
			<div class="sidebar_section">
				<a data-toggle="collapse" href="#proposal"><h5>Project
						Proposal</h5></a>
				<div id="proposal" class="speciesField collapse in">

					<g:if test="${projectInstance?.projectProposal}">
						<div class="textarea-value">
							${projectInstance?.projectProposal}
						</div>
					</g:if>

					<g:if test="${projectInstance?.proposalFiles}">
						<b>Files</b>
						<g:each in="${projectInstance?.proposalFiles}" var="proposalFile">
							<g:render template="/document/showDocument"
								model="['documentInstance':proposalFile]" />
						</g:each>
					</g:if>
				</div>
			</div>
		</g:if>


		<g:if
			test="${projectInstance?.projectReport || projectInstance?.reportFiles}">

			<div class="sidebar_section">
				<a data-toggle="collapse" href="#report"><h5>Project Report</h5></a>
				<div id="report" class="speciesField collapse in">

					<g:if test="${projectInstance?.projectReport}">
						<div class="textarea-value">
							${projectInstance?.projectReport}
						</div>
					</g:if>

					<g:if test="${projectInstance?.reportFiles}">
						<b>Files</b>

						<g:each in="${projectInstance?.reportFiles}" var="reportFile">
							<g:render template="/document/showDocument"
								model="['documentInstance':reportFile]" />
						</g:each>
					</g:if>
				</div>
			</div>
		</g:if>


		<g:if test="${projectInstance?.dataLinks}">

			<div class="sidebar_section">
				<a data-toggle="collapse" href="#data-links"><h5>Data
						Contribution Links</h5></a>

				<div id="data-links" class="speciesField collapse in">

					<g:each in="${projectInstance?.dataLinks}" var="dataLink">
						<dl class="dl-horizontal">
							<dt>Description</dt>
							<dd>
								${dataLink.description}
							</dd>
							<dt>URL</dt>
							<dd>
								${dataLink.url}
							</dd>
						</dl>
					</g:each>
				</div>
			</div>
		</g:if>




		<g:if test="${projectInstance?.misc || projectInstance?.miscFiles}">

			<div class="sidebar_section">
				<a data-toggle="collapse" href="#misc"><h5>Miscellaneous</h5></a>
				<div id="misc" class="speciesField collapse in">
					<g:if test="${projectInstance?.misc}">
						<div class="textarea-value">
							${projectInstance?.misc}
						</div>
					</g:if>

					<g:if test="${projectInstance?.miscFiles}">
						<b>Files</b>

						<g:each in="${projectInstance?.miscFiles}" var="miscFile">
							<g:render template="/document/showDocument"
								model="['documentInstance':miscFile]" />
						</g:each>
					</g:if>
				</div>
			</div>
		</g:if>


		<sUser:isCEPFAdmin>
			<div class="buttons" style="clear: both;">
				<form
					action="${uGroup.createLink(controller:'project', action:'edit', userGroupWebaddress:params.webaddress)}"
					method="GET">
					<g:hiddenField name="id" value="${projectInstance?.id}" />
					<g:hiddenField name="userGroup"
						value="${newsletterInstance?.userGroup?.webaddress}" />
					<span class="button"> <input class="btn btn-primary"
						style="float: right; margin-right: 5px;" type="submit"
						value="Edit" />
					</span> <span class="button"> <a class="btn btn-danger"
						style="float: right; margin-right: 5px;"
						href="${uGroup.createLink(controller:'project', action:'delete', id:projectInstance?.id)}"
						onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">Delete
					</a>
					</span>


				</form>
			</div>
		</sUser:isCEPFAdmin>
	</div>



	<g:render template="/project/projectSidebar" />
</body>
</html>
