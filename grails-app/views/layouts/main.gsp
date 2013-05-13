<!DOCTYPE html>
<%@page import="species.groups.UserGroup"%>
<%@page import="species.utils.Utils"%>
<%@page
	import="org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils"%>
<html  xmlns="http://www.w3.org/1999/xhtml" xmlns:og="http://ogp.me/ns#" 
      xmlns:fb="https://www.facebook.com/2008/fbml">
<head prefix="og: http://ogp.me/ns# fb: http://ogp.me/ns/fb#">
<title>
	${Utils.getDomainName(request)}
</title>
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
<meta name="description" content="Welcome to the India Biodiversity Portal (IBP) - A repository of information designed to harness and disseminate collective intelligence on the biodiversity of the Indian subcontinent.">
<r:layoutResources />
<ckeditor:resources />
<g:set var="domain" value="${Utils.getDomain(request)}"/>
<g:if test="${domain.equals(grailsApplication.config.ibp.domain) }">
	<link rel="shortcut icon" href="/sites/default/files/ibp_favicon_2.png"
		type="image/x-icon" />
</g:if>
<g:else>
	<link rel="shortcut icon"
		href="/sites/all/themes/wg/images/favicon.png" type="image/x-icon" />
</g:else>
<!-- The standard Google Loader script. --> 
<script src="https://www.google.com/jsapi"
		type="text/javascript"></script>


<g:layoutHead />
<r:require modules="observations_list" />
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
	<div id="postToUGroup" class="overlay" style="display: none;">
        <i class="icon-plus"></i>
    </div>
<%
String supportEmail = "";
String domain = Utils.getDomain(request);
if(domain.equals(grailsApplication.config.wgp.domain)) {
	supportEmail = grailsApplication.config.speciesPortal.wgp.supportEmail;
} else if(domain.equals(grailsApplication.config.ibp.domain)) {
	supportEmail =  grailsApplication.config.speciesPortal.ibp.supportEmail;
}
%>

	<div id="species_main_wrapper" style="clear: both;">
		<domain:showIBPHeader model="['userGroupInstance':userGroupInstance]" />

		<div class="container outer-wrapper">
                    <div id="contributeMenu" class="collapse">
                        <div class="container">
                            <ul style="list-style:none;">
                                <li>
                                Do you have an interesting picture of a species ... you can share it by uploading it here
                        <a class="btn btn-success"
                            href="${uGroup.createLink(
                            controller:'observation', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}" class="btn btn-info"> <i class="icon-plus"></i>Add an Observation</a>
                            </li>
                            <li>
                            or have any document related to biodiversity like any project report or presentations or posters share them here
                        <a class="btn btn-success"
                            href="${uGroup.createLink(
                            controller:'document', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"
                            class="btn btn-info" title="Add Document">
                            <i class="icon-plus"></i> Add Document
                        </a>
                        </li>
                        <li>
                        Have an interesting story post it here.
                        <g:if test="${userGroupInstance}">
                        <sec:permitted className='species.groups.UserGroup'
                        id='${userGroupInstance.id}'
                        permission='${org.springframework.security.acls.domain.BasePermission.ADMINISTRATION}'>

                        <a 
                            href="${uGroup.createLink(mapping:"userGroup", action:"pageCreate", 'userGroup':userGroupInstance)}"
                            class="btn  btn-success"> <i class="icon-plus"></i>Add
                            a Page</a>
                        </sec:permitted>
                        </g:if>
                        <g:else>
                        <sUser:isAdmin>
                        <a
                            href="${uGroup.createLink(mapping:"userGroupGeneric", controller:'userGroup', action:"pageCreate") }"
                            class="btn btn-success"> <i class="icon-plus"></i>Add
                            a Page</a>
                        </sUser:isAdmin>
                        </g:else>
                        </li>
                        <li>Every small bit of information helps in planning for biodiversity conservation. So please contribute and if you have any suggestions or feedback please don't hesistate in share it with us at <span class="mailme">${supportEmail}</span>
</li>
                    </ul>
                        </div>

                    </div>


			<div>
				<div style="padding: 10px 0px; margin-left: -20px">
					<g:layoutBody />
				</div>
			</div>
		</div>


		<domain:showIBPFooter />

	</div>
	<div id="feedback_button" onclick="location.href='/feedback_form';" style="left: -10px;"></div>


	<r:layoutResources />
</body>
</html>
