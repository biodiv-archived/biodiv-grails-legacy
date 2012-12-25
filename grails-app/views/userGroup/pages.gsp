
<%@page import="org.springframework.security.acls.domain.BasePermission"%>

<%@page import="org.springframework.security.acls.domain.BasePermission"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@ page import="species.groups.UserGroup"%>
<html>
<head>
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${(userGroupInstance)?userGroupInstance.name:Utils.getDomainName(request)}" />
<title><g:message code="default.show.label"
		args="[(userGroupInstance)?userGroupInstance.name:Utils.getDomainName(request)]" />
</title>
<r:require modules="userGroups_show" />
</head>
<body>

	<div class="observation span12">
		<uGroup:showSubmenuTemplate model="['entityName':'Pages']" />
		<uGroup:rightSidebar model="['userGroupInstance':userGroupInstance]" />
		<div class="userGroup-section">
			<div class="pull-right">
				<g:if test="${userGroupInstance}">
					<sec:permitted className='species.groups.UserGroup'
						id='${userGroupInstance.id}'
						permission='${org.springframework.security.acls.domain.BasePermission.ADMINISTRATION}'>

						<a style="margin-bottom: 10px;"
							href="${uGroup.createLink(mapping:"userGroup", action:"pageCreate", 'userGroup':userGroupInstance)}"
							class="btn btn-large btn-info"> <i class="icon-plus"></i>Add
							a Page</a>
					</sec:permitted>
				</g:if>
				<g:else>
					<sUser:isAdmin>
						<a style="margin-bottom: 10px;"
							href="${uGroup.createLink(mapping:"userGroupGeneric", controller:'userGroup', action:"pageCreate") }"
							class="btn btn-large btn-info"> <i class="icon-plus"></i>Add
							a Page</a>
					</sUser:isAdmin>
				</g:else>
			</div>
			<div class="list" style="clear: both;">

				<div id="contentMenu" class="tabbable tabs-right" style="">
					<ul class="nav nav-tabs sidebar" id="pageTabs">
						<g:if test="${userGroupInstance}">
							<li><a href="/cepf_grantee_database">Western Ghats CEPF
									Projects</a></li>
							<li><a href="/themepages/list">Themes</a></li>
						</g:if>
						<g:each in="${newsletters}" var="newsletterInstance" status="i">
							<li><a data-toggle="tab" class="pageTab" href="#${newsletterInstance.id}">
									${fieldValue(bean: newsletterInstance, field: "title")} </a></li>
						</g:each>
					</ul>
					<div class="tab-content">
						<g:each in="${newsletters}" var="newsletterInstance" status="i">
							<div class="tab-pane active" id=${newsletterInstance.id}></div>
						</g:each>
					</div>
				</div>

			</div>
		</div>
	</div>


	<r:script>
		$(document).ready(function(){
			var baseURL = "${uGroup.createLink('controller':'newsletter', 'action':'show', 'userGroup':userGroupInstance) }";
			
	        $('#pageTabs a').click(function (e) {
  				e.preventDefault();
  				var me = $(this);
  				var contentID = e.target.hash; //get anchor 
	           	$(contentID).load(baseURL+'/'+contentID.replace('#','')+' #pageContent', function(){
	            	me.tab('show');
	           	});
			});
			$('a.pageTab:first').click();
		});
	</r:script>
</body>
</html>