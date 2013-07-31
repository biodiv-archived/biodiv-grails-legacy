<%@page import="species.Resource.ResourceType"%>
<%@page import="species.utils.Utils"%>
<div>
    <ul id="imagesList" class="thumbwrap thumbnails"
        style='list-style: none; margin-left: 0px;'>
        <g:set var="i" value="${1}" />
        <g:each in="${observationInstance?.resource}" var="r">
        <li class="addedResource thumbnail">
        <%
        def imagePath = '';
        if(r) {
        imagePath = r.thumbnailUrl(Utils.getDomainServerUrlWithContext(request) + '/observations')?:null;
        }
        %>

        <div class='figure' style="height: 40px;">
            <g:link url="${uGroup.createLink(controller:'observation', action:'show', id:observationInstance.id, 'userGroup':userGroupInstance)}">	
                <img id="image_${i}" class="small_profile_pic" style="width: auto; height: auto;"
             src='${imagePath}'/>
            </g:link>
        </div>

        </li>
        </g:each>
    </ul>
</div>

