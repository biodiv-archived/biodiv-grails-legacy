
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
		<!-- uGroup:showSubmenuTemplate model="['entityName':'Pages']" /-->
		<uGroup:rightSidebar model="['userGroupInstance':userGroupInstance]" />
		<div class="userGroup-section">
			<div class="pull-right">
				<g:if test="${userGroupInstance}">
					<sec:permitted className='species.groups.UserGroup'
						id='${userGroupInstance.id}'
						permission='${org.springframework.security.acls.domain.BasePermission.ADMINISTRATION}'>

						<a style="margin-bottom: 10px;"
							href="${uGroup.createLink(mapping:"userGroup", action:"pageCreate", 'userGroup':userGroupInstance)}"
							class="btn  btn-success"> <i class="icon-plus"></i>Add
							a Page</a>
					</sec:permitted>
				</g:if>
				<g:else>
					<sUser:isAdmin>
						<a style="margin-bottom: 10px;"
							href="${uGroup.createLink(mapping:"userGroupGeneric", controller:'userGroup', action:"pageCreate") }"
							class="btn btn-success"> <i class="icon-plus"></i>Add
							a Page</a>
					</sUser:isAdmin>
				</g:else>
			</div>
			<div class="list" style="clear: both;">
				<div id="contentMenu" class="tabbable tabs-right" style="">

            					<ul class="nav nav-tabs sidebar_section span4" id="pageTabs">
                                                <li><h5>Pages</h5></li>
						<g:each in="${newsletters}" var="newsletterInstance" status="i">
							<li><a data-toggle="tab" class="pageTab" href="#${newsletterInstance.id}">
									${fieldValue(bean: newsletterInstance, field: "title")} </a></li>
						</g:each>
                                                <g:if test="${userGroupInstance && userGroupInstance.name.equals('The Western Ghats')}">
							<li><a href="/project/list">Western Ghats CEPF
									Projects</a></li>
						</g:if>

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
	</div>


	<r:script>
		$(document).ready(function(){
			var baseURL = "${uGroup.createLink('controller':'newsletter', 'action':'show', 'userGroup':userGroupInstance) }";
			<%if(userGroupInstance ) {%>
				var pageURL = "${uGroup.createLink('mapping':'userGroup', 'action':'page', 'userGroup':userGroupInstance) }";
			<%} else {%>
				var pageURL = "/page";
			<%}%>
			
	        $('#pageTabs a').click(function (e) {
  				
  				var me = $(this);
  				var contentID = e.target.hash; //get anchor
  				if(contentID) {
	  				e.preventDefault();
	  				var History = window.History; 
		           	$(contentID).load(baseURL+'/'+contentID.replace('#','')+' #pageContent', function(){
				    	History.pushState({state:1}, "Species Portal", pageURL+'/'+contentID.replace('#',''));
		            	me.tab('show');
		           	});
	           	} 
			});
			<%if(params.newsletterId) { %>
				$('a.pageTab[href~="#${params.newsletterId}"]').click();
			<%} else {%>
				$('a.pageTab:first').click();
			<%}%>
		});
	</r:script>
</body>
</html>
