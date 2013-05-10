
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
						
						<a class="btn btn-danger"  href="#" style="margin-right: 5px; margin-bottom: 10px;"
							onclick="deleteDocument(); return false;">
							<i class="icon-trash"></i>Delete
						</a>
							
						<form action="${uGroup.createLink(controller:'document', action:'delete')}" method='POST' name='deleteForm'>
							<input type="hidden" name="id" value="${documentInstance.id}" />
						</form>
						<r:script>
						function deleteDocument(){
							if(confirm('This document will be deleted. Are you sure ?')){
								document.forms.deleteForm.submit();
							}
						}
						</r:script>
						
					</sUser:ifOwns>
				</div>
			</div>

		</div>



                <div class="span8 right-shadow-box observation" style="margin:0;">

		        <g:render template="/document/showDocument" model="['documentInstance':documentInstance]"/>
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
                                                                            Place</span></td>
                                                                    <td>
                                                                        
                                                                        <g:if test="${documentInstance.coverage.placeName}">
                                                                        <g:set var="location" value="${documentInstance.coverage.placeName}"/>
                                                                        </g:if>
                                                                        <g:else>
                                                                        <g:set var="location" value="${documentInstance.coverage.reverseGeocodedName}"/>
                                                                        </g:else>

                                                                        <div class="value ellipsis multiline" title="${location}">
                                                                            ${location}
                                                                        </div>
										</td>
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
