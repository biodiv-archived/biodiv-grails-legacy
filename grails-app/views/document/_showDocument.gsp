<%@ page import="content.eml.Document"%>
<div class="observation_story sidebar_section">
	<%
    // To overcome hibernate fileassist issue - http://www.intelligrape.com/blog/2012/09/21/extract-correct-class-from-hibernate-object-wrapped-with-javassist/
     documentInstance = Document.read(documentInstance.id)
    %>


	<g:if test="${documentInstance.uFile || documentInstance.uri}">
		<div class="sidebar_section" style="margin-left: 0px;">

			<g:if test="${documentInstance.uFile}">

				<dl class="dl-horizontal">

					<dt>File</dt>
					<dd>
						<g:if test="${showDetails}">
							<fileManager:displayFile
								filePath="${ documentInstance?.uFile?.path}"></fileManager:displayFile>
						</g:if>
						<g:else>
							<fileManager:displayFile
								filePath="${ documentInstance?.uFile?.path}"
								fileName="${ documentInstance?.title}"></fileManager:displayFile>
						</g:else>
						
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

	<g:if test="${showDetails}">
		<div class="prop">
			<span class="name">Type</span>
			<div class="value">
				${documentInstance.type?.value }
			</div>
		</div>
	</g:if>

	<g:if
		test="${documentInstance?.description && documentInstance?.description.trim() != ''}">
		<div class="prop">
			<span class="name">Description</span>
			<div class="notes_view linktext value">
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

			<div class="value">
				<img
					src="${resource(dir:'images/license',file:documentInstance?.license?.name.value().toLowerCase().replaceAll('\\s+','')+'.png', absolute:true)}"
					title="${documentInstance.license.name}" />
			</div>
		</div>
	</g:if>

	<g:if test="${documentInstance?.tags}">

		<div class="prop">
			<span class="name">Tags</span>

			<div class="value">
				<g:render template="/project/showTagsList"
					model="['instance': documentInstance, 'controller': 'document', 'action':'browser']" />
			</div>
		</div>
	</g:if>

</div>
