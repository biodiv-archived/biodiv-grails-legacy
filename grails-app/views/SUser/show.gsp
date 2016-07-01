        <%@page import="species.participation.Observation"%>
        <%@ page import="species.auth.SUser"%>
        <%@ page import="species.utils.Utils"%>
        <%@page import="species.participation.DownloadLog"%>
        <%@page import="species.participation.Discussion"%>
        <%@page import="species.participation.ActivityFeedService"%>
        <%@page import="species.utils.ImageType"%>
        <%@page import="species.Resource.ResourceType"%>
        <%@page import="species.participation.SpeciesBulkUpload"%>
        <%@page import="species.participation.NamesReportGenerator"%>
        <%@page import="content.eml.Document"%>

        <html>
        <head>

        <g:set var="canonicalUrl" value="${uGroup.createLink([controller:'user', action:'show', id:user.id, base:Utils.getIBPServerDomain()])}"/>
        <g:set var="title" value="${user.name}"/>
        <%def imagePath = user.profilePicture();%>
        <g:set var="description" value="${Utils.stripHTML(user.aboutMe)?:'' }" />

        <g:render template="/common/titleTemplate" model="['title':title, 'description':description, 'canonicalUrl':canonicalUrl, 'imagePath':imagePath]"/>


        <gvisualization:apiImport />
        <g:set var="entityName"
            value="${message(code: 'SUser.label', default: 'SUser')}" />

        <style>
        .prop .name {
            clear: both;
        }
        .super-section  {
            background-color:white;
        }
        .thumbnail .observation_story {
            width: 715px;
        }

        .user_profile{
          background-color: white;
          padding: 5px 0px;
          height: 540px
        }
        .activity_count{
          background-color: white;
          padding: 5px 0px;
          height: 540px
        }
        .userProfileSection .accordion{
          margin: 5px 0px;
          background-color: white;
        }
        .accordion-heading .accordion-toggle{
            height:20px;
        }
        .acc_label{
            float: left;
        }
        .acc_count{
          float: right;
          background-color: gray;
          border-radius: 25px;
          width: 30px;
          height: 19px;
          text-align: center;
          color: whitesmoke;
          font-size: 10px;
          font-weight: bold;
        }
        .user_profile .prop{
            padding:0px 5px;
        }


        .list-group.panel > .list-group-item {
          border-bottom-right-radius: 4px;
          border-bottom-left-radius: 4px
        }
        .list-group-submenu {
          margin-left:20px;
        }
        .user_profile {
            background-color: white;
            height: 540px;
            padding: 15px;
            margin-bottom:10px;
        }
         .prop {    
            border-bottom:1px groove;
            margin-bottom:8px;
            overflow: auto;
            }
        .value{
            font-weight:normal;
            color:#808080;
        }
        .prop:hover{background-color:#f2f2f2;}
        .name{margin-bottom:5px;}
        .value{margin-bottom:5px;}
        </style>
        </head>
        <body>
            <div class="span12">
                <div class="page-header clearfix">
                    <div style="width: 100%;">
                        <div class="span8 main_heading" style="margin-left: 0px;">
                            <h1>
                                ${fieldValue(bean: user, field: "name")}
                            </h1>
                        </div>

                        <div style="float: right; margin: 10px 0;">
                            <sUser:ifOwns model="['user':user]">

                                <a class="btn btn-info pull-right"
                                    href="${uGroup.createLink(action:'edit', controller:'user', id:user.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"><i
                                                                    class="icon-edit"></i><g:message code="button.edit.profile" /> </a>
                                                            <a class="btn btn-info" style="float: right; margin-right: 5px;"href="${uGroup.createLink(action:'myuploads', controller:'user', id:user.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"><iclass="icon-edit"></i><g:message code="button.my.uploads" /> </a>

                            </sUser:ifOwns>
                        </div>
                    </div>
                </div>



                <%--  <obv:identificationByEmail model="['source':'userProfileShow', 'requestObject':request]" />--%>
                <div>
                    <div class="row-fluid userProfileSection">

                        <div class="span3 activity_count">
                           
                       <g:render template="userAccordionDetailsTemplate" model="[]" />
                        </div>
                        <div class="span9 user_profile">
                            <sUser:showUserStory model="['userInstance':user, 'showDetails':true]"></sUser:showUserStory>
                        
                                       
                            <div class="prop">
                                <span class="name"><i class="icon-user"></i><g:message code="default.about.me.label" /></span>
                                <div class="value pre-scrollable" style="display:block;height:80px;">
                                            <g:if test="${user.aboutMe}">

                                            <%  def styleVar = 'block';
                                                def clickcontentVar = '' 
                                            %> 
                                            <g:if test="${user?.language?.id != userLanguage?.id}">
                                                <%  
                                                  styleVar = "none"
                                                  clickcontentVar = '<a href="javascript:void(0);" class="clickcontent btn btn-mini">'+user?.language?.threeLetterCode.toUpperCase()+'</a>';
                                                %>
                                            </g:if>

                                            ${raw(clickcontentVar)}
                                            <div style="display:${styleVar}">
                                                ${raw(user.aboutMe.replace('\n', '<br/>\n'))}
                                            </div>
                                                
                                            </g:if>
                                </div>
                            </div>
                        </div>
                        
                    </div>
                    <%
                        def downloadLogList = DownloadLog.findAllByAuthorAndStatus(user, 'Success', [sort: 'createdOn', order: 'asc'])
                        def speciesBulkUploadList = SpeciesBulkUpload.findAllByAuthor(user, [sort: 'startDate', order: 'asc'])
                        def namesReportList = NamesReportGenerator.findAllByAuthor(user, [sort: 'startDate', order: 'asc'])
                    %>
                              <div class="container">
                                    <div id="content" class="super-section" style="clear: both;">
                                        <h5>
                                            <g:message code="default.observation.label" />
                                        </h5>
                                        <div id="observations" class="section" style="clear:both;margin-left:20px;">
                                            <h6>
                                                <span class="name" style="color: #b1b1b1;"> 
                                                    <obv:showNoOfObservationsOfUser
                                                    model="['user':user, 'userGroup':userGroupInstance]" /> </span> <g:message code="default.observation.uploaded.label" />
                                    <sUser:ifOwns model="['user':user]">
                                <a class="btn btn-link"
                                    href="${uGroup.createLink(action:'create', controller:'observation', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"><i class="icon-plus"></i><g:message code="link.add.observation" /></a>
                            </sUser:ifOwns>

                                            </h6>
                                            
                                            <obv:showRelatedStory
                                            model="['controller':'observation', 'action':'related', 'filterProperty': 'user', 'filterPropertyValue':user.id, 'id':'user', 'userGroupInstance':userGroupInstance]" />
                                        </div>
                                        <div id="identifications" class="section" style="clear:both;">
                                       
                                        </div>
                                        <div id="identifications" class="section" style="clear:both;">
                                            <h6>
                                                <span class="name" style="color: #b1b1b1;"> 
                                                    <obv:showNoOfRecommendationsOfUser model="['user':user, 'userGroup':userGroupInstance]" /> </span>
                                               <g:message code="default.observation.identified.label" /> 
                                    <sUser:ifOwns model="['user':user]">
                                <a class="btn btn-link"
                                    href="${uGroup.createLink(action:'list', controller:'observation', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"><i class="icon-list"></i><g:message code="heading.browse.observations" /> </a>
                            </sUser:ifOwns>

                                            </h6>
                                            <obv:showRelatedStory
                                            model="['controller':'user', 'resultController':'observation', 'action':'getRecommendationVotes', 'filterProperty': 'user', 'filterPropertyValue':user.id, 'id':'userIds', 'userGroupInstance':userGroupInstance, 'userGroupWebaddress':params.webaddress, 'hideShowAll':true]" />

                                        </div>
                                        
                                        <obv:showBulkUploadRes model="['user':user]">
                                         <div id="bulkUploadResources" class="section" style="clear:both;">
                                            <h6>
                                                <span class="name" style="color: #b1b1b1;"> 
                                                    <obv:showNoOfBulkUploadResOfUser model="['user':user]" /> </span>
                                                Bulk Upload Resources 
                                            </h6>
                                            <obv:showRelatedStory
                                            model="['controller':'observation', 'action':'related', 'filterProperty': 'bulkUploadResources', 'filterPropertyValue':user.id, 'id':'bulkUploadResources', 'userGroupInstance':userGroupInstance, 'userGroupWebaddress':params.webaddress, 'hideShowAll':true]" />

                                        </div>
                                        </obv:showBulkUploadRes>

                                        <div class="row-fuild">
                                        <div id="observations_list_map" class="section observation span6"
                                            style="margin:0px;margin-left:20px;">
                                            <h6>
                                                 <g:message code="suser.show.observations.spread" />
                                            </h6>
                                            <obv:showObservationsLocation
                                            model="['observationInstanceList':totalObservationInstanceList, 'ignoreMouseOutListener':true, width:460, height:400]">
                                            </obv:showObservationsLocation>
                                            <a id="resetMap" data-toggle="dropdown"
                                                href="#"><i class="icon-refresh"></i>
                                                <g:message code="button.reset" /></a>

                                            <div><i class="icon-info"></i><g:message code="map.limit.info" /></div>
                                            <input id="bounds" name="bounds" value="" type="hidden"/>
                                            <input id="user" name="user" value="${user.id}" type="hidden"/>
                                        </div>
                                        <div id="expertice" class="section span6" style="margin:0px;margin-left:20px;width:420px;">
                                        <%
                                        def species_group_observations="${g.message(code:'suser.heading.species.observations')}"
                                        %>
                                        
                                        <chart:showStats model="['title':species_group_observations, columns:obvData.columns, data:obvData.data, htmlData:obvData.htmlData, htmlColumns:obvData.htmlColumns, width:420, height:420, 'hideTable':true]"/>
                                    </div>                                                          
                                     <g:if test="${!downloadLogList.isEmpty()}">
                                     
                                        <div id="downloads" class="section" style="clear: both;">
                                            <h6>
                            <span class="name" style="color: #b1b1b1;"></span> <g:message code="suser.show.Downloads" />
                                            </h6>
                                            <obv:downloadTable model="[downloadLogList:downloadLogList]" />
                                        </div>
                                        </g:if>
         
                                    </div>      
                                         <g:if test="${!namesReportList.isEmpty()}">
                                        <div id="namesValidationReports" class="section" style="clear: both;overflow:auto;">
                                            <h6>
                                                <span class="name" style="color: #b1b1b1;"></span> <g:message code="suser.show.names.validation.report" />
                                            </h6>
                                            <s:namesReportTable model="[uploadList:namesReportList]" />
                                        </div>
                                        </g:if>
                                        
                                        <g:if test="${!speciesBulkUploadList.isEmpty()}">
                                        <div id="uploads" class="section" style="clear: both;overflow:auto;">
                                            <h6>
                                                <span class="name" style="color: #b1b1b1;"></span> <g:message code="suser.show.species.bulk.uploads" />
                                            </h6>
                                            <s:rollBackTable model="[uploadList:speciesBulkUploadList]" />
                                        </div>
                                        </g:if>
                                    </div>    
                                    
                    <%
                        def c = Document.createCriteria()
                        def documents = c.list {
                            eq("author", user)                            
                        }
                    %>
                    <g:if test="${documents.size() > 0 }">
                     <div id="documents" class="super-section" style="clear: both;">
                        <h5>
                            <span class="name" style="color: #b1b1b1;"> <i
                                class="icon-file"></i></span> <g:message code="button.documents" />    
                        </h5>               
                        <ul class="sidebar_section pre-scrollable" style="clear:both; border:1px solid #CECECE;overflow-x:hidden;list-style:none; width:100%; margin-left:0px;">
                        <%    documents.each { 
                                    def documentId = it.id
                                    String docTitle = it.title
                                    String description = it.notes
                                    %>
                                   <li style="float: left; list-style: none; width:876px; border: 1px solid #CECECE; background-color: #FFF; border-radius:7px;">
                                              <g:render template="/species/showSpeciesDocumentTemplate" model="[controller:'document',documentInstance:it,showFeatured:true,showDetails:false, docTitle:docTitle, desc:description]" />
                                    </li>
                                <% } %>
                        </ul>
                    </div>
                    </g:if>

                    <%
                        def d = Discussion.createCriteria()
                        def discussions = d.list {
                            eq("author", user)                            
                        }
                    %>
                    <g:if test="${discussions.size() > 0}">
                    <div id="discussions" class="super-section" style="clear: both;">
                        <h5>
                            <span class="name" style="color: #b1b1b1;"> <i
                                class="icon-comment"></i></span> <g:message code="default.discussion.label" />    
                        </h5>
                    <ul class="sidebar_section pre-scrollable" style="clear:both; border:1px solid #CECECE;overflow-x:hidden;list-style:none; width:100%; margin-left:0px;">                
                            <% discussions.each { 
                                    def documentId = it.id
                                    String docTitle = it.subject
                                    String description = it.body
                                    %>
                                   <li style="float: left; list-style: none; width:876px; border: 1px solid #CECECE; background-color: #FFF; border-radius:7px;">
                                  <g:render template="/species/showSpeciesDocumentTemplate" model="[
                    controller:'discussion', 
                    documentInstance:it,
                    docId:documentId,
                    showFeatured:true, 
                    showDetails:false, docTitle:docTitle, desc:description]"/></li>
                                <% } %>  
                    </ul>
                    </div>
                    </g:if>
                    <div id="groups" class="super-section" style="clear: both;">
                        <h5>
                            <span class="name" style="color: #b1b1b1;"> <i
                                class="icon-group"></i></span> <g:message code="default.groups.label" />
                                    <sUser:ifOwns model="['user':user]">
                                <a class="btn btn-link"
                                    href="${uGroup.createLink(action:'list', controller:'userGroup', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"><i class="icon-plus"></i><g:message code="button.join.groups" /></a>
                            </sUser:ifOwns>

                        </h5>
                        <uGroup:showUserUserGroups model="['userInstance':user]"></uGroup:showUserUserGroups>

                    </div>
                    <%--            </div>--%>
                    <!--<div id="activity" class="super-section" style="clear: both;">
                        <h5>
                            <span class="name" style="color: #b1b1b1;"> <i
                                                        class="icon-tasks"></i> </span><g:message code="button.activity" />
                                                </h5>
                                                <feed:showAllActivityFeeds model="['user':user?.id,'userGroup':userGroupInstance, feedType:ActivityFeedService.USER, 'feedPermission':false, 'feedOrder':ActivityFeedService.LATEST_FIRST]" />
                            </div>
                    -->

                                </div>
                            
                </div>
            </div>



            <asset:script>
            var userRecoffset = 0;
                $(document).ready(function() {
                    updateMapView({'user':'${user?.id}'});
                    $("#seeMoreMessage").hide();
                    $('#tc_tagcloud a').click(function(){
                        var tg = $(this).contents().first().text();
                        window.location.href = "${uGroup.createLink(controller:'observation', action: 'list', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}?tag=" + tg ;
                        return false;
                    });
                    var max = 3;
                    $("#seeMore").click(function(){
                        preLoadRecos(max, userRecoffset, true);
                        userRecoffset = max + userRecoffset;
                    });

                    preLoadRecos(max, userRecoffset, false);
                    userRecoffset = max + userRecoffset;
                    $('.linktext').linkify();
                    //$('#userprofilenavbar').affix();
                
                    $("#resetMap").click(function() {
                        var mapLocationPicker = $('#big_map_canvas').data('maplocationpicker');
                        //refreshList(mapLocationPicker.getSelectedBounds());
                        $("#bounds").val('');
                        refreshMapBounds(mapLocationPicker);
                    });

            });
        </asset:script>
        <script type="text/javascript">
        $(document).ready(function(){
            window.params.observation.getRecommendationVotesURL = "${uGroup.createLink(controller:'user', action:'getRecommendationVotes', id:user.id, userGroupWebaddress:params.webaddress) }";
            window.params.observation.listUrl = "${uGroup.createLink(controller:'observation', action: 'listJSON')}"
        });
        </script>
        </body>

        </html>
