<%@page import="species.utils.Utils"%>
<html>
<head>
<g:set var="canonicalUrl" value="${uGroup.createLink([controller:'SUser', action:'list', base:Utils.getIBPServerDomain()])}" />
<g:set var="title" value="Users"/>
<g:render template="/common/titleTemplate" model="['title':title, 'canonicalUrl':canonicalUrl]"/>

<r:require modules="susers_list"/>
<g:set var="entityName"
	value="${message(code: 'sUser.label', default: 'Users')}" />
</head>

<body>

	<div class="span12">
		<div class="page-header">
			<h1>
				<g:message code="default.list.label" args="[entityName]" />
			</h1>
		</div>
		
		<uGroup:rightSidebar model="['userGroupInstance':userGroupInstance]"/>
		<sUser:showUserListWrapper
			model="['results':results, 'totalCount':totalCount]" />
	</div>

	
</body>
</html>
