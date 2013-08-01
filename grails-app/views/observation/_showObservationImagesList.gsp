<%@page import="species.Resource.ResourceType"%>
<%@page import="species.utils.Utils"%>
<%@page import="species.utils.ImageType"%>
<div>
    <ul id="imagesList" class="thumbwrap thumbnails"
        style='list-style: none; margin-left: 0px;'>
        <g:set var="i" value="${1}" />
        <g:each in="${observationInstance?.mainImage()}" var="r">
        <li class="addedResource thumbnail">
        <%
        def imagePath = '';
        if(r) {
        imagePath = r.thumbnailUrl(null, observationInstance.sourceId ? '.png' :null, ImageType.LARGE)?:null;
        }
        %>

        <div class='figure' style="height: 32px;">
            <g:link url="${uGroup.createLink(controller:'observation', action:'show', id:observationInstance.id, 'userGroup':userGroupInstance)}">	
            <img id="image_${i}" class="small_profile_pic" style="width: auto; height: auto;"
            src='${imagePath}'/>
            <g:set var="resCount" value="${observationInstance.resource.size()}"/>
            <g:if test="${resCount > 1}">
                <span class="help-inline">(${resCount-1} more)</span>
            </g:if>

                </g:link>
        </div>

        </li>
        </g:each>
    </ul>
</div>

