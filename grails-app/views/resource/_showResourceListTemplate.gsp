<%@page import="species.utils.Utils"%>
<%@page import="species.Resource.ResourceType"%>
<%@page import="species.auth.SUser"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<%@page import="species.Resource"%>


<div class="observations_list ${(params.action == 'bulkUploadResources')?'span8':''} observation" style="clear: both;${(params.action == 'bulkUploadResources')?'top:0px;':''}">
        <div class="mainContentList">
        <div class="mainContent" name="p${params?.offset}">

            <%
            def observationPos = (queryParams?.offset != null) ? queryParams.offset : params?.offset
            %>
            
            <ul class="grid_view thumbnails">

                <g:each in="${resourceInstanceList}" status="i"
                var="resourceInstance">

                <g:if test="${i%4 == 0}">
                <li class="thumbnail" style="clear: both;margin-left:0px;${!inGroupMap || inGroupMap[resourceInstance.id]?'':'background-color:transparent;'}">
                </g:if>
                <g:else>
                <li class="thumbnail" style="${!inGroupMap || inGroupMap[resourceInstance.id]?'':'background-color:transparent;'}">
                </g:else>
                <%
                def basePath;
                if(resourceInstance?.context?.value() == Resource.ResourceContext.OBSERVATION.toString()){
                    basePath = grailsApplication.config.speciesPortal.observations.serverURL
                }
                else if(resourceInstance?.context?.value() == Resource.ResourceContext.SPECIES.toString() || resourceInstance?.context?.value() == Resource.ResourceContext.SPECIES_FIELD.toString()){
                    basePath = grailsApplication.config.speciesPortal.resources.serverURL
                }
                else if(resourceInstance?.context?.value() == Resource.ResourceContext.USER.toString()){
                    basePath = grailsApplication.config.speciesPortal.usersResource.serverURL
                }

                def imagePath = resourceInstance?resourceInstance.thumbnailUrl(basePath): null;
                def fullUrl =  resourceInstance?resourceInstance.url: null;

                def fullImagePath;
                
                if(resourceInstance.type == ResourceType.IMAGE) {
                    fullImagePath = g.createLinkTo(base:basePath,	file: resourceInstance.fileName)
                } else if(resourceInstance.type == ResourceType.VIDEO){
                    fullImagePath = g.createLinkTo(base:fullUrl,	file: '')
                }
                %>
                <div class="snippet tablet">

                    <div class="figure" style="height:150px;">
                        <g:if
                        test="${imagePath}">
                        <g:link url="${fullImagePath}" rel="prettyPhoto[gallery]">
                        <img class="img-polaroid"
                        src="${imagePath}" />
                        </g:link>
                        </g:if>
                    </div>
                    <g:if test="${params.action == 'bulkUploadResources'}">
                    <div>
                        <a href="${uGroup.createLink(action:'show', controller:'user', id:resourceInstance.uploader.id, 'userGroup':userGroup)}">
                            ${resourceInstance.uploader.name}
                        </a> 
                    </div>
                    </g:if>
                    <g:if test="${params.action != 'bulkUploadResources'}">
                    <div class="caption" >
                        <obv:rating model="['resource':resourceInstance]"/>
                    </div>
                    </g:if>
                    </div>
                </li>

                </g:each>
            </ul>
        </div>
    </div>

    <g:if test="${instanceTotal > (queryParams?.max?:0)}">
    <div class="centered">
        <div class="btn loadMore">
            <span class="progress" style="display: none;"><g:message code="msg.loading" /> </span> <span
                class="buttonTitle"><g:message code="msg.load.more" /></span>
        </div>
    </div>
    </g:if>

    <%
    activeFilters?.loadMore = true
    activeFilters?.webaddress = userGroup?.webaddress
    %>

    <div class="paginateButtons" style="visibility: hidden; clear: both">
        <p:paginate total="${instanceTotal?:0}" action="${params.action}" controller="${params.controller?:'resource'}"
        userGroup="${userGroup}" userGroupWebaddress="${userGroupWebaddress}"
        max="${queryParams?.max}" params="${activeFilters}" />
    </div>

    </div>


<g:if test="${params.action == 'bulkUploadResources'}">
    <div class="pull-right hello">
        <g:render template="/resource/usersResTableTemplate" model="['userCountList':userCountList]"/>
    </div>
    </g:if>

