<%@page import="species.Resource.ResourceType"%>
<%@page import="species.Resource"%>
<%@page import="species.utils.ImageType"%>
<%@page	import="org.springframework.web.context.request.RequestContextHolder"%>
<%@page import="species.License"%>
<%@page import="species.License.LicenseType"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<%@ page import="org.grails.taggable.Tag"%>
<%@ page import="species.utils.Utils"%>
<%@page import="species.Resource.ResourceType"%>
<%@page import="java.util.Arrays"%>

<html>
    <head>
        <g:set var="title" value="${g.message(code:'default.checklist.label')}"/>
        <g:render template="/common/titleTemplate" model="['title':title]"/>
        <style>
            .upload_file div {
                display:inline-block;
            }
        </style>
        <asset:javascript src="slickgrid.js"/>
    </head>
    <body>
        <div class="observation_create">
            <div class="span12">
                <g:render template="/observation/addObservationMenu" model="['entityName':(params.action == 'edit' || params.action == 'update')?'Edit List':'Add List']"/>
                <g:render template="/checklist/addChecklist"/>
            </div>
        </div>

   </body>
</html>
