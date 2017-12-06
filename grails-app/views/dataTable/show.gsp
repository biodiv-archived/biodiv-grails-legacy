<%@page import="species.utils.Utils"%>
<%@ page import="species.participation.DownloadLog.DownloadType"%>

<html>
    <head>
        <g:set var="canonicalUrl" value="${uGroup.createLink([controller:'dataTable', action:'show', id:dataTableInstance.id, base:Utils.getIBPServerDomain()])}"/>
        <g:set var="title" value="${dataTableInstance.title}"/>
        <g:set var="description" value="${Utils.stripHTML(dataTableInstance.description?:'')}" />
        <g:render template="/common/titleTemplate" model="['title':title, 'description':description, 'canonicalUrl':canonicalUrl, 'imagePath':null]"/>
        <style>
            .observation_story .observation_footer {
                margin-top:50px;
            }
        </style>
    </head>
    <body>

        <div class="span12">

	    <clist:showSubmenuTemplate />
             <g:if test="${dataTableInstance}">
                            <g:set var="featureCount" value="${dataTableInstance.featureCount}"/>
                            </g:if>

            <div class="page-header clearfix">
                <div style="width:100%;">
                    <div class="main_heading" style="margin-left:0px; position:relative">
<%
     def featuredTitle = g.message(code:"title.feature")
    %>
                        <span class="badge ${(featureCount>0) ? 'featured':''}" style="left:-50px"  title="${(featureCount>0) ? featuredTitle:''}">
                                            </span>

                        <div class="pull-right">
                            <sUser:ifOwns model="['user':dataTableInstance.author]">
                            <a class="btn btn-primary pull-right" style="margin-right: 5px;"
                                href="${uGroup.createLink(controller:'dataTable', action:'edit', id:dataTableInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
                                <i class="icon-edit"></i><g:message code="button.edit" /></a>

                            <a class="btn btn-danger btn-primary pull-right" style="margin-right: 5px;"
                                href="${uGroup.createLink(controller:'dataTable', action:'flagDeleted', id:dataTableInstance.id)}"
                                onclick="return confirm('${message(code: 'default.dataTable.delete.confirm.message', default: 'This dataTable will be deleted. Are you sure ?')}');"><i class="icon-trash"></i><g:message code="button.delete" /></a>

                            </sUser:ifOwns>

                        </div>
                        <s:showHeadingAndSubHeading
                            model="['heading':dataTableInstance.title, 'headingClass':headingClass, 'subHeadingClass':subHeadingClass]" />

                        </div>
                    </div>
                    <div style="clear:both;"></div>
                </div>	
                <div class="span12" style="margin-left:0px; padding:4px; background-color:whitesmoke">
                    <g:render template="/common/observation/showObservationStoryActionsTemplate"
                    model="['instance':dataTableInstance, 'href':canonicalUrl, 'title':title, 'description':description, 'hideFlag':true, 'hideDownload':true, 'hideFollow':true]" />

                </div>


                <div class="span8 observation" style="margin:0">
                    <div class="observation_story">

                    <clist:showData model="['checklistInstance':dataTableInstance]"/>
                    <g:render template="/dataTable/showDataTableStoryTemplate" model="['dataTableInstance':dataTableInstance, showDetails:true,'userLanguage':userLanguage]"/>
                    </div>
                    <uGroup:objectPostToGroupsWrapper 
                    model="['observationInstance':dataTableInstance, 'objectType':dataTableInstance.class.canonicalName]"/>
                    <div class="union-comment">
                        <feed:showAllActivityFeeds model="['rootHolder':dataTableInstance, feedType:'Specific', refreshType:'manual', 'feedPermission':'editable']" />
                        <comment:showAllComments model="['commentHolder':dataTableInstance, commentType:'super','showCommentList':false]" />
                    </div>

                </div>

                <div class="span4">

                    <div class="sidebar_section">
                        <obv:showLocation
                            model="['observationInstance':dataTableInstance]" />
                    </div>
                            </div>

            </div>	
            <asset:script>
            $(document).ready(function(){
            var species = $.url(decodeURIComponent(window.location.search)).param()["species"]
            if(species){
            species = $.trim(species).replace(/ /g, '_') 
            $(".dataTable-data ." + species).css("background-color","#66FF66");
            }
            });
            </asset:script>
        </body>
    </html>