<!DOCTYPE html>
<%@page import="species.groups.UserGroup"%>
<%@page import="species.utils.Utils"%>
<%@page
import="grails.plugin.springsecurity.SpringSecurityUtils"%>
<html  xmlns="http://www.w3.org/1999/xhtml" xmlns:og="http://ogp.me/ns#" 
    xmlns:fb="https://www.facebook.com/2008/fbml">
    <head prefix="og: http://ogp.me/ns# fb: http://ogp.me/ns/fb#">

        <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

        <title><g:layoutTitle/></title>

        <asset:javascript src="jquery.js"/>
        <asset:javascript src="fileuploader.js"/>
        <asset:stylesheet href="application.css"/>
        <asset:stylesheet src="/all/${org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.app.siteCode}.css"/>
        <g:layoutHead />
        <ckeditor:resources />

        <g:set var="domain" value="${Utils.getDomain(request)}"/>
        <script src="https://www.google.com/jsapi" type="text/javascript"></script>
        <script src="http://code.jquery.com/jquery-migrate-1.2.1.js"></script>
        <script src="https://apis.google.com/js/auth.js" type="text/javascript">
        </script>

        <g:set var="userGroupInstance" value="${userGroupInstance}"/>
        <g:if test="${userGroupInstance && userGroupInstance.theme}">
        <link rel="stylesheet" type="text/css" href="${assetPath(src:'/all/group-themes/'+userGroupInstance.theme + '.css')}" />

        </g:if>


        <g:if test="${grailsApplication.config.speciesPortal.app.googlePlusUrl}">
        <a href="${grailsApplication.config.speciesPortal.app.googlePlusUrl}" rel="publisher"></a>
        </g:if>	

    </head>
    <body>
        <div id="loading" class="loading" style="display: none;">
            <span><g:message code="msg.loading" /> </span>
        </div>
        <div id="postToUGroup" class="overlay" style="display: none;">
            <i class="icon-plus"></i>
        </div>
        <div id="species_main_wrapper" style="clear: both;">
            <domain:showSiteHeader model="['userGroupInstance':userGroupInstance]" />
            <g:if test ="${params.controller == 'namelist'||params.controller == 'trait'}">
            <div class="container-fluid outer-wrapper">
            </g:if>
            <g:else>
            <div class="container outer-wrapper">
             </g:else>
                    <div>
                        <div style="padding: 10px 0px; margin-left: -20px">
                            <g:layoutBody />
                        </div>
                    </div>
            </div>

            <domain:showSiteFooter />
        </div>
 
        <asset:javascript src="application.js"/>
        <asset:deferredScripts/>
    </body>
</html>
