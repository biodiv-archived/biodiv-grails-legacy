<%@ page import="content.fileManager.UFile"%>
<%@ page import="org.grails.taggable.Tag"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'UFile.label', default: 'UFile')}" />
<title>File Manager</title>
<r:require modules="add_file" />
<uploader:head />
</head>
<body>
	<div class="nav">
		<span class="menuButton"><a class="home"
			href="${createLink(uri: '/')}"><g:message
					code="default.home.label" /></a></span> <span class="menuButton"><g:link
				class="create" action="create">
				<g:message code="default.new.label" args="[entityName]" />
			</g:link></span>
	</div>
	<div class="body">
		<h1>
			File Manager
		</h1>
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>


		<%
					def canUploadFile = true //based on configuration
		%>
		<g:if test="${canUploadFile}">
		<sec:ifLoggedIn>
			<%
                def form_action = uGroup.createLink(action:'save_browser', controller:'UFile', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
				def name = 'browser'
            %>
            
			<form class="form-horizontal" action="${form_action}" method="POST" id="upload-file">


        <fileManager:uploader model="['name':'browser']"/>

				<div class="buttons">
					<span class="button"><g:submitButton name="create"
							class="save"
							value="${message(code: 'default.button.save.label', default: 'Save')}" /></span>
				</div>
			</form>

</sec:ifLoggedIn>
		</g:if>


		<div class="list">
			<table class="table table-hover">
				<thead>
					<tr>

						<g:sortableColumn property="name"
							title="${message(code: 'UFile.name.label', default: 'Name')}" />

						<g:sortableColumn property="description"
							title="${message(code: 'UFile.description.label', default: 'Description')}" />

						<g:sortableColumn property="tags"
							title="${message(code: 'UFile.tags.label', default: 'Tags')}" />

						<g:sortableColumn property="size"
							title="${message(code: 'UFile.size.label', default: 'Size')}"
							colspan="3" />

					</tr>
				</thead>
				<tbody>
					<g:each in="${UFileInstanceList}" status="i" var="UFileInstance">
						<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

							<td>
								<g:link controller="UFile" action="show"  id="${UFileInstance.id}"> ${fieldValue(bean: UFileInstance, field: "name")}</g:link>
							</td>

							<td>
								${fieldValue(bean: UFileInstance, field: "description")}
							</td>
							
							<td>
								${UFileInstance.tags}
							</td>

							<td>
								${fieldValue(bean: UFileInstance, field: "size")}
							</td>
							
							
							<td>
							<fileManager:download id="${UFileInstance.id}">Download</fileManager:download>
							</td>

							
						</tr>
					</g:each>
				</tbody>
			</table>
		</div>
		            <div class="paginateButtons">
                <g:paginate total="${UFileInstanceTotal}" />
            </div>
	</div>


</body>
</html>
