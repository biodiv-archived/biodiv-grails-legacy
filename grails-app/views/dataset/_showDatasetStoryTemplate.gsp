<%@page import="species.utils.Utils"%>
<%@page import="species.UtilsService"%>
<%@page import="species.Species"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.participation.Observation"%>
<%@page import="species.auth.SUser"%>

<div name="${datasetInstance.id}" class="sidebar_section observation_story" style="height:100%;width:100%;margin:0px;">
   <g:if test="${showFeatured}">
    <span class="featured_details btn" style="display:none;"><i class="icon-list"></i></span>
    </g:if>

    <g:if test="${showFeatured}">
    <div class="featured_body">
        <div class="featured_title ellipsis"> 
            <div class="heading">
            </div>
        </div>
        <g:render template="/common/featureNotesTemplate" model="['instance':datasetInstance, 'featuredNotes':featuredNotes, 'userLanguage': userLanguage]"/>
    </div>
    </g:if>
    <g:else>
    <div class="observation_story_body ${showFeatured?'toggle_story':''}" style=" ${showFeatured?'display:none;':''}">
        <g:if test="${showTitleDetail}">
                <div class="prop">
                    <span class="name"><i class="icon-list"></i><g:message code="dataset.name.label" /></span>

                    <div class="value">
                        <a href="${uGroup.createLink(controller:'dataset', action: 'show', id:datasetInstance.id)}"><b>${datasetInstance.title}</b></a>
                    </div>
                </div>
                </g:if>



            <g:if test="${datasetInstance.description}">
                <div class="prop">
                    <g:if test="${showDetails}">
                    <span class="name"><i class="icon-info-sign"></i><g:message code="default.notes.label" /></span>
                        <div class="value notes_view"> 
                        <%  def styleVar = 'block';
                            def clickcontentVar = '' 
                        %> 
                        <g:if test="${datasetInstance?.language?.id != userLanguage?.id}">
                                <%  
                                    styleVar = "none"
                                    clickcontentVar = '<a href="javascript:void(0);" class="clickcontent btn btn-mini">'+datasetInstance?.language?.threeLetterCode?.toUpperCase()+'</a>';
                                %>
                            </g:if>
                            ${raw(clickcontentVar)}
                    
                        </div>
                    </g:if>
                    <g:else>
                    <% String desc = datasetInstance.description.replaceAll('(?:\r\n|\r|\n)', '<br />')%> 
                    <div class="value notes_view linktext ellipsis multiline">
                        ${raw(desc)}
                    </div>

                    </g:else>
                </div>
            </g:if>

            <g:if test="${datasetInstance.access.licenseId}">
                <div class="prop">
                    <g:if test="${showDetails}">
                    <span class="name"><i class="icon-globe"></i><g:message code="default.accessRights.label" /></span>
                    </g:if>
                    <g:else>
                    <i class="pull-left icon-globe"></i>
                    </g:else>

                    <div class="value">
                        ${datasetInstance.access.licenseId}
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
                        datetime="${datasetInstance.createdOn.getTime()}"></time>
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
                        datetime="${datasetInstance.lastRevised?.getTime()}"></time>
                    </div>
                </div>


                <g:if test="${datasetInstance.externalUrl}">
                <div class="prop">
                    <g:if test="${showDetails}">
                    <span class="name"><i class="icon-globe"></i><g:message code="default.externalId.label" /></span>
                    </g:if>
                    <g:else>
                    <i class="pull-left icon-globe"></i>
                    </g:else>

                    <div class="value">
                        <a href="${datasetInstance.externalUrl}">${datasetInstance.externalId?:datasetInstance.externalUrl}</a> 
                    </div>
                </div>
                </g:if>

            <g:if test="${datasetInstance.party.contributorId}">
                <div class="prop">
                    <g:if test="${showDetails}">
                    <span class="name"><i class="icon-user"></i><g:message code="default.contributors.label" /></span>
                    </g:if>
                    <g:else>
                    <i class="pull-left icon-user"></i>
                    </g:else>

                    <div class="value">
                    <%def contributor = SUser.read(datasetInstance.party.contributorId);%>
                        <a href="${uGroup.createLink(controller:'SUser', action:'show', id:contributor.id)}">${contributor.name}</a>
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
                        <g:if test="${datasetInstance.party.attributions}">
                        ${datasetInstance.party.attributions}
                        </g:if>
                        <g:else>
                        ${datasetInstance.title} (${UtilsService.formatDate(datasetInstance.publicationDate)})
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

