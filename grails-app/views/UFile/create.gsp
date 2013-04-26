

<%@ page import="content.fileManager.UFile" %>
<%@ page import="org.grails.taggable.Tag"%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'UFile.label', default: 'UFile')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
        <r:require modules="add_file"/>
        <uploader:head />
        
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.create.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${uFileInstance}">
            <div class="errors">
                <g:renderErrors bean="${uFileInstance}" as="list" />
            </div>
            </g:hasErrors>
            <% 
			def form_action = uGroup.createLink(action:'save', controller:'uFile', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
			%>
		<form method="post" action="${form_action}"
			enctype="multipart/form-data">
			<div class="dialog">
				
			
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
