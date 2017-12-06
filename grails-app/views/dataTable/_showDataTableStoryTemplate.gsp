<%@page import="species.utils.Utils"%>
<%@page import="species.UtilsService"%>
<%@page import="species.Species"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.participation.Observation"%>

<div name="${dataTableInstance.id}" class="sidebar_section observation_story" style="height:100%;width:100%;margin:0px;">
    <h5>
        <a name="${dataTableInstance.id}"></a>
        <span><g:message code="default.dataTable.label" /> : </span>
        <g:link url="${uGroup.createLink(controller:'observation', action:'list', 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress, 'dataTable':dataTableInstance.id, isMediaFilter:false, max:12, offset:0) }" name="l${pos}">
        ${dataTableInstance.title}
        </g:link>

    
    </h5>
    <g:if test="${showFeatured}">
    <span class="featured_details btn" style="display:none;"><i class="icon-list"></i></span>
    </g:if>

    <g:if test="${showFeatured}">
    <div class="featured_body">
        <div class="featured_title ellipsis"> 
            <div class="heading">
            </div>
        </div>
        <g:render template="/common/featureNotesTemplate" model="['instance':dataTableInstance, 'featuredNotes':featuredNotes, 'userLanguage': userLanguage]"/>
    </div>
    </g:if>
    <g:else>
    <div class="observation_story_body ${showFeatured?'toggle_story':''}" style=" ${showFeatured?'display:none;':''}">
                        <div class="pull-right">
                            <sUser:ifOwns model="['user':dataTableInstance.author]">

                            <a class="btn btn-primary pull-right" style="margin-right: 5px;"
                                href="${uGroup.createLink(controller:'dataTable', action:'edit', id:dataTableInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
                                <i class="icon-edit"></i><g:message code="button.edit" /></a>

                            <a class="btn btn-danger btn-primary pull-right" style="margin-right: 5px;"
                                href="${uGroup.createLink(controller:'dataTable', action:'flagDeleted', id:dataTableInstance.id)}"
                                onclick="return confirm('${message(code: 'default.dataset.delete.confirm.message', default: 'This dataset will be deleted. Are you sure ?')}');"><i class="icon-trash"></i><g:message code="button.delete" /></a>

                            </sUser:ifOwns>

                        </div>
 
            <g:if test="${dataTableInstance.description}">
                <div class="prop">
                    <g:if test="${showDetails}">
                    <span class="name"><i class="icon-info-sign"></i><g:message code="default.notes.label" /></span>
                        <div class="value notes_view"> 
                        <%  def styleVar = 'block';
                            def clickcontentVar = '' 
                        %> 
                        <g:if test="${dataTableInstance?.language?.id != userLanguage?.id}">
                                <%  
                                    styleVar = "none"
                                    clickcontentVar = '<a href="javascript:void(0);" class="clickcontent btn btn-mini">'+dataTableInstance?.language?.threeLetterCode?.toUpperCase()+'</a>';
                                %>
                            </g:if>
                            ${raw(clickcontentVar)}
                            <div class=" linktext ellipsis multiline" style="display:${styleVar}">${raw(Utils.linkifyYoutubeLink(dataTableInstance.description.replaceAll('(?:\r\n|\r|\n)', '<br />')))}</div>
                    
                        </div>
                    </g:if>
                    <g:else>
                    <% String desc = dataTableInstance.description.replaceAll('(?:\r\n|\r|\n)', '<br />')%> 
                    <div class="value notes_view linktext ellipsis multiline">
                        ${raw(desc)}
                    </div>

                    </g:else>
                </div>
            </g:if>

            <g:if test="${dataTableInstance.access.rights}">
                <div class="prop">
                    <g:if test="${showDetails}">
                    <span class="name"><i class="icon-globe"></i><g:message code="default.accessRights.label" /></span>
                    </g:if>
                    <g:else>
                    <i class="pull-left icon-globe"></i>
                    </g:else>

                    <div class="value">
                        ${dataTableInstance.access.rights}
                    </div>
                </div>
                </g:if>


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
                        datetime="${dataTableInstance.createdOn.getTime()}"></time>
                    </div>
                </div>

                <div class="prop">
                    <g:if test="${showDetails}">
                    <span class="name"><i class="icon-time"></i><g:message code="default.updated.label" /></span>
                    </g:if>
                    <g:else>
                    <i class="pull-left icon-time"></i>
                    </g:else>
                    <div class="value">
                        <time class="timeago"
                        datetime="${dataTableInstance.lastRevised?.getTime()}"></time>
                    </div>
                </div>


                <g:if test="${dataTableInstance.externalUrl}">
                <div class="prop">
                    <g:if test="${showDetails}">
                    <span class="name"><i class="icon-globe"></i><g:message code="default.externalId.label" /></span>
                    </g:if>
                    <g:else>
                    <i class="pull-left icon-globe"></i>
                    </g:else>

                    <div class="value">
                        <a href="${dataTableInstance.externalUrl}">${dataTableInstance.externalId?:dataTableInstance.externalUrl}</a> 
                    </div>
                </div>
                </g:if>

                <div class="prop">
                    <g:if test="${showDetails}">
                    <span class="name"><i class="icon-info-sign"></i><g:message code="default.attribution.label" /></span>
                    </g:if>
                    <g:else>
                    <i class="pull-left icon-info-sign"></i>
                    </g:else>

                    <div class="value linktext">
                        <g:if test="${dataTableInstance.party.attributions}">
                        ${raw(dataTableInstance.party.attributions.replaceAll('(?:\r\n|\r|\n)', '<br />'))}
                        </g:if>
                        <g:else>
                        ${dataTableInstance.title} (${UtilsService.formatDate(dataTableInstance.cratedOn)})
                        </g:else>
                    </div>
                </div>
   
           </g:if>

        </div>
        </g:else>
    </div>
<style>
    <g:if test="${!showDetails}">

    .observation .prop .value {
        margin-left:260px;
    }
    .group_icon_show_wrap{
        float:left;
    }
    </g:if>
    <g:if test="${!showFeatured}">
    li.group_option{
        height:30px;
    }
    li.group_option span{
        padding: 0px;
        float: left;
    }
    .groups_super_div{
        margin-top: -15px;
        margin-right: 10px;
    }
    .groups_div > .dropdown-toggle{
          height: 25px;
    }
    .group_options, .group_option{
          min-width: 110px;
    }
    .save_group_btn{
        float: right;
        margin-right: 11px;
          margin-top: -9px;
    }
    .group_icon_show_wrap{
        border: 1px solid #ccc;
        float: right;
        height: 33px;
        margin-right: 4px;
    }
    .edit_group_btn{
        top: -10px;
        position: relative;
        margin-right: 12px;
    }
    .propagateGrpHab{
        display:none;
        float: right;
        margin-top: -5px;
    }
    
    </g:if>

</style>
