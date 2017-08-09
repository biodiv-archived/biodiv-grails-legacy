        <%@page import="species.participation.Observation"%>
        <%@ page import="species.auth.SUser"%>
        <%@ page import="species.utils.Utils"%>
        <%@page import="species.participation.DownloadLog"%>
        <%@page import="species.participation.Discussion"%>
        <%@page import="species.participation.ActivityFeedService"%>
        <%@page import="species.utils.ImageType"%>
        <%@page import="species.Resource.ResourceType"%>
        <%@page import="species.participation.UploadLog"%>
        <%@page import="species.participation.SpeciesBulkUpload"%>
        <%@page import="species.participation.NamesReportGenerator"%>
        <%@page import="content.eml.Document"%>
        <%@page import="species.Classification"%>
        <%@ page import="species.ScientificName.TaxonomyRank"%>
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

        .activity_count{
          background-color: white;
          padding: 5px 0px;
          height: 520px
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
  

        .list-group.panel > .list-group-item {
          border-bottom-right-radius: 4px;
          border-bottom-left-radius: 4px
        }
        .list-group-submenu {
          margin-left:20px;
        }
         .activity_count{
            height: 550px;
            margin-bottom: 20px;
        }

         .prop {    
            border-bottom:1px groove;
            margin-bottom:6px;
            overflow: auto;
            }
        .prop:hover{background-color:#f2f2f2;}
        .name{margin-bottom:5px;}
        .value{margin-bottom:5px;}
        .forContributor {position: relative;}
        .forContributor { margin-left: 200px; font-weight: normal; position:relative;color:green;}
        .jstree-icon { display: none;}
        .permission_hilight{background-color:#b1f0e7;}
        .uploaded-carousel{background-color:#cfede1;}
        .prop{padding-left:5px;}
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

                <div id="userprofilenavbar" class="navbar">
                            <!--data-spy="affix affix-top" data-offset-top="10px" style="z-index:10000"-->
                            <div class="navbar-inner">
                                <ul class="nav">

                                    <li><a href="#aboutMe"><i class="icon-user"></i><g:message code="default.about.me.label" /></a></li>
                                    <li class="divider-vertical"></li>
                                    <li class="dropdown">
                                            <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                                <i class="icon-book"></i> <g:message code="default.content.label" />
                                                <b class="caret"></b>
                                            </a>
                                            <ul class="dropdown-menu">
                                                <li><a href="#observations"><i class="icon-screenshot"></i><g:message code="default.observation.label" /></a></li>
                                                <li><a href="#species"><i class=" icon-leaf"></i><g:message code="default.species.label" /></a></li>
                                                <li><a href="#documents"><i class="icon-list-alt"></i><g:message code="suser.show.documents" /></a></li>
                                                <li><a href="#discussions"><i class="icon-user"></i><g:message code="suser.show.discussions" /></a></li>
                                            </ul>
                                    </li>
                                    <li class="divider-vertical"></li>
                                    <li><a href="#groups"><i class="icon-group"></i><g:message code="default.groups.label" /></a></li>
                                    <li class="divider-vertical"></li>
                                    
                                    <g:if test="${user.hideEmailId}">
                                        <li style="padding:5px 0px">
                                        <% String staticMessage = '';
                                        if(currentUser) {
                                            staticMessage = g.message(code:'suser.message')+' <a href="'+currentUserProfile+'">'+currentUser.name+'</a>'
                                        }
def contact_me_text=g.message(code:'button.contact.me')                                     
%>

                                            <obv:identificationByEmail
                                            model="['source':params.controller+params.action.capitalize(), 'requestObject':request, 'cssClass':'btn btn-mini', hideTo:true, title:contact_me_text, titleTooltip:'', mailSubject:'', staticMessage:staticMessage,  users:[user]]" />


                                        </li>
                                    </g:if>
                                </ul>
                            </div>
                        </div>

                <div>
                    <div class="row-fluid userProfileSection">

                        <div class="span3 activity_count">
                           
                       <g:render template="userAccordionDetailsTemplate" model="[]" />
                        </div>
                        <div class="span9">
                            <sUser:showUserStory model="['userInstance':user, 'showDetails':true]"></sUser:showUserStory>
                        </div>
                        <div class="clearfix"></div> 
                    </div>
                    <div class="clearfix"></div> 
                        
                    <%
                        def downloadLogList = DownloadLog.findAllByAuthorAndStatus(user, 'Success', [sort: 'createdOn', order: 'desc'])
                        def uploadLogList = UploadLog.findAllByAuthor(user, [sort: 'startDate', order: 'desc']);
                        def speciesBulkUploadList = SpeciesBulkUpload.findAllByAuthor(user, [sort: 'startDate', order: 'desc'])
                        def namesReportList = NamesReportGenerator.findAllByAuthor(user, [sort: 'startDate', order: 'desc'])
                        
                    %>
                            <div class="container">
                                    <div id="content" class="super-section" style="clear: both;">
                                        <h5>
                                            <g:message code="default.observation.label" />
                                        </h5>
                                        <div id="observations" class="section" style="clear:both;margin-left:20px;">
                                         <g:render template="/observation/distinctRecoTableAccordionTemplate" model="[distinctRecoList:distinctRecoList, totalCount:totalCount]"/>
                                         <div class="panel" style="padding-bottom:45px;padding-top:0px;">
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
                                        </div>
                                        <div class="panel" style="padding-bottom:45px;padding-top:0px;">
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
                                        <div>
                                        
                                        </div>
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
                                        
                                    </div>    
                                    <div id="content" class="super-section" style="clear: both;">

                                        <div id="species" class="section" style="clear:both;margin-left:20px;">
                                         <h5>
                                            <g:message code="default.species.label" />
                                        </h5>
                                        <div class="panel" style="padding-bottom:45px;padding-top:0px;">
                                             <h6>
                                                 <span class="name" style="color: #b1b1b1;"> 
                                                   <div class="noOfContributedSpecies" style="display:inline;"> <s:noOfContributedSpecies model="['user':user, 'permissionType':"ROLE_CONTRIBUTOR"]" /></div> </span>
                                             <g:message code="suser.show.contributedspecies" /></h6>               
                                              <%--  <s:showContributedSpecies model="['user':user.id]"/> --%>
                                              <obv:showRelatedStory
                            model="[ 'controller':'observation', 'action':'related','filterProperty': 'contributedSpecies', 'userGroupInstance':userGroupInstance,'filterPropertyValue':user.id,'hideShowAll':true]" />
                                        </div>
                                        </div>
                                         <g:if test="${!namesReportList.isEmpty()}">
                                        <div id="namesValidationReports" class="section" style="clear: both;overflow:auto;">
                                            <h6>
                                                <span class="name" style="color: #b1b1b1;"></span> <g:message code="suser.show.names.validation.report" />
                                            </h6>
                                            <s:namesReportTable model="[uploadList:namesReportList]" />
                                        </div>
                                        </g:if>
                                        <g:if test="${!uploadLogList.isEmpty()}">
                                        <div id="uploads" class="section" style="clear: both;overflow:auto;">
                                            <h6>
                                                <span class="name" style="color: #b1b1b1;"></span> <g:message code="suser.show.uploads" />
                                            </h6>
                                            <s:rollBackTable model="[uploadList:uploadLogList]" />
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
                                           <%
                                    def classifications = [];
                                    Classification.list().each {
                                    classifications.add([it.id, it, null]);
                                    }
                                    classifications = classifications?.sort {return it[1].name}; 
                                    %>
                <!--    <div class="taxonomyBrowser sidebar_section" style="position:relative">
                                <h5><g:message code="button.taxon.browser" /></h5>  
                                <div id="taxaHierarchy">
                    <g:render template="/common/taxonBrowserUserTemplate" model="['classifications':classifications, selectedClassification:265799, 'expandAll':false, 'user':user.id]"/>-->
                    </div>
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
            window.params.tagsLink = "${uGroup.createLink(controller:'species', action: 'tags')}";

            // Javascript to enable link to tab
            var url = document.location.toString();
            if (url.match('#')) {
            $('.nav-tabs a[href=#'+url.split('#')[1]+']').tab('show') ;
            } 

            // Change hash for page-reload
            $('.nav-tabs a').on('shown', function (e) {
                window.location.hash = e.target.hash;
            })
        });
        var taxonRanks = [];
        <g:each in="${TaxonomyRank.list()}" var="t">
        taxonRanks.push({value:"${t.ordinal()}", text:"${g.message(error:t)}"});
        </g:each>
    </script>
        <asset:script>
    $(document).ready(function() {
      $.each(params.map, function(index, val) {
    console.log(index+":"+val);
        });
        <% params.user=user.id %>
        var taxonBrowserOptions = {
            expandAll:false,
            controller:"${params.controller?:'species'}",
            action:"${params.action?:'list'}",
            expandTaxon:"${params.taxon?true:false}",
            user:"${params.user}"
        }
        if(${params.taxon?:false}){
        taxonBrowserOptions['taxonId'] = "${params.taxon}";
        }
        var taxonBrowser = $('.taxonomyBrowser').taxonhierarchy(taxonBrowserOptions);   
        $('.species-list-tabs a').click(function (e) {
          e.preventDefault();
          $('.nav-tabs li').removeClass('active');
          $(this).parent().addClass('active');
          var href = $(this).attr('href');
          $('.tab-pane').removeClass('active');
          $(href).addClass('active');
          //$(this).tab('show');
          return false;
        })
        var noOfContributedSpecies=$('.noOfContributedSpecies').text();
        if(noOfContributedSpecies == 0)
        {
            $('#species').hide();
        }
    });
    </asset:script>
        </body>

        </html>
