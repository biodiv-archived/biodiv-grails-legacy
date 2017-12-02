<%@page import="species.utils.Utils"%>
<%@ page import="species.participation.DownloadLog.DownloadType"%>
<%@page import="species.dataset.DataPackage"%>
<%@page import="species.dataset.Dataset"%>

<html>
    <head>
        <g:set var="canonicalUrl" value="${uGroup.createLink([controller:'dataPackage', action:'show', id:dataPackageInstance.id, base:Utils.getIBPServerDomain()])}"/>
        <g:set var="title" value="${dataPackageInstance.title}"/>
        <g:set var="description" value="${Utils.stripHTML(dataPackageInstance.description?:'')}" />
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
            <div class="page-header clearfix">
                <div style="width:100%;">
                    <div class="main_heading" style="margin-left:0px; position:relative">
                        <div class="pull-right">
                            <sUser:ifOwns model="['user':dataPackageInstance.author]">

                            <a class="btn btn-primary pull-right" style="margin-right: 5px;"
                                href="${uGroup.createLink(controller:'dataset', action:'create', dataPackage:dataPackageInstance.id)}"
                                ><i class="icon-plus"></i><g:message code="default.add.label" args="['Dataset']" /></a>


                            <a class="btn btn-primary pull-right" style="margin-right: 5px;"
                                href="${uGroup.createLink(controller:'dataPackage', action:'edit', id:dataPackageInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
                                <i class="icon-edit"></i><g:message code="button.edit" /></a>

                            <a class="btn btn-danger btn-primary pull-right" style="margin-right: 5px;"
                                href="${uGroup.createLink(controller:'dataPackage', action:'flagDeleted', id:dataPackageInstance.id)}"
                                onclick="return confirm('${message(code: 'default.dataPackage.delete.confirm.message', default: 'This dataPackage will be deleted. Are you sure ?')}');"><i class="icon-trash"></i><g:message code="button.delete" /></a>

                            </sUser:ifOwns>

                        </div>
                        <s:showHeadingAndSubHeading
                            model="['preText':'DataPackage : ', 'heading':dataPackageInstance.title, 'headingClass':headingClass, 'subHeadingClass':subHeadingClass]" />

                        </div>
                    </div>
                    <div style="clear:both;"></div>
                </div>	

                <div class="span12 right-shadow-box observation" style="margin:0">
                    <g:render template="/dataPackage/showDataPackageStoryTemplate" model="['dataPackageInstance':dataPackageInstance, showDetails:true,'userLanguage':userLanguage]"/>

                    <div class="union-comment">
                        <feed:showAllActivityFeeds model="['rootHolder':dataPackageInstance, feedType:'Specific', refreshType:'manual', 'feedPermission':'editable']" />
                        <comment:showAllComments model="['commentHolder':dataPackageInstance, commentType:'super','showCommentList':false]" />
                    </div>

                </div>


            </div>	
        </div>
    </body>
</html>
