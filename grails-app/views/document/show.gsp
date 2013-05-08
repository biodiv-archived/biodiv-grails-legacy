
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

						<a class="btn btn-danger btn-primary pull-right"
							style="margin-right: 5px; margin-bottom: 10px;"
							href="${uGroup.createLink(controller:'document', action:'flagDeleted', id:documentInstance.id)}"
							onclick="return confirm('${message(code: 'default.document.delete.confirm.message', default: 'This document will be deleted. Are you sure ?')}');"><i
							class="icon-trash"></i>Delete</a>

					</sUser:ifOwns>
				</div>
			</div>

		</div>



		<div class="span8 right-shadow-box"
			style=" padding-right: 5px;">


			<div style="height: 50px;">
				<span style="float: right; font-size: 12pt; font-weight: bold;">Document
					| ${documentInstance.type?.value }
				</span>
			</div>



			<g:if test="${documentInstance?.description}">
				<div class="sidebar_section">
					<a class="speciesFieldHeader" data-toggle="collapse"
						href="#description"><h5>Description</h5></a>
					<div id="description" class="speciesField collapse in">
						<dl class="dl linktext">
							<dd>
								${documentInstance?.description}
							</dd>
						</dl>

					</div>
				</div>
			</g:if>



			<g:if
				test="${documentInstance.contributors || documentInstance?.attribution || documentInstance.license}">
				<div class="sidebar_section">
					<a class="speciesFieldHeader" data-toggle="collapse"
						href="#authoringInfo"><h5>Authoring Information</h5></a>
					<div id="authoringInfo" class="speciesField collapse in">

						<table>
							<g:if test="${documentInstance?.contributors}">

								<tr>
									<td class="prop"><span class="grid_3 name">Contributors</span></td>
									<td class="linktext">
										${documentInstance?.contributors}
									</td>
								</tr>
							</g:if>

							<g:if test="${documentInstance?.attribution}">

								<tr>
									<td class="prop"><span class="grid_3 name">Attribution</span></td>
									<td class="linktext">
										${documentInstance?.attribution}
									</td>


								</tr>
							</g:if>

							<g:if test="${documentInstance?.license}">
								<tr>
									<td class="prop"><span class="grid_3 name">License</span></td>

									<td><img
										src="${resource(dir:'images/license',file:documentInstance?.license?.name.value().toLowerCase().replaceAll('\\s+','')+'.png', absolute:true)}"
										title="${documentInstance.license.name}" /></td>
								</tr>
							</g:if>

						</table>



					</div>
				</div>
			</g:if>

			<g:if
				test="${documentInstance?.coverage?.speciesGroups || documentInstance.coverage?.habitats || documentInstance.coverage?.placeName }">

				<div class="sidebar_section">
					<a class="speciesFieldHeader" href="#coverageInfo"
						data-toggle="collapse"><h5>Coverage Information</h5></a>
					<div id="coverageInfo" class="speciesField collapse in">
						<table>

							<g:if test="${documentInstance.coverage?.speciesGroups}">


								<tr>
									<td class="prop"><span class="grid_3 name">SpeciesGroups</span></td>
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


							<g:if test="${documentInstance.coverage?.placeName}">
								<tr>

									<td class="prop"><span class="grid_3 name"><i
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

			<g:if test="${documentInstance?.tags}">

				<div class="sidebar_section">
					<a class="speciesFieldHeader" href="#tags" data-toggle="collapse"><h5>Tags</h5></a>
					<div id="tags" class="speciesField collapse in">
						<table>
							<tr>
								<td><g:render template="/project/showTagsList"
										model="['instance': documentInstance, 'controller': 'document', 'action':'browser']" />
								</td>
							</tr>
						</table>
					</div>
				</div>
			</g:if>
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
							<dd>
								${documentInstance.uri}
							</dd>
						</dl>
					</g:if>
				</div>
			</g:if>


			<g:if test="${documentInstance.userGroups}">
				<div class="sidebar_section">
					<h5>Document is in groups</h5>
					<ul class="tile" style="list-style: none; padding-left: 10px;">
						<g:each in="${documentInstance.userGroups}" var="userGroup">
							<li class=""><uGroup:showUserGroupSignature
									model="[ 'userGroup':userGroup]" /></li>
						</g:each>
					</ul>

				</div>
			</g:if>

		</div>
		<g:render template="/document/documentSidebar" />

	</div>

</body>
</html>
