<!DOCTYPE html>
<%@page import="species.groups.UserGroup"%>
<%@page import="species.utils.Utils"%>
<%@page
	import="org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils"%>
<html lang="en" xmlns:fb="http://ogp.me/ns/fb#"
	xmlns:og="og: http://ogp.me/ns#">
<head>
<title>
	${Utils.getDomainName(request)}
</title>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

<r:layoutResources />
<ckeditor:resources />

<g:if test="${domain.equals(grailsApplication.config.ibp.domain) }">
	<link rel="shortcut icon" href="/sites/default/files/ibp_favicon_2.png"
		type="image/x-icon" />
</g:if>
<g:else>
	<link rel="shortcut icon"
		href="/sites/all/themes/wg/images/favicon.png" type="image/x-icon" />
</g:else>

<g:layoutHead />

<!-- script src="http://cdn.wibiya.com/Toolbars/dir_1100/Toolbar_1100354/Loader_1100354.js" type="text/javascript"></script><noscript><a href="http://www.wibiya.com/">Web Toolbar by Wibiya</a></noscript-->
<g:set var="userGroupInstance" value="${userGroupInstance}"/>
<%
	//TODO: conditions needs to be cleaned 
		if(!userGroupInstance) {
			if(userGroup) {
				userGroupInstance = userGroup
			} else if(params.userGroup) {
				if(params.userGroup instanceof UserGroup) {
					userGroupInstance = params.userGroup	
				} else {
					userGroupInstance = UserGroup.get(params.long('userGroup'));
				}
			} else if(params.webaddress) {
				userGroupInstance = UserGroup.findByWebaddress(params.webaddress);
			} else if(params.userGroupWebaddress) {
				userGroupInstance = UserGroup.findByWebaddress(params.userGroupWebaddress);
			}
		}
	%>
<g:if test="${userGroupInstance && userGroupInstance.theme}">
	<link rel="stylesheet" type="text/css"
		href="${resource(dir:'group-themes', file:userGroupInstance.theme + '.css')}" />
</g:if>

</head>
<body>
	<div id="loading" class="loading" style="display: none;">
		<span>Loading ...</span>
	</div>

	<div id="species_main_wrapper" style="clear: both;">
		<domain:showIBPHeader model="['userGroupInstance':userGroupInstance]" />

		<div class="container outer-wrapper">
			<div>
				<div style="padding: 10px 0px; margin-left: -20px">
					<g:layoutBody />
				</div>
			</div>
		</div>


		<domain:showIBPFooter />

	</div>



	<r:layoutResources />

</body>
</html>
