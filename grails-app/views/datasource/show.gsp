<%@page import="species.utils.Utils"%>
<%@ page import="species.participation.DownloadLog.DownloadType"%>
<%@page import="species.dataset.Datasource"%>
<%@page import="species.dataset.Dataset"%>

<html>
    <head>
        <g:set var="canonicalUrl" value="${uGroup.createLink([controller:'datasource', action:'show', id:datasourceInstance.id, base:Utils.getIBPServerDomain()])}"/>
        <g:set var="title" value="${datasourceInstance.title}"/>
        <g:set var="description" value="${Utils.stripHTML(datasourceInstance.description?:'')}" />
        <g:render template="/common/titleTemplate" model="['title':title, 'description':description, 'canonicalUrl':canonicalUrl, 'imagePath':null]"/>
        <r:require modules="checklist"/>
        <style>
            .observation_story .observation_footer {
            margin-top:50px;
            }
        </style>
    </head>
    <body>

        <div class="span12">

            <clist:showSubmenuTemplate />
            <g:if test="${datasourceInstance}">
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
                            <sUser:ifOwns model="['user':datasourceInstance.author]">

                            <a class="btn btn-primary pull-right" style="margin-right: 5px;"
                                href="${uGroup.createLink(controller:'dataset', action:'create', datasource:datasourceInstance.id)}"
                                ><i class="icon-plus"></i><g:message code="button.create.dataset" /></a>


                            <a class="btn btn-primary pull-right" style="margin-right: 5px;"
                                href="${uGroup.createLink(controller:'datasource', action:'edit', id:datasourceInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
                                <i class="icon-edit"></i><g:message code="button.edit" /></a>

                            <a class="btn btn-danger btn-primary pull-right" style="margin-right: 5px;"
                                href="${uGroup.createLink(controller:'datasource', action:'flagDeleted', id:datasourceInstance.id)}"
                                onclick="return confirm('${message(code: 'default.datasource.delete.confirm.message', default: 'This datasource will be deleted. Are you sure ?')}');"><i class="icon-trash"></i><g:message code="button.delete" /></a>

                            </sUser:ifOwns>

                        </div>
                        <s:showHeadingAndSubHeading
                            model="['preText':'Datasource : ', 'heading':datasourceInstance.title, 'headingClass':headingClass, 'subHeadingClass':subHeadingClass]" />

                        </div>
                    </div>
                    <div style="clear:both;"></div>
                </div>	

                <div class="span12 right-shadow-box observation" style="margin:0">
                    <g:render template="/datasource/showDatasourceStoryTemplate" model="['datasourceInstance':datasourceInstance, showDetails:true,'userLanguage':userLanguage]"/>

                    <div class="mainContentList">
                        <div class="mainContent">
                            <ul class="list_view obvListwrapper" style="list-style:none;margin-left:0px;">
                                <g:each in="${Dataset.findAllByDatasourceAndIsDeleted(datasourceInstance, false, [sort:'createdOn', order:'desc'])}" var="datasetInstance">
                                <li id="dataset_${datasetInstance.id}" style="margin-top:10px;">
                                <g:render template="/dataset/showDatasetSnippetTemplate" model="['datasetInstance':datasetInstance, showDetails:true,'userLanguage':userLanguage]"/>
                                </li>
                                </g:each>
                            </ul>			
                        </div>
                    </div>

                    <div class="union-comment">
                        <feed:showAllActivityFeeds model="['rootHolder':datasourceInstance, feedType:'Specific', refreshType:'manual', 'feedPermission':'editable']" />
                        <comment:showAllComments model="['commentHolder':datasourceInstance, commentType:'super','showCommentList':false]" />
                    </div>

                </div>


            </div>	
        </div>
    </body>
</html>
