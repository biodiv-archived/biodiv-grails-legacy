
<%@ page import="content.eml.Document"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'document.label', default: 'Document')}" />
<title><g:message code="default.show.label" args="[entityName]" /></title>
<r:require modules="content_view" />
</head>
<body>
	<div class="span12">


		<div class="page-header clearfix">
			<div style="width: 100%;">
				<div class="span8 main_heading" style="margin-left: 0px;">
					<s:showHeadingAndSubHeading
						model="['heading':documentInstance.title, 'subHeading':documentInstance.attribution, 'headingClass':headingClass, 'subHeadingClass':subHeadingClass]" />

				</div>
				<a class="btn btn-success pull-right"
					href="${uGroup.createLink(
						controller:'document', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"
					class="btn btn-info"
					style="margin-top: 10px; margin-bottom: -1px; margin-left: 30px;">
					<i class="icon-plus"></i>Add Document
				</a>
				


				<div style="float: right; margin: 10px 0;">
					<sUser:ifOwns model="['user':documentInstance.author]">

						<a class="btn btn-primary pull-right"
							href="${uGroup.createLink(controller:'document', action:'edit', id:documentInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
							<i class="icon-edit"></i>Edit
						</a>
						
						<a class="btn btn-danger" id="deleteButton" style="margin-right: 5px; margin-bottom: 10px;"><i
							class="icon-trash"></i>Delete</a>
						

							
							<form action="${uGroup.createLink(controller:'document', action:'delete')}" method='POST' name='deleteForm'>
							<input type="hidden" name="id" value="${documentInstance.id}" />
						</form>
						<div id="deleteConfirmDialog" title="Are you sure?"></div>

						<r:script>
							$(document).ready(function() {
								$("#deleteButton").button().bind('click', function() {
									$('#deleteConfirmDialog').dialog('open');
								});
				
								$("#deleteConfirmDialog").dialog({
									autoOpen: false,
									resizable: false,
									height: 100,
									modal: true,
									buttons: {
										'Delete': function() {
											document.forms.deleteForm.submit();
										},
										Cancel: function() {
											$(this).dialog('close');
										}
									}
								});
							});
						</r:script>

					</sUser:ifOwns>
				</div>
			</div>

		</div>



                <div class="span8 right-shadow-box observation" style="margin:0;">

                    <div class="observation_story sidebar_section">
                        <g:if test="${documentInstance.uFile || documentInstance.uri}">
                        <div class="sidebar_section" style="margin-left: 0px;">

                            <g:if test="${documentInstance.uFile}">

                            <dl class="dl-horizontal">

                                <dt>File</dt>
                                <dd>

                                <fileManager:displayFile
                                filePath="${ documentInstance?.uFile?.path}"
                                fileName="${ documentInstance?.uFile?.path}"></fileManager:displayFile>
                                </dd>
                            </dl>
                            </g:if>
                            <g:if test="${documentInstance.uri}">
                            <dl class="dl-horizontal">

                                <dt>URL</dt>
                                <dd class="linktext">
                                ${documentInstance.uri}
                                </dd>
                            </dl>
                            </g:if>
                        </div>
                        </g:if>

                        <div class="prop">
                            <span class="name">Type</span>
                            <div class="value">
                                ${documentInstance.type?.value }
                            </div>
                        </div>

                        <g:if test="${documentInstance?.description}">
                        <div class="prop">
                            <span class="name">Description</span>
                            <div class="notes_view linktext">
                                ${documentInstance?.description}
                            </div>
                        </div>
                        </g:if>
                        <g:if test="${documentInstance?.contributors}">
                        <div class="prop">
                            <span class="name">Contributor(s)</span>
                            <div class="value">
                                ${documentInstance?.contributors}
                            </div>
                        </div>
                        </g:if>
                        <g:if test="${documentInstance?.attribution}">
                        <div class="prop">
                            <span class="name">Attribution</span>
                            <div class="value">
                                ${documentInstance?.attribution}
                            </div>
                        </div>
                        </g:if>
                        <g:if test="${documentInstance?.license}">
                        <div class="prop">
                            <span class="name">License</span>

                            <div class="value"><img
                                src="${resource(dir:'images/license',file:documentInstance?.license?.name.value().toLowerCase().replaceAll('\\s+','')+'.png', absolute:true)}"
                                title="${documentInstance.license.name}" /></div>
                        </div>
                        </g:if>


                    </div>

			<g:if
				test="${documentInstance?.coverage?.speciesGroups || documentInstance.coverage?.habitats || documentInstance.coverage?.placeName }">

				<div class="sidebar_section">
					<a class="speciesFieldHeader" href="#coverageInfo"
						data-toggle="collapse"><h5>Coverage Information</h5></a>
					<div id="coverageInfo" class="speciesField collapse in">
						<table>

							<g:if test="${documentInstance.coverage?.speciesGroups}">


								<tr>
									<td class="prop"><span class="grid_3 name">Species Groups</span></td>
									<td class="linktext"><g:each
											in="${documentInstance?.coverage?.speciesGroups}"
											var="speciesGroup">
											<button
												class="btn species_groups_sprites ${speciesGroup.iconClass()} active"
												id="${"group_" + speciesGroup.id}"
												value="${speciesGroup.id}" title="${speciesGroup.name}"></button>
										</g:each></td>
								</tr>
							</g:if>



							<g:if test="${documentInstance.coverage?.habitats}">
								<tr>
									<td class="prop"><span class="grid_3 name">Habitats</span></td>

									<td class="linktext"><g:each
											in="${documentInstance.coverage?.habitats}" var="habitat">
											<button
												class="btn habitats_sprites ${habitat.iconClass()} active"
												id="${"habitat_" + habitat.id}" value="${habitat.id}"
												title="${habitat.name}"
												data-content="${message(code: 'habitat.definition.' + habitat.name)}"
												rel="tooltip" data-original-title="A Title"></button>
										</g:each></td>
								</tr>
							</g:if>


							<g:if test="${documentInstance.coverage?.placeName || documentInstance.coverage.reverseGeocodedName}">
								<tr>

									<td class="prop"><span class="grid_3 name">
											class="icon-map-marker"></i>Place</span></td>
									<td><g:if
											test="${documentInstance.coverage.placeName == ''}">
											${documentInstance.coverage.reverseGeocodedName}
										</g:if> <g:else>
											${documentInstance.coverage.placeName}
										</g:else></td>
								</tr>
							</g:if>
						</table>

					</div>
				</div>

			</g:if>


		</div>
		<g:render template="/document/documentSidebar" model="['documentInstance':documentInstance]"/>

	</div>

</body>
</html>
