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
                clickcontentVar = '<a href="javascript:void(0);" class="clickcontent">Click to see the content of '+resourceInstance?.language?.threeLetterCode.toUpperCase()+'</a>';
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

        <div class="prop" >
            <span class="name"><i class="icon-info-sign"></i><g:message code="default.contributors.label" /></span>
            <div class="value">
                <g:each in="${resourceInstance.contributors}" var="contributor">
                ${contributor.name},
                </g:each>
            </div>
        </div>

        <g:if test="${resourceInstance?.licenses}">
        <div class="prop">
            <span class="name"><i class='icon-ok-sign'></i><g:message code="default.licenses.label" /></span>

            <div class="value">
                <g:each in="${resourceInstance?.licenses}" var="licenseInstance">
                <img
                src="${resource(dir:'images/license',file:licenseInstance.name.value().toLowerCase().replaceAll('\\s+','')+'.png', absolute:true)}"
                title="${licenseInstance.name}" />
                </g:each>
            </div>
        </div>
        </g:if>


    </div>
</div>

