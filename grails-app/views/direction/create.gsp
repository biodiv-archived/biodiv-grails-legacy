

<%@ page import="content.StrategicDirection"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'strategicDirection.label', default: 'StrategicDirection')}" />
<title><g:message code="default.create.label"
		args="[entityName]" /></title>
<r:require modules="core" />
</head>
<body>
	<div class="nav">
		<span class="menuButton"><a class="home"
			href="${createLink(uri: '/')}"><g:message
					code="default.home.label" /></a></span> <span class="menuButton"><g:link
				class="list" action="list">
				<g:message code="default.list.label" args="[entityName]" />
			</g:link></span>
	</div>
	<div class="body">
		<h1>
			<g:message code="default.create.label" args="[entityName]" />
		</h1>
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${strategicDirectionInstance}">
			<div class="errors">
				<g:renderErrors bean="${strategicDirectionInstance}" as="list" />
			</div>
		</g:hasErrors>
		<%
                def form_action= uGroup.createLink(action:'save', controller:'direction', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
            %>
		<form action="${form_action}" method="POST">
			<div class="dialog">

				<div class="super-section" style="clear: both">
					<div class="section" style="position: relative; overflow: visible;">
					
								<div
							class="control-group ${hasErrors(bean: strategicDirectionInstance, field: 'title', 'error')}">
							<label class="control-label" for="title"><g:message
									code="strategicDirection.title.label" default="Title" /></label>
							<div class="controls">

								<g:textField class="input-xxlarge" name="title"
									value="${strategicDirectionInstance?.title}" />
							</div>

						</div>
						<label for="strategy"><g:message
								code="strategicDirection.strategy.label" default="Strategy" /></label>


						</h5>
						<div class="section-item" style="margin-right: 10px;">

							<ckeditor:config var="toolbar_editorToolbar">
									[
    									[ 'Bold', 'Italic' ]
									]
									</ckeditor:config>
							<ckeditor:editor name="strategy" height="200px"
								toolbar="editorToolbar">
								${strategicDirectionInstance?.strategy}
							</ckeditor:editor>
						</div>
					</div>


				</div>
				<div class="buttons">
					<span class="button"><g:submitButton name="create"
							class="save"
							value="${message(code: 'default.button.create.label', default: 'Create')}" /></span>
				</div>
		</form>
	</div>
</body>
</html>
