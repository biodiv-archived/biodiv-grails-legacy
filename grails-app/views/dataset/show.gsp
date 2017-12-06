<%@page import="species.utils.Utils"%>
<%@ page import="species.participation.DownloadLog.DownloadType"%>
<%@page import="species.dataset.Dataset1"%>
<%@page import="species.dataset.DataTable"%>

<html>
    <head>
        <g:set var="canonicalUrl" value="${uGroup.createLink([controller:'dataset', action:'show', id:datasetInstance.id, base:Utils.getIBPServerDomain()])}"/>
        <g:set var="title" value="${datasetInstance.title}"/>
        <g:set var="description" value="${Utils.stripHTML(datasetInstance.description?:'')}" />
        <g:render template="/common/titleTemplate" model="['title':title, 'description':description, 'canonicalUrl':canonicalUrl, 'imagePath':null]"/>
        <r:require modules="checklist"/>
        <style>
            .observation_story .observation_footer {
            margin-top:50px;
            }
            .list_view li:nth-child(odd) {
                margin-left:0px;
            }
            .list_view li:nth-child(even) {
                clear:right;
            }
        </style>
    </head>
    <body>

        <div class="row-fluid span12">

            <clist:showSubmenuTemplate />
            <g:if test="${datasetInstance}">
            <g:set var="featureCount" value="${0}"/>
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
                            <sUser:ifOwns model="['user':datasetInstance.author]">

                            <a class="btn btn-primary pull-right" style="margin-right: 5px;"
                                href="${uGroup.createLink(controller:'dataTable', action:'create', dataset:datasetInstance.id)}"
                                ><i class="icon-plus"></i><g:message code="button.create.dataTable" /></a>


                            <a class="btn btn-primary pull-right" style="margin-right: 5px;"
                                href="${uGroup.createLink(controller:'dataset', action:'edit', id:datasetInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
                                <i class="icon-edit"></i><g:message code="button.edit" /></a>

                            <a class="btn btn-danger btn-primary pull-right" style="margin-right: 5px;"
                                href="${uGroup.createLink(controller:'dataset', action:'flagDeleted', id:datasetInstance.id)}"
                                onclick="return confirm('${message(code: 'default.dataset.delete.confirm.message', default: 'This dataset will be deleted. Are you sure ?')}');"><i class="icon-trash"></i><g:message code="button.delete" /></a>

                            </sUser:ifOwns>

                        </div>
                        <s:showHeadingAndSubHeading
                            model="['preText':'Dataset : ', 'heading':datasetInstance.title, 'headingClass':headingClass, 'subHeadingClass':subHeadingClass]" />

                        </div>
                    </div>
                    <div style="clear:both;"></div>
                </div>	

                <div class="span12 right-shadow-box observation" style="margin:0">
                    <g:render template="/dataset/showDatasetStoryTemplate" model="['datasetInstance':datasetInstance, showDetails:true,'userLanguage':userLanguage]"/>

                    <div class="mainContentList" style="overflow:hidden;">
                        <div class="mainContent">
                            <ul class="list_view obvListWrapper" style="list-style:none;margin-left:0px;">
                                <g:each in="${DataTable.findAllByDatasetAndIsDeleted(datasetInstance, false, [sort:'createdOn', order:'desc'])}" var="dataTableInstance">
                                <li id="dataTable_${dataTableInstance.id}" class="span6" style="margin-top:10px;">
                                <g:render template="/dataTable/showDataTableStoryTemplate" model="['dataTableInstance':dataTableInstance, showDetails:true,'userLanguage':userLanguage]"/>
                                </li>
                                </g:each>
                            </ul>			
                        </div>
                    </div>
                    <div class="union-comment">
                        <feed:showAllActivityFeeds model="['rootHolder':datasetInstance, feedType:'Specific', refreshType:'manual', 'feedPermission':'editable']" />
                        <comment:showAllComments model="['commentHolder':datasetInstance, commentType:'super','showCommentList':false]" />
                    </div>

                </div>


            </div>	
        </div>
    </body>
</html>
