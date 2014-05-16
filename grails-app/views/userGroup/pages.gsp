
<%@page import="org.springframework.security.acls.domain.BasePermission"%>

<%@page import="org.springframework.security.acls.domain.BasePermission"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@ page import="species.groups.UserGroup"%>
<html>
<head>
<g:set var="entityName"
	value="${(userGroupInstance)?userGroupInstance.name:Utils.getDomainName(request)}" />

<g:set var="title" value="Pages"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
<r:require modules="userGroups_show" />
<style>
    #contentMenu > .nav-tabs > .active > a {
        font-weight:normal;
        color: black;
        background-color:rgba(98, 100, 39, 0.14);
        background: transparent;
        -ms-filter: "progid:DXImageTransform.Microsoft.gradient(startColorstr=#23626427,endColorstr=#23626427)"; /* IE8 */
        filter: progid:DXImageTransform.Microsoft.gradient(startColorstr=#23626427,endColorstr=#23626427);   /* IE6 & 7 */
              zoom: 1;
    }
    #contentMenu > .nav-tabs > li > a {
       color: #16509E;
        font-weight:bold;
        background-color: #CEEBD3;
        border: none
    }
</style>
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
                                                <li id="newsletter_${newsletterInstance.id}"><a data-toggle="tab" class="pageTab" href="#${newsletterInstance.id}"><p style="width:150px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">
                                                    ${fieldValue(bean: newsletterInstance, field: "title")}</p>
                                                            <sUser:permToReorderPages model="['userGroupInstance':userGroupInstance]"><i class="icon-circle-arrow-down pull-right" onclick='changeDisplayOrder("${uGroup.createLink(controller: 'newsletter', action:'changeDisplayOrder', 'userGroup':userGroupInstance)}","${newsletterInstance.id}", "down")'></i><i class="icon-circle-arrow-up pull-right" onclick='changeDisplayOrder("${uGroup.createLink(controller: 'newsletter', action:'changeDisplayOrder', 'userGroup':userGroupInstance)}", "${newsletterInstance.id}", "up")'></i>
                                                            </sUser:permToReorderPages>
                                                        </a></li>
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
  				var contentID = me.attr('href');//e.target.hash; //get anchor
  				if(contentID && contentID != '/project/list') {
	  				e.preventDefault();
	  				var History = window.History; 
		           	$(contentID).load(baseURL+'/'+contentID.replace('#','')+' #pageContent', function(){
				    	History.pushState({state:1}, document.title, pageURL+'/'+contentID.replace('#',''));
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
