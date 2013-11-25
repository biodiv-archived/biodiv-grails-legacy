<%@page import="species.utils.Utils"%>
<%@ page import="species.participation.DownloadLog.DownloadType"%>

<html>
    <head>
        <g:set var="canonicalUrl" value="${uGroup.createLink([controller:'checklist', action:'show', id:checklistInstance.id, base:Utils.getIBPServerDomain()])}"/>
        <g:set var="title" value="${checklistInstance.title}"/>
        <g:set var="description" value="${Utils.stripHTML(checklistInstance.notes?:'')}" />
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
             <g:if test="${checklistInstance}">
                            <g:set var="featureCount" value="${checklistInstance.featureCount}"/>
                            </g:if>

            <div class="page-header clearfix">
                <div style="width:100%;">
                    <div class="main_heading" style="margin-left:0px; position:relative">
                        <span class="badge ${(featureCount>0) ? 'featured':''}" style="left:-50px"  title="${(featureCount>0) ? 'Featured':''}">
                                            </span>

                        <div class="pull-right">
                            <sUser:ifOwns model="['user':checklistInstance.author]">
                            <a class="btn btn-primary pull-right" style="margin-right: 5px;"
                                href="${uGroup.createLink(controller:'checklist', action:'edit', id:checklistInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
                                <i class="icon-edit"></i>Edit</a>

                            <a class="btn btn-danger btn-primary pull-right" style="margin-right: 5px;"
                                href="${uGroup.createLink(controller:'checklist', action:'flagDeleted', id:checklistInstance.id)}"
                                onclick="return confirm('${message(code: 'default.checklist.delete.confirm.message', default: 'This checklist will be deleted. Are you sure ?')}');"><i class="icon-trash"></i>Delete</a>

                            </sUser:ifOwns>

                        </div>
                        <s:showHeadingAndSubHeading
                            model="['heading':checklistInstance.title, 'headingClass':headingClass, 'subHeadingClass':subHeadingClass]" />

                        </div>
                    </div>
                    <div style="clear:both;"></div>
                </div>	
                <div class="span12" style="margin-left:0px; padding:4px; background-color:whitesmoke">
                    <g:render template="/common/observation/showObservationStoryActionsTemplate"
                    model="['instance':checklistInstance, 'href':canonicalUrl, 'title':title, 'description':description, 'hideFlag':true, 'hideDownload':false, 'hideFollow':true]" />

                </div>


                <div class="span8 observation" style="margin:0">
                    <div class="observation_story">
                        <clist:showData model="['checklistInstance':checklistInstance]"/>

                        <obv:showStory
                        model="['observationInstance':checklistInstance, 'showDetails':true, 'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress]" />
                    </div>
                    <uGroup:objectPostToGroupsWrapper 
                    model="['observationInstance':checklistInstance, 'objectType':checklistInstance.class.canonicalName]"/>
                    <div class="union-comment">
                        <feed:showAllActivityFeeds model="['rootHolder':checklistInstance, feedType:'Specific', refreshType:'manual', 'feedPermission':'editable']" />
                        <comment:showAllComments model="['commentHolder':checklistInstance, commentType:'super','showCommentList':false]" />
                    </div>

                </div>

                <div class="span4">

                    <div class="sidebar_section">
                        <obv:showLocation
                            model="['observationInstance':checklistInstance]" />
                    </div>
                    <g:if test="${checklistInstance.userGroups}">
                    <div class="sidebar_section">
                        <h5>Is in groups</h5>
                        <ul class="tile" style="list-style:none; padding-left: 10px;">
                            <g:each in="${checklistInstance.userGroups}" var="userGroup">
                            <li class="">
                            <uGroup:showUserGroupSignature  model="[ 'userGroup':userGroup]" />
                            </li>
                            </g:each>
                        </ul>
                        <!-- obv:showRelatedStory
                        model="['observationInstance':checklistInstance, 'observationId': checklistInstance.id, 'controller':'userGroup', 'action':'getRelatedUserGroups', 'filterProperty': 'obvRelatedUserGroups', 'id':'relatedGroups']" /-->
                    </div>
                    </g:if>
<%--                    <%--%>
<%--                    def annotations = checklistInstance.fetchChecklistAnnotation()--%>
<%--                    %>--%>
<%--                    <g:if test="${annotations?.size() > 0}">--%>
<%--                    <div class="sidebar_section">--%>
<%--                        <h5>Annotations</h5>--%>
<%--                        <div class="tile" style="clear: both">--%>
<%--                            <obv:showAnnotation model="[annotations:annotations]" />--%>
<%--                        </div>--%>
<%--                    </div>	--%>
<%--                    </g:if>--%>

                </div>

            </div>	
            <r:script>
            $(document).ready(function(){
            var species = $.url(decodeURIComponent(window.location.search)).param()["species"]
            if(species){
            species = $.trim(species).replace(/ /g, '_') 
            $(".checklist-data ." + species).css("background-color","#66FF66");
            }
            });
            </r:script>
        </body>
    </html>
