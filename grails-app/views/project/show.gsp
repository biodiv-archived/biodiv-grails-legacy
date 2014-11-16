<%@page import="species.utils.Utils"%>
<%@ page import="content.Project"%>
<html>
<head>
<g:set var="canonicalUrl"
	value="${uGroup.createLink([controller:'project', action:'show', id:projectInstance.id, base:Utils.getIBPServerDomain()])}" />
<g:set var="title" value="${projectInstance.title}" />
<g:set var="description"
	value="${Utils.stripHTML(projectInstance.summary?:'')?:''}" />
<g:render template="/common/titleTemplate"
	model="['title':title, 'description':description, 'canonicalUrl':canonicalUrl]" />

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

		<div class="page-header clearfix" style="margin-bottom: 0px;">
			<div style="width: 100%;">
				<div class="main_heading" style="margin-left: 0px;">
					<div class="pull-right">
						<sUser:isCEPFAdmin>

							<a class="btn btn-success pull-right" title="Add CEPF Project"
								href="${uGroup.createLink(
                                controller:'project', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
								<i class="icon-plus"></i><g:message code="button.add.cepf.projects" /> 

							</a>


							<a class="btn btn-primary pull-right" title="Edit CEPF Project"
								style="margin-right: 5px;"
								href="${uGroup.createLink(controller:'project', action:'edit', id:projectInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
								<i class="icon-edit"></i><g:message code="button.edit" />

							</a>

							<a class="btn btn-danger pull-right" href="#"
								title="Delete CEPF Project" style="margin-right: 5px;"
								onclick="deleteProject(); return false;"> <i
								class="icon-trash"></i><g:message code="button.delete" />
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
						</sUser:isCEPFAdmin>

					</div>


					<s:showHeadingAndSubHeading
						model="['heading':projectInstance.title, 'subHeading':subHeading, 'headingClass':headingClass, 'subHeadingClass':subHeadingClass]" />
					<small><g:message code="text.submitted.by" /> <a
						href="${uGroup.createLink(controller:'user', action:'show', id:projectInstance.author.id, userGroupWebaddress:params.webaddress)}">
							${projectInstance.author.name}
					</a> <g:message code="text.on" /> <g:formatDate type="datetime"
							date="${projectInstance.dateCreated}" style=" MEDIUM" />
					</small>
				</div>
			</div>
			<% 
	def curr_id = projectInstance.id
	def prevProjectId =  Project.countByIdLessThan(curr_id)>0?Project.findAllByIdLessThan(curr_id, ['max':1, 'sort':'id', 'order':'desc'])?.last()?.id:''
	def nextProjectId = Project.countByIdGreaterThan(curr_id)>0?Project.findByIdGreaterThan(curr_id, ['max':1, 'sort':'id', 'order':'asc'])?.id:''
	
	 %>
			<div class="nav" style="width: 100%; margin-top: 10px;">

				<a class="pull-left btn ${prevProjectId?:'disabled'}"
					href="${uGroup.createLink([action:"show", controller:"project",
                                        id:prevProjectId,  'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress])}"><i
					class="icon-backward"></i><g:message code="button.prev" /> </a> <a
					class="pull-right  btn ${nextProjectId?:'disabled'}"
					href="${uGroup.createLink([action:"show", controller:"project",
                                        id:nextProjectId,  'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress])}"><g:message code="button.next" />
					<i style="margin-right: 0px; margin-left: 3px;"
					class="icon-forward"></i>
				</a> <a class="btn"
					href="${uGroup.createLink([action:'list', controller:'project'])}"
					style="text-align: center; display: block; margin: 0 auto;"><g:message code="default.list.label" /></a>

			</div>



		</div>



		<uGroup:rightSidebar />
		<div class="span8 right-shadow-box observation" style="margin: 0px;">



			<g:if test="${projectInstance?.direction}">
				<div id="strategic-direction" class="speciesField collapse in"
					style="border: 1px solid #735005; padding: 10px; margin-top: 20px; margin-bottom: 20px; border-radius: 5px; background-color: #fff1a8; color: #735005; margin-left: 10px;">

					<h3 style="font-size: 16px; border-bottom: 1px solid #735005;"><g:message code="project.show.strategic.direction" /></h3>
					${projectInstance?.direction?.title.encodeAsHTML()}
					-
					${projectInstance?.direction?.strategy.encodeAsHTML()}

				</div>
			</g:if>

			<g:if test="${projectInstance?.summary || projectInstance.tags}">
				<div class="observation_story sidebar_section">
					<g:if test="${projectInstance?.summary}">

						<div>
							<h4><g:message code="project.show.Summary" /></h4>
							<p>
								${raw(projectInstance?.summary)}
							</p>
						</div>
					</g:if>
					<g:if test="${projectInstance.tags}">
						<b><g:message code="default.keywords.label" /> : </b>

						<g:render template="/project/showTagsList"
							model="['instance': projectInstance, 'controller': 'project', 'action':'list']" />

					</g:if>
				</div>
			</g:if>

			<g:if test="${projectInstance.locations.size()}">
				<div class="sidebar_section">
					<a class="speciesFieldHeader" data-toggle="collapse"
						href="#locations"><h5><g:message code="link.project.sites" /></h5></a>
					<div id="locations" class="speciesField collapse in">
						<table class="table table-hover" style="margin: 0px;">
							<thead>
								<tr>
									<th><g:message code="default.site.name.label" /> </th>
									<th><g:message code="default.corridor.label" /></th>
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
						href="#grantee-details"><h5><g:message code="heading.grantee.details" /> </h5></a>
					<div id="grantee-details" class="speciesField collapse in">


						<g:if test="${projectInstance?.granteeLogo}">
							<fileManager:displayFile
								filePath="${ projectInstance?.granteeLogo}"
								fileName="${projectInstance?.granteeOrganization}"></fileManager:displayFile>

						</g:if>

						<g:if test="${projectInstance?.granteeOrganization }">

							<table>
								<tr>
									<td class="prop"><span class=" name"><g:message code="default.organization.label" /></td>
									<td>
										${raw(projectInstance?.granteeOrganization)}
									</td>
								</tr>
							</table>
						</g:if>
					</div>
				</div>

			</g:if>
			<div class="sidebar_section">
				<a data-toggle="collapse" href="#project-details"><h5><g:message code="heading.project.details" /></h5></a>
				<div id="project-details" class="speciesField in collapse">
					<table>
						<g:if
							test="${projectInstance.grantFrom || projectInstance.grantTo}">
							<tr>
								<td class="prop"><span class=" name"><g:message code="default.grant.term.label" />&nbsp;</span></td>
								<td>
									${projectInstance?.grantFrom?.format('dd/MM/yyyy')} - ${projectInstance?.grantTo?.format('dd/MM/yyyy')}
								</td>
							</tr>
						</g:if>
						<g:if test="${projectInstance.grantedAmount}">
							<tr>
								<td class="prop"><span class=" name"><g:message code="default.amount.label" /></td>
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
					<a data-toggle="collapse" href="#proposal"><h5><g:message code="heading.project.proposal" /></h5></a>
					<div id="proposal" class="speciesField collapse in">

						<g:if test="${projectInstance?.projectProposal}">
							<div class="notes_view linktext">
								${raw(projectInstance?.projectProposal)}
							</div>
						</g:if>

						<g:if test="${projectInstance?.proposalFiles}">
							<b><g:message code="project.show.files" /></b>

							<g:each in="${projectInstance?.fetchProposalFiles()}" var="proposalFile">

                                                            <g:render template="/document/showDocumentStoryTemplate" model="['documentInstance':proposalFile, showDetails:false]"/>
							</g:each>
						</g:if>
					</div>
				</div>
			</g:if>


			<g:if
				test="${projectInstance?.projectReport || projectInstance?.reportFiles}">

				<div class="sidebar_section">
					<a data-toggle="collapse" href="#report"><h5><g:message code="heading.project.report" />
							</h5></a>
					<div id="report" class="speciesField collapse in">

						<g:if test="${projectInstance?.projectReport}">
							<div class="notes_view linktext">
								${raw(projectInstance?.projectReport)}
							</div>
						</g:if>

						<g:if test="${projectInstance?.reportFiles}">
							<b><g:message code="project.show.files" /></b>

							<g:each in="${projectInstance?.fetchReportFiles()}" var="reportFile">

                                                            <g:render template="/document/showDocumentStoryTemplate" model="['documentInstance':reportFile, showDetails:false]"/>
							</g:each>
						</g:if>
					</div>
				</div>
			</g:if>


			<g:if test="${projectInstance?.dataLinks}">

				<div class="sidebar_section">
					<a data-toggle="collapse" href="#data-links"><h5><g:message code="link.data.contribution.links" /></h5></a>

					<div id="data-links" class="speciesField collapse in">

						<g:each in="${projectInstance?.dataLinks}" var="dataLink">
							<table>
								<tr>
									<td class="prop" style="vertical-align:top;"><span class="name"><g:message code="default.description.label" /></span></td>
									<td>
										${raw(dataLink.description)}
									</td>
								</tr>
								<tr>
									<td></td>
									<td class="linktext">
										<a target="_blank" href="${dataLink.url}"><g:message code="link.datalink" /></a>
									</td>
								</tr>
							</table>
						</g:each>
					</div>
				</div>
			</g:if>




			<g:if test="${projectInstance?.misc || projectInstance?.miscFiles}">

				<div class="sidebar_section">
					<a data-toggle="collapse" href="#misc"><h5><g:message code="heading.miscellaneous" /></h5></a>
					<div id="misc" class="speciesField collapse in">
						<g:if test="${projectInstance?.misc}">
							<div class="notes_view linktext">
								${raw(projectInstance?.misc)}
							</div>
						</g:if>

						<g:if test="${projectInstance?.miscFiles}">
							<b><g:message code="project.show.files" /></b>

							<g:each in="${projectInstance?.fetchMiscFiles()}" var="miscFile">

                                                            <g:render template="/document/showDocumentStoryTemplate" model="['documentInstance':miscFile, showDetails:false]"/>
							</g:each>
						</g:if>
					</div>
				</div>
			</g:if>


		</div>



		<g:render template="/project/projectSidebar" />
	</div>
	<r:script>
		$(document).ready(function() {
	//		window.params.tagsLink = "${uGroup.createLink(controller:'observation', action: 'tags')}";
	//		initRelativeTime("${uGroup.createLink(controller:'activityFeed', action:'getServerTime')}");
		});
	</r:script>


</body>
</html>
