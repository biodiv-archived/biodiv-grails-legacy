
<%@ page import="content.Project"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'project.label', default: 'Project')}" />
<title><g:message code="default.show.label" args="[entityName]" /></title>
<r:require modules="core" />
</head>
<body>
	<div class="nav">
		<span class="menuButton"><a class="home"
			href="${createLink(uri: '/')}"><g:message
					code="default.home.label" /></a></span> <span class="menuButton"><g:link
				class="list" action="list">
				<g:message code="default.list.label" args="[entityName]" />
			</g:link></span> <span class="menuButton"><g:link class="create"
				action="create">
				<g:message code="default.new.label" args="[entityName]" />
			</g:link></span>
	</div>
	<div class="body">

		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>

		<div class="span12">
			<div class="page-header">
				<h1>
					${fieldValue(bean: projectInstance, field: "title")}
				</h1>

			</div>

			<div class="sidebar_section">
				<a class="speciesFieldHeader" data-toggle="collapse"
					href="#strategi-direction"><h5>Strategic Direction</h5></a>
				<div id="strategic-direction" class="speciesField collapse in">
					${projectInstance?.direction?.strategy.encodeAsHTML()}

				</div>
			</div>

			<div>
				<h4>Summary</h4>
				<p>
					${projectInstance?.summary}
				</p>
			</div>


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

			<div class="sidebar_section">
				<a class="speciesFieldHeader" data-toggle="collapse"
					href="#grantee-details"><h5>Grantee Details</h5></a>
				<div id="grantee-details" class="speciesField collapse in">

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
					<a href="speciesFieldHeader" data-toggle="collapse"
						href="#proposal"><h5>Project Proposal</h5></a>
					<div id="proposal" class="speciesField collapse in">
						${projectInstance?.projectProposal}
					</div>
				</div>
			</g:if>

			<g:if test="${projectInstance?.projectReport}">

				<div class="sidebar_section">
					<a href="speciesFieldHeader" data-toggle="collapse" href="#report"><h5>Project
							Report</h5></a>
					<div id="report" class="speciesField collapse in">
						${projectInstance?.projectReport}
					</div>
				</div>
			</g:if>



			<g:if test="${projectInstance?.misc}">

				<div class="sidebar_section">
					<a href="speciesFieldHeader" data-toggle="collapse" href="#misc"><h5>Miscellaneous</h5></a>
					<div id="misc" class="speciesField collapse in">
						${projectInstance?.misc}
					</div>
				</div>
			</g:if>


		</div>
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
</body>
</html>
