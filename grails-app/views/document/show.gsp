
<%@ page import="content.eml.Document"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'document.label', default: 'Document')}" />
<title><g:message code="default.show.label" args="[entityName]" /></title>
<r:require modules="add_file" />
</head>
<body>

	<div style="float: right; margin: 10px 0;">
		<sUser:ifOwns model="['user':documentInstance?.author]">

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
	<div class="body span8" style="padding-left: 20px;">

		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>



		<div class="page-header">
			<h1>
				${fieldValue(bean: documentInstance, field: "title")}
			</h1>
		</div>


		<div>
			<span style="float: right; font-size: 12pt; font-weight: bold;">DOCUMENT
				| ${documentInstance.type }
			</span>
		</div>


		<g:if test="${documentInstance?.description}">

			<dl>
				<dt>Description</dt>
				<dd>
					${documentInstance?.description}
				</dd>
			</dl>
		</g:if>




		<div class="sidebar_section">
			<a href="authoringInfo" data-toggle="collapse" href="#authoringInfo"><h5>Authoring
					Information</h5></a>
			<div id="authoringInfo" class="speciesField collapse in">
				<g:if test="${documentInstance?.uFile?.license}">

					<dl class="dl-horizontal">
						<dt>License</dt>
						<dd>
							${documentInstance?.uFile?.license}
						</dd>
					</dl>
				</g:if>



				<g:if test="${documentInstance?.contributors}">


					<dl class="dl-horizontal">
						<dt>Contributors</dt>
						<dd>
							${documentInstance?.contributors}
						</dd>
					</dl>

				</g:if>

				<g:if test="${documentInstance?.attribution}">

					<dl class="dl-horizontal">
						<dt>Attribution</dt>
						<dd>
							${documentInstance?.attribution}
						</dd>
					</dl>

				</g:if>

			</div>
		</div>



		<div class="sidebar_section">
			<a href="coverageInfo" data-toggle="collapse" href="#coverageInfo"><h5>Coverage
					Information</h5></a>
			<div id="coverageInfo" class="speciesField collapse in">


				<g:if test="${documentInstance.coverage?.speciesGroups}">


					<dl class="dl-horizontal">
						<dt>SpeciesGroups</dt>
						<dd>
							<g:each in="${documentInstance?.coverage?.speciesGroups}"
								var="speciesGroup">
								<button
									class="btn species_groups_sprites ${speciesGroup.iconClass()} active"
									id="${"group_" + speciesGroup.id}" value="${speciesGroup.id}"
									title="${speciesGroup.name}"></button>
							</g:each>
						</dd>
					</dl>
				</g:if>



				<g:if test="${documentInstance.coverage?.habitats}">
					<dl class="dl-horizontal">
						<dt>Habitats</dt>
						<dd>

							<g:each in="${documentInstance.coverage?.habitats}" var="habitat">
								<button
									class="btn habitats_sprites ${habitat.iconClass()} active"
									id="${"habitat_" + habitat.id}" value="${habitat.id}"
									title="${habitat.name}"
									data-content="${message(code: 'habitat.definition.' + habitat.name)}"
									rel="tooltip" data-original-title="A Title"></button>
							</g:each>
						</dd>
					</dl>
				</g:if>


				<g:if test="${documentInstance.coverage?.placeName}">

					<dl class="dl-horizontal">
						<dt>
							<span class="name"><i class="icon-map-marker"></i>Place</span>
						</dt>
						<dd>
							<g:if test="${documentInstance.coverage.placeName == ''}">
								${documentInstance.coverage.reverseGeocodedName}
							</g:if>
							<g:else>
								${documentInstance.coverage.placeName}
							</g:else>
						</dd>
					</dl>
				</g:if>

			</div>
		</div>



		<div class="sidebar_section">

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

		<g:if test="${documentInstance?.tags}">
			<b>Tags : </b>

			<g:render template="/project/showTagsList"
				model="['instance': documentInstance, 'controller': 'document', 'action':'browser']" />

		</g:if>
	</div>
	<g:render template="/document/documentSidebar" />

</body>
</html>
