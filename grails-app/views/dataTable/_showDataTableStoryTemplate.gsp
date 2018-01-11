<%@page import="species.utils.Utils"%>
<%@page import="species.UtilsService"%>
<%@page import="species.Species"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.participation.Observation"%>
<%@page import="species.dataset.DataTable"%>
<%@ page import="species.participation.DownloadLog.DownloadType"%>
<%@ page import="species.participation.UploadLog"%>


<% 
int instanceCount = dataTableInstance.getDataObjectsCount();
String instanceType = dataTableInstance.dataTableType;
%>
<div name="dataTable_${dataTableInstance.id}" class="sidebar_section observation_story" style="margin:0px;height:100%;width:99.9%;padding:0px;">
        <button type="button" data-toggle="collapse" data-parent="#dataTable_${dataTableInstance.id}" data-target="#dataTable_${dataTableInstance.id}_body" style="width:100%; text-align:left;${showTitleDetail?:'display:none;'}">
<h5>

        <span><g:message code="default.dataTable.label" /> : </span>
        <g:link url="${uGroup.createLink(controller:'dataTable', action:'show', 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress, 'id':dataTableInstance.id) }">
        ${dataTableInstance.title} <g:if test="${instanceCount}">(${instanceCount} ${instanceType})</g:if>
        <span class="pull-right icon-chevron-down"></span>
        </g:link>
 
    </h5>
    </button>
   <div id="dataTable_${dataTableInstance.id}_body" class="observation_story_body collapse ${showDetails && !hideBody ?'in':''} ${showFeatured?'toggle_story':''}" style=" ${showFeatured?'display:none;':''}">
            <div>
                <div id="dataTable_${dataTableInstance.id}_table">
                    <g:if test="${dataTableInstance.uploadLog}">
                        <g:if test="${dataTableInstance.uploadLog.status == UploadLog.Status.UPLOADED}">
                            <g:render template="/dataTable/showDataTableDataTemplate" model="[dataTableInstance:dataTableInstance]"/>
                        </g:if> 
                        <g:else>
                        <div class="alert alert-info">
                            Upload is under process <br/>
                            Current status is : ${dataTableInstance.uploadLog.status}
                        </div>
                        </g:else>
                    </g:if>
                    <g:else>
                        <g:render template="/dataTable/showDataTableDataTemplate" model="[dataTableInstance:dataTableInstance]"/>
                    </g:else>
                </div>
            </div>


 
                <div class="prop">
                    <g:if test="${showDetails}">
                    <span class="name"><i class="icon-time"></i><g:message code="default.observed.on.label" /></span>
                    </g:if>
                    <g:else>
                    <i class="pull-left icon-time"></i>
                    </g:else>
                    <div class="value">
                        <time class="timeago"
                        datetime="${dataTableInstance.temporalCoverage.fromDate.getTime()}"></time>
                        <g:if test="${dataTableInstance.temporalCoverage.toDate && dataTableInstance.temporalCoverage.fromDate != dataTableInstance.temporalCoverage.toDate}">&nbsp;
                        <b>-</b>&nbsp; <time class="timeago" datetime="${dataTableInstance.temporalCoverage.toDate.getTime()}"></time>
                        </g:if>
                        <g:if test="${dataTableInstance.temporalCoverage.dateAccuracy}">
                            (${dataTableInstance.temporalCoverage.dateAccuracy.toLowerCase().capitalize()})
                        </g:if>
                    </div>
                </div>


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

                <g:if test="${dataTableInstance.dataset}">
                <div class="prop">
                    <g:if test="${showDetails}">
                    <span class="name"><i class="icon-globe"></i><g:message code="dataset.label" /></span>
                    </g:if>
                    <g:else>
                    <i class="pull-left icon-globe"></i>
                    </g:else>

                    <div class="value">
                        <g:link url="${uGroup.createLink(controller:'dataset', action:'show', 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress, 'id':dataTableInstance.dataset.id) }">${dataTableInstance.dataset.title}</g:link>
                    </div>
                </div>
                </g:if>
                 <div class="prop">
                    <g:if test="${showDetails}">
                    <span class="name"><i class="icon-info-sign"></i><g:message code="default.dataTableType.label" /></span>
                    </g:if>
                    <g:else>
                    <i class="pull-left icon-info-sign"></i>
                    </g:else>
                    <div class="value">
                        ${instanceType}
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
 
                <g:if test="${dataTableInstance.party?.attributions}">
                <div class="prop" >
                    <g:if test="${showDetails}">
                    <span class="name"><i class="icon-info-sign"></i><g:message code="default.attribution.label" /></span>
                    </g:if>
                    <g:else>
                    <i class="pull-left icon-info-sign"></i>
                    </g:else>
                    <div class="value linktext">
                        ${dataTableInstance.party.attributions}
                    </div>
                </div>
                </g:if>
 
                <g:if test="${dataTableInstance.project}">
                <div class="prop" >
                    <g:if test="${showDetails}">
                    <span class="name"><i class="icon-info-sign"></i><g:message code="default.project.label" /></span>
                    </g:if>
                    <g:else>
                    <i class="pull-left icon-info-sign"></i>
                    </g:else>
                    <div class="value linktext">
                        ${dataTableInstance.project}
                    </div>
                </div>
                </g:if>
 
                <g:if test="${dataTableInstance.methods}">
                <div class="prop" >
                    <g:if test="${showDetails}">
                    <span class="name"><i class="icon-info-sign"></i><g:message code="default.methods.label" /></span>
                    </g:if>
                    <g:else>
                    <i class="pull-left icon-info-sign"></i>
                    </g:else>
                    <div class="value linktext">
                        ${dataTableInstance.methods}
                    </div>
                </div>
                </g:if>
 
                <g:if test="${dataTableInstance.summary}">
                <div class="prop">
                    <g:if test="${showDetails}">
                    <span class="name"><i class="icon-info-sign"></i><g:message code="default.summary.label" /></span>
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
                            <div class=" linktext ellipsis multiline" style="display:${styleVar}">${dataTableInstance.summary.encodeAsRaw()}</div>
                        </div>
                   </g:if>
                    <g:else>
                    <% String desc = dataTableInstance.summary%> 
                    <div class="value notes_view linktext ellipsis multiline">
                        ${raw(desc)}
                    </div>

                    </g:else>
                </div>
        </g:if>





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
                            <div class=" linktext ellipsis multiline" style="display:${styleVar}">${raw(Utils.linkifyYoutubeLink(dataTableInstance.description))}</div>
                    
                        </div>
                    </g:if>
                    <g:else>
                    <% String desc = dataTableInstance.description%> 
                    <div class="value notes_view linktext ellipsis multiline">
                        ${raw(desc)}
                    </div>

                    </g:else>
                </div>
            </g:if>



                <g:if test="${dataTableInstance.customFields}">
                <g:each in="${dataTableInstance.fetchCustomFields()}" var="${cf}">
                <g:each in="${cf}" var="${cfv}">
                <g:if test="${cfv.value}">
                <div class="prop">
                    <g:if test="${showDetails}">
                    <span class="name"><i class="icon-globe"></i>${cfv.key}</span>
                    </g:if>
                    <g:else>
                    <i class="pull-left icon-globe"></i>
                    </g:else>
                    <div class="value">
                        ${raw(cfv.value)} 
                    </div>
                </div>
                </g:if>
                </g:each>
                </g:each>
                </g:if>
  
           </g:if>

               <div class="row observation_footer" style="margin-left:0px;height:40px;">
                    <g:render template="/dataTable/showDataTableStoryFooterTemplate" model="['instance':dataTableInstance, 'showDetails':showDetails, 'showLike':true]" />

                    <div class="story-footer" style="right:3px;">
                        <sUser:showUserTemplate
                        model="['userInstance':dataTableInstance.party.fetchContributor(), 'userGroup':userGroup]" />
                    </div>
                </div>

        </div>
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
.thumbnail .observation_story_body {
    padding:0px;
}
    </g:if>

</style>

