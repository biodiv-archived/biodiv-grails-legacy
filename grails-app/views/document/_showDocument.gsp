<%@ page import="content.eml.Document"%>
<div class="observation_story sidebar_section">
	<%
    // To overcome hibernate fileassist issue - http://www.intelligrape.com/blog/2012/09/21/extract-correct-class-from-hibernate-object-wrapped-with-javassist/
     documentInstance = Document.read(documentInstance.id)
    %>

	<g:if test="${documentInstance.uFile}">
		<div class="prop">
			<span class="name">File</span>
			<div class="value">

					<fileManager:displayFile
						filePath="${ documentInstance?.uFile?.path}"
						fileName="${ documentInstance?.title}"></fileManager:displayFile>
			</div>
		</div>
	</g:if>

	<g:if test="${documentInstance.uri}">
		<div class="prop">
			<span class="name">URL</span>
			<div class="value">
				<span class="linktext" style="word-wrap: break-word;">
					${documentInstance.uri}
				</span>
			</div>
		</div>
	</g:if>

	<div class="prop">
		<span class="name">Type</span>
		<div class="value">
			${documentInstance.type?.value }
		</div>
	</div>

	<g:if
		test="${documentInstance?.notes && documentInstance?.notes.trim() != ''}">
		<div class="prop">
			<span class="name">Description</span>
			<div class="notes_view linktext value">
				${documentInstance?.notes}
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

	<g:if test="${showDetails && documentInstance?.fetchSource()}">
		<div class="prop">
			<span class="name">Source</span>
			<%	
				def sourceObj = documentInstance.fetchSource()
				def className = sourceObj.class.getSimpleName()
			%>
			<div class="value">
				<a
					href="${uGroup.createLink(controller: className.toLowerCase(), action:"show", id:sourceObj.id, 'userGroupWebaddress':params?.webaddress)}"><b>
						${className + ": "}
				</b>
					${sourceObj}</a>
			</div>
		</div>
	</g:if>

	<g:if test="${showDetails && documentInstance?.tags}">
		<div class="prop">
			<span class="name">Tags</span>

			<div class="value">
				<g:render template="/project/showTagsList"
					model="['instance': documentInstance, 'controller': 'document', 'action':'browser']" />
			</div>
		</div>
	</g:if>
</div>
