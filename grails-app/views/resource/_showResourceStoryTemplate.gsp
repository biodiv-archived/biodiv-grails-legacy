<%@page import="species.utils.Utils"%>
<%@page import="species.Species"%>
<%@page import="species.utils.ImageType"%>
<style>
    <g:if test="${!showDetails}">

    .observation .prop .value {
    margin-left:10px;
    }

    </g:if>
</style>
<div class="observation_story">
    <div class="observation_story_body ${showFeatured?'toggle_story':''}" style=" ${showFeatured?'display:none;':''}">
        <g:if test="${showDetails}">
        <div class="prop">
            <g:if test="${showDetails}">
            <span class="name"><i class="icon-time"></i><g:message code="default.submitted.label" /></span>
            </g:if>
            <g:else>
            <i class="pull-left icon-time"></i>
            </g:else>
            <div class="value">
                <time class="timeago"
                datetime="${resourceInstance.uploadTime.getTime()}"></time>
            </div>
        </div>
        </g:if>

        <g:if test="${resourceInstance.description}">
        <div class="prop">
            <g:if test="${showDetails}">
            <span class="name"><i class="icon-info-sign"></i><g:message code="default.notes.label" /></span>
            <div class="value notes_view linktext">                        
                <%  def styleVar = 'block';
                def clickcontentVar = '' 
                %> 
                <g:if test="${resourceInstance?.language?.id != userLanguage?.id}">
                <%  
                styleVar = "none"
                clickcontentVar = '<a href="javascript:void(0);" class="clickcontent">Click to see the content of '+resourceInstance?.language?.name+'</a>';
                %>
                </g:if>

                ${raw(clickcontentVar)}
                <div style="display:${styleVar}">${raw(Utils.linkifyYoutubeLink(resourceInstance.description))}</div>

            </div>
            </g:if>
            <g:else>
            <div class="value notes_view linktext ${showDetails?'':'ellipsis'}">
                ${raw(Utils.stripHTML(resourceInstance.description))}
            </div>

            </g:else>
        </div>
        </g:if>
    
        <div class="prop" >
            <span class="name"><i class="icon-info-sign"></i><g:message code="default.belongs.to" /></span>
            <div class="value">
                <g:each in="${containers}" var="containerInstance">
                <g:link url="${uGroup.createLink(controller:containerInstance.class.simpleName.toLowerCase(), action:'show', id:containerInstance.id, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }" name="l${pos}" target="_blank">
                ${raw(containerInstance.fetchSpeciesCall())}
                </g:link>
                </g:each>
            </div>
        </div>

        <div class="row observation_footer" style="margin-left:0px;">

            <div class="story-footer" style="right:3px;">
                <sUser:showUserTemplate
                model="['userInstance':resourceInstance.uploader]" />
            </div>
        </div>
    </div>
</div>

