
<%@page import="org.springframework.security.acls.domain.BasePermission"%>

<%@page import="org.springframework.security.acls.domain.BasePermission"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@ page import="species.groups.UserGroup"%>
<html>
<head>
<meta name="layout" content="main" />
<g:set var="entityName" value="${userGroupInstance.name}" />
<title><g:message code="default.show.label"
		args="[userGroupInstance.name]" />
</title>
<r:require modules="userGroups_show" />
</head>
<body>
	<div class="container outer-wrapper">
		<div class="row">
			<div class="observation span12">
				<uGroup:showSubmenuTemplate/>
				<div class="super-section userGroup-section">
					<div class="section">
						<div class="page-header">
							<h5>Pages</h5>
							<div class="btn-group pull-right" style="z-index: 10;">
								<sec:permitted className='species.groups.UserGroup'
									id='${userGroupInstance.id}'
									permission='${org.springframework.security.acls.domain.BasePermission.ADMINISTRATION}'>

									<g:link action="pageCreate" id="${userGroupInstance.id}"
										class="btn btn-large btn-info">
										<i class="icon-plus"></i>Add a Newsletter</g:link>
								</sec:permitted>
							</div>
						</div>
						<g:each in="${userGroupInstance.newsletters}" var="newsletter">
							<g:include controller="newsletter" action="show"
								id="${newsletter.id }" />
						</g:each>
					</div>



				</div>

			</div>
		</div>
	</div>
	<r:script>
		$(document).ready(function(){

		});
	</r:script>
</body>
</html>