
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

	<div class="span12">

		<div class="page-header clearfix" style="margin-bottom:0px;">
			<div style="width: 100%;">
				<div class="main_heading span8" style="margin-left: 0px;">

					<s:showHeadingAndSubHeading
						model="['heading':projectInstance.title, 'subHeading':subHeading, 'headingClass':headingClass, 'subHeadingClass':subHeadingClass]" />
				</div>
				<sUser:isCEPFAdmin>

					<a class="btn btn-success pull-right"
						href="${uGroup.createLink(
						controller:'project', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"
						style="margin-left: 8px;"> <i class="icon-plus"></i>Add CEPF
						Project
					</a>

					<div style="clear:both; float: right; margin: 10px 0;">

						<a class="btn btn-primary pull-right"
							href="${uGroup.createLink(controller:'project', action:'edit', id:projectInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
							<i class="icon-edit"></i>Edit
						</a> 
						
						<a class="btn btn-danger"  href="#" style="margin-right: 5px; margin-bottom: 10px;"
							onclick="deleteProject(); return false;">
							<i class="icon-trash"></i>Delete
						</a>
							
						<form
							action="${uGroup.createLink(controller:'project', action:'delete')}"
							method='POST' name='deleteForm'>
							<input type="hidden" name="id" value="${projectInstance.id}" />
						</form>
						
						<r:script>
						function deleteProject(){
							if(confirm('This project will be deleted. Are you sure ?')){
								document.forms.deleteForm.submit();
							}
						}
						</r:script>


					</div>


				</sUser:isCEPFAdmin>
			</div>
		</div>



		<uGroup:rightSidebar />
		<div class="span8 right-shadow-box observation"
			style="margin: 0px;">



			<% 
	def curr_id = projectInstance.id
	def prevProjectId =  Project.countByIdLessThan(curr_id)>0?Project.findAllByIdLessThan(curr_id)?.last()?.id:''
	def nextProjectId = Project.countByIdGreaterThan(curr_id)>0?Project.findByIdGreaterThan(curr_id)?.id:''
	
	 %>
			<div class="nav" style="width: 100%;">

				<a class="pull-left btn ${prevProjectId?:'disabled'}"
					href="${uGroup.createLink([action:"show", controller:"project",
                                        id:prevProjectId,  'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress])}"><i class="icon-backward"></i>Prev
					</a> <a class="pull-right  btn ${nextProjectId?:'disabled'}"
					href="${uGroup.createLink([action:"show", controller:"project",
                                        id:nextProjectId,  'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress])}">Next <i style="margin-right: 0px; margin-left: 3px;" class="icon-forward"></i>
					</a> <a class="btn"
					href="${uGroup.createLink([action:'list', controller:'project'])}"
					style="text-align: center; display: block; margin: 0 auto;">List</a>

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

                        <div class="observation_story sidebar_section">
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
                        </div>

			<g:if test="${projectInstance.locations.size()}">
				<div class="sidebar_section">
					<a class="speciesFieldHeader" data-toggle="collapse"
						href="#locations"><h5>Project Sites</h5></a>
					<div id="locations" class="speciesField collapse in">
						<table class="table table-hover" style="margin: 0px;">
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

			<g:if
				test="${projectInstance?.granteeLogo ||projectInstance?.granteeOrganization }">

				<div class="sidebar_section">
					<a class="speciesFieldHeader" data-toggle="collapse"
						href="#grantee-details"><h5>Grantee Details</h5></a>
					<div id="grantee-details" class="speciesField collapse in">


						<g:if test="${projectInstance?.granteeLogo}">
							<fileManager:displayFile
								filePath="${ projectInstance?.granteeLogo}"
								fileName="${projectInstance?.granteeOrganization}"></fileManager:displayFile>

						</g:if>

						<g:if test="${projectInstance?.granteeOrganization }">

							<table>
								<tr>
									<td class="prop"><span class=" name">Organization</td>
									<td>
										${projectInstance?.granteeOrganization}
									</td>
								</tr>
							</table>
						</g:if>
					</div>
				</div>

			</g:if>
			<div class="sidebar_section">
				<a data-toggle="collapse" href="#project-details"><h5>Project Details</h5></a>
				<div id="project-details" class="speciesField in collapse">
					<table>
						<g:if test="${projectInstance.grantFrom || projectInstance.grantTo}">
						<tr>
							<td class="prop"><span class=" name">Grant Term</span></td>
							<td>
								${projectInstance?.grantFrom} - ${projectInstance?.grantTo}
							</td>
						</tr>
						</g:if>
						<g:if test="${projectInstance.grantedAmount}">
						<tr>
							<td class="prop"><span class=" name">Amount</td>
							<td>$ ${projectInstance?.grantedAmount}
							</td>
						</tr>
						</g:if>
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
							<div class="notes_view linktext">
								${projectInstance?.projectProposal}
							</div>
						</g:if>

						<g:if test="${projectInstance?.proposalFiles}">
							<b>Files</b>

							<g:each in="${projectInstance?.proposalFiles}" var="proposalFile">
								<g:render template="/document/showDocument"
									model="['documentInstance':proposalFile, 'showDetails':false]" />
							</g:each>
						</g:if>
					</div>
				</div>
			</g:if>


			<g:if
				test="${projectInstance?.projectReport || projectInstance?.reportFiles}">

				<div class="sidebar_section">
					<a data-toggle="collapse" href="#report"><h5>Project
							Report</h5></a>
					<div id="report" class="speciesField collapse in">

						<g:if test="${projectInstance?.projectReport}">
							<div class="notes_view linktext">
								${projectInstance?.projectReport}
							</div>
						</g:if>

						<g:if test="${projectInstance?.reportFiles}">
							<b>Files</b>

							<g:each in="${projectInstance?.reportFiles}" var="reportFile">
								<g:render template="/document/showDocument"
									model="['documentInstance':reportFile, 'showDetails':false]" />
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
								<dd class="linktext">
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
							<div class="notes_view linktext">
								${projectInstance?.misc}
							</div>
						</g:if>

						<g:if test="${projectInstance?.miscFiles}">
							<b>Files</b>

							<g:each in="${projectInstance?.miscFiles}" var="miscFile">
								<g:render template="/document/showDocument"
									model="['documentInstance':miscFile, 'showDetails':false]" />
							</g:each>
						</g:if>
					</div>
				</div>
			</g:if>


		</div>



		<g:render template="/project/projectSidebar" />
	</div>
</body>
</html>
