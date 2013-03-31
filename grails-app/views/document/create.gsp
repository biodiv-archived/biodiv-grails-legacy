

<%@ page import="content.eml.Document"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'document.label', default: 'Document')}" />
<title><g:message code="default.create.label"
		args="[entityName]" /></title>
<script src="http://maps.google.com/maps/api/js?sensor=true"></script>

<r:require modules="add_file" />
<uploader:head />

<style>
.control-group.error  .help-inline {
	padding-top: 15px
}

input.dms_field {
	width: 19%;
	display: none;
}

.sidebar-section {
	width: 450px;
	margin: 0px 0px 20px -10px;
	float: right;
}
</style>
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
		<g:hasErrors bean="${documentInstance}">
			<div class="errors">
				<g:renderErrors bean="${documentInstance}" as="list" />
			</div>
		</g:hasErrors>

		<% 
				def form_action = uGroup.createLink(action:'save', controller:'document', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
			
			%>

		<form action="${form_action}" method="POST" class="form-horizontal">

			<div class="span12 super-section">
				<div class="section">

					<div
						class="control-group ${hasErrors(bean: documentInstance, field: 'type', 'error')}">
						<label class="control-label" for="type"><g:message
								code="document.type.label" default="Document Type" /><span
							class="req">*</span></label>
						<div class="controls">
							<g:select name="type"
								from="${content.eml.Document$DocumentType?.values()}"
								keys="${content.eml.Document$DocumentType?.values()*.name()}"
								value="${documentInstance?.type?.name()}" />


						</div>

					</div>
					<div
						class="control-group ${hasErrors(bean: documentInstance, field: 'title', 'error')}">
						<label class="control-label" for="title"><g:message
								code="document.title.label" default="Document Title" /><span
							class="req">*</span></label>
						<div class="controls">

							<input type="text" class="input-xxlarge" name="title"
								value="${documentInstance?.title}" required />
						</div>

					</div>


				</div>
			</div>
			<g:render template="/UFile/uFile"
				model="['uFileInstance':documentInstance?.uFile]"></g:render>
			<g:render template="coverage"
				model="['coverageInstance':documentInstance?.coverage]"></g:render>


			<div class="buttons">
				<span class="button"><g:submitButton name="create"
						class="save"
						value="${message(code: 'default.button.create.label', default: 'Create')}" /></span>
			</div>
		</form>
	</div>

	<r:script>
		$('#use_dms').click(function(){
            if ($('#use_dms').is(':checked')) {
                $('.dms_field').fadeIn();
                $('.degree_field').hide();
            } else {
                $('.dms_field').hide();
                $('.degree_field').fadeIn();
            }
    });
    
        </r:script>
</body>
</html>
