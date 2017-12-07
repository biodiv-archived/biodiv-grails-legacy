<%@page import="species.utils.Utils"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.dataset.DataPackage.SupportingModules"%>

<g:set var="mainImage" value="${null}" />
<%def imagePath = mainImage?mainImage.fileName : null%>


<div class="thumbnail">
    <div class="figure pull-left observation_story_image" 
        title='${dataPackageInstance.title}'>
        <div style="position:relative;margin:auto;">
            <g:if test="${imagePath}">
            <img class="img-polaroid" style="opacity:0.7;height:auto;width:auto;" src="${imagePath}"/>
            </g:if>
            <g:else>
            </g:else>
        </div>
    </div>

    <div class="observation_story" style="width:auto;">
        <div class="observation_story_body ${showFeatured?'toggle_story':''}" style=" ${showFeatured?'display:none;':''}">
            <g:if test="${dataPackageInstance.description}">
            <div class="prop">
                <g:if test="${showDetails}">
                <!--span class="name"><i class="icon-info-sign"></i><g:message code="default.notes.label" /></span-->
                <div class="linktext">                        

                    <div style="display:${styleVar}">${raw(Utils.linkifyYoutubeLink(dataPackageInstance.description))}</div>

                </div>
                </g:if>
                <g:else>
                <div class="value notes_view linktext ${showDetails?'':'ellipsis'}">
                    ${raw(Utils.stripHTML(dataPackageInstance.description))}
               </div>

                </g:else>
            </div>
            </g:if>
            <div>
            Supporting Modules : 
            <g:each in="${dataPackageInstance.supportingModules()}" var="${sm}">
                ${SupportingModules.list()[Integer.parseInt(sm.key)]}
                <g:if test="${sm.value}">
                    ( ${sm.value.collect {it.name}})
                </g:if>
                ,
            </g:each>
            </div>
            <div>
            Allowed DataTable Types : ${dataPackageInstance.allowedDataTableTypes()}
            </div>

        </div>
    </div>
</div>
