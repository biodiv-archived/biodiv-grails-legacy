<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@page import="species.utils.ImageUtils"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.participation.Recommendation"%>
<%@ page import="species.participation.RecommendationVote"%>
<%@page import="species.Resource.ResourceType"%>
<%@page import="species.Resource"%>
<%@page import="speciespage.ChartService"%>
<%@page import="species.participation.Featured"%>

<html>
<head>
<g:set var="canonicalUrl" value="${uGroup.createLink([controller:'observation', action:'show', id:observationInstance.id, base:Utils.getIBPServerDomain()])}"/>
<g:set var="title" value="${(!observationInstance.fetchSpeciesCall()?.equalsIgnoreCase('Unknown'))?observationInstance.fetchSpeciesCall():g.message(code:'link.help.identify')}"/>

<%
def r = observationInstance.mainImage();
def imagePath = '', videoPath='';
if(r) {
    if(r.type == ResourceType.IMAGE) {
        imagePath = r.thumbnailUrl(null, !observationInstance.resource ? '.png' :null, ImageType.LARGE)
    } else if(r.type == ResourceType.VIDEO){
        imagePath = r.thumbnailUrl()
        videoPath = r.getUrl();
    }
}
    
%>
<g:set var="description" value="${Utils.stripHTML(observationInstance.summary()) }" />

<g:render template="/common/titleTemplate" model="['title':title, 'description':description, 'canonicalUrl':canonicalUrl, 'imagePath':imagePath, 'videoPath':videoPath]"/>

<r:require modules="observations_show"/>

<style>
.nameContainer {
    float: left;
}


.textbox input{
    text-align: left;
    width: 380px;
    padding:5px;
}

.btn .combobox-clear {
    margin-top: 3px;
}
.btn .caret {
    margin-top: 3px;
}
.nameContainer.textbox #commonName {
    width: 282px;
}
.nameContainer .combobox-container {
    left: 282px;
}
 
.observation_story .observation_footer {
    margin-top:50px;
}
.commonName {
    width:282px !important;
}
</style>
</head>
<body>

<link rel="stylesheet" href="/${grailsApplication.metadata['app.name']}/js/galleria/1.4.2/themes/classic/galleria.classic.css">
<script src="/${grailsApplication.metadata['app.name']}/js/galleria/1.4.2/galleria.1.4.2-youtubeV3.js"></script>
<script src="/${grailsApplication.metadata['app.name']}/js/galleria/1.4.2/themes/classic/galleria.classic.min.js"></script>

            <div class="observation  span12">
                            <obv:showSubmenuTemplate/>
                            
                        <g:if test="${observationInstance}">
                        <g:set var="featureCount" value="${observationInstance.featureCount}"/>
                        <g:set var="obvLock" value="${observationInstance.isLocked}"/>
                            </g:if>
                            
                        <div class="page-header clearfix ">
                                    <div style="width:100%;">
                                        <div class="main_heading" style="margin-left:0px; position:relative">
                                            <span class="badge ${(featureCount>0) ? 'featured':''}" style="left:-50px"  title="${(featureCount>0) ? g.message(code:'text.featured'):''}">
                                            </span>

                                            <div class="pull-right">
                                                <sUser:ifOwns model="['user':observationInstance.author]">
                                                
                                                <a class="btn btn-primary pull-right" style="margin-right: 5px;"
                                                   href="${uGroup.createLink(controller:'observation', action:'edit', id:observationInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
                                                    <i class="icon-edit"></i><g:message code="button.edit" /></a>

                                                <a class="btn btn-danger btn-primary pull-right" style="margin-right: 5px;"
                                                    href="#"
                                                    onclick="return deleteObservation();"><i class="icon-trash"></i><g:message code="button.delete" /></a>
                                                <form action="${uGroup.createLink(controller:'observation', action:'flagDeleted')}" method='POST' name='deleteForm'>
                                                    <input type="hidden" name="id" value="${observationInstance.id}" />
                                                </form>
 
                                                </sUser:ifOwns>

                                            </div>
                                           <obv:showSpeciesName
                                            model="['observationInstance':observationInstance, 'isHeading':true]" />


                                        </div>
                                    </div>
                                    <div style="clear:both;"></div>
                               </div>
                               
                               <div class="span12" style="margin-left:0px">
                                   <g:render template="/common/observation/showObservationStoryActionsTemplate"
                                   model="['instance':observationInstance, 'href':canonicalUrl, 'title':title, 'description':description, 'showDetails':true,'hideDownload':true, 'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress, 'userGroup':userGroupInstance, ibpClassification:observationInstance.maxVotedReco?.taxonConcept?.fetchDefaultHierarchy()]" />
                               </div>

                <div class="span8 right-shadow-box" style="margin: 0;">
                <div class="noTitle" style="height:400px;position:relative">
                <div class="story-footer" style="padding: 3px 3px;right:0;bottom:372px;z-index:5;background-color:whitesmoke" >
                <g:render template="/common/observation/noOfResources" model="['instance':observationInstance, 'bottom':'bottom:55px;']"/>
                </div>
                <center>
                    <div id="gallerySpinner" class="spinner">
                        <r:img uri="${grailsApplication.config.grails.serverURL}/images/spinner.gif" width="20" height="20"/>
                    </div>
                </center>
                                      
                    <div id="gallery1" style="visibility:hidden; margin-top: 60px;">
                         <% def audioResource = 0 
                            def audioCount    = 0
                            def imageCount = 0
                            def observationInstanceListResources  %>
                        <g:if test="${observationInstance.resource}">
                            <%  
                                observationInstanceListResources = observationInstance.listResourcesByRating()
                            %>
                            <g:each in="${observationInstanceListResources}" var="r">
                                <g:if test="${r.type == ResourceType.IMAGE}">
                                
                                <% imageCount += 1
                                def gallImagePath = ImageUtils.getFileName(r.fileName.trim(), ImageType.LARGE)%>
                                <%def gallThumbImagePath = ImageUtils.getFileName(r.fileName.trim(), ImageType.SMALL)%>
                                <a target="_blank"
                                    rel="${createLinkTo(file: gallImagePath, base:grailsApplication.config.speciesPortal.observations.serverURL)}"
                                    href="${createLinkTo(file: gallImagePath, base:grailsApplication.config.speciesPortal.observations.serverURL)}">
                                                                    
                                    <img class="galleryImage"
                                    src="${createLinkTo(file: gallThumbImagePath, base:grailsApplication.config.speciesPortal.observations.serverURL)}" 
                                    data-original="${createLinkTo(file: r.fileName.trim(), base:grailsApplication.config.speciesPortal.observations.serverURL)}" 
                                    title="${r?.description}" /> </a>

                                <g:imageAttribution model="['resource':r, base:grailsApplication.config.speciesPortal.observations.serverURL]" />
                                </g:if>
                                <g:elseif test="${r.type == ResourceType.VIDEO}">
                                    <%
                                        imageCount += 1
                                    %> 
                                    <a href="${r.url }"><span class="video galleryImage"><g:message code="link.watch.in.youtube" /></span></a>
                                    <g:imageAttribution model="['resource':r]" />
                                </g:elseif>
                                <g:elseif test="${r.type == ResourceType.AUDIO}">                                                                    
                                    <% audioCount = audioCount +1 %>
                                </g:elseif>
                            </g:each>
                                                
                        </g:if>
                        <g:else>
                            <img class="galleryImage" style=" ${observationInstance.sourceId? 'opacity:0.7;' :''}"
                            src="${observationInstance.mainImage()?.thumbnailUrl(null, !observationInstance.resource ? '.png' :null, ImageType.LARGE)}" />

                        </g:else>
                        <g:if test="${imageCount == 0 && audioCount != 0}">
                            <r:script>
                                $(".noTitle").hide();
                            </r:script>
                        </g:if>
                    </div>
                    </div>

               
                <g:render template="/species/speciesaudio" model="['resourceInstance': observationInstance , 'resourcesInstanceList' : observationInstanceListResources]"/>


                    <obv:showStory
                        model="['observationInstance':observationInstance, 'showDetails':true, 'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress,'userLanguage':userLanguage]" />

                    
                    <obv:showCustomFields model="['observationInstance':observationInstance]"/>
                     
                        
                    <div class="recommendations sidebar_section" style="overflow:visible;clear:both;">
                        <div>
                            <ul id="recoSummary" class="pollBars recoSummary_${observationInstance.id}">

                            </ul>
                            <div id="seeMoreMessage_${observationInstance.id}" class="message"></div>
                            <div id="seeMore_${observationInstance.id}" class="btn btn-mini"><g:message code="button.show.all" /></div>
                        </div>
                        <div style="width:100%;">
                            <g:hasErrors bean="${recommendationInstance}">
                                <div class="errors">
                                    <g:renderErrors bean="${recommendationInstance}" as="list" />
                                </div>
                            </g:hasErrors>
                            <g:hasErrors bean="${recommendationVoteInstance}">
                                <div class="errors">
                                    <g:renderErrors bean="${recommendationVoteInstance}" as="list" />
                                </div>
                            </g:hasErrors>
                            <form id="addRecommendation" name="addRecommendation"
                                action="${uGroup.createLink(controller:'observation', action:'addRecommendationVote')}"
                                method="GET" class="form-horizontal">
                                <div class="reco-input">
                                <reco:create
                                    model="['recommendationInstance':recommendationInstance]" />
                                    <input type="hidden" name='obvId'
                                            value="${observationInstance.id}" />
                                    
                                     <input type="submit"
                                            value="${g.message(code:'title.value.add')}" class="btn btn-primary btn-small pull-right" style="position: relative; border-radius:4px;right: 4px;" />
                                            <div style="clear:both"></div>
                                </div>
                                
                            </form>
                            <uGroup:showUserGroupsListInModal model="['userGroupInstanceList':observationInstance.userGroups]" />
                        </div>
                        
                                            </div>
                                                                                       
                    <uGroup:objectPostToGroupsWrapper 
                        model="['observationInstance':observationInstance, 'objectType':observationInstance.class.canonicalName]"/>
                    <div class="union-comment">
                    <feed:showAllActivityFeeds model="['rootHolder':observationInstance, feedType:'Specific', refreshType:'manual', 'feedPermission':'editable', 'userLanguage':userLanguage]" />
                    <comment:showAllComments model="['commentHolder':observationInstance, commentType:'super','showCommentList':false, 'userLanguage':userLanguage]" />
                    </div>
                </div>

                                <div class="span4">
                                        <obv:showLocation
                                        model="['observationInstance':observationInstance]" />

                                    <!-- obv:showRating model="['observationInstance':observationInstance]" /-->
                                    <!--  static species content -->

                                    <div class="sidebar_section">
                                        <h5><g:message code="observation.show.related.observations" /> </h5>
                                        <div class="tile" style="clear: both">
                                            <div class="title"><g:message code="observation.show.other.observations" /><span class="item_count"></span></div>
                                            <obv:showRelatedStory
                                            model="['observationInstance':observationInstance, 'observationId': observationInstance.id, 'controller':'observation', 'action':'related','filterProperty': 'speciesName', 'id':'a','userGroupInstance':userGroupInstance]" />
                                        </div>
                                        <div class="tile">
                                            <div class="title"><g:message code="text.observations.nearby" /></div>
                                            <obv:showRelatedStory
                                            model="['observationInstance':observationInstance, 'observationId': observationInstance.id, 'controller':'observation', 'action':'related', 'filterProperty': 'nearByRelated', 'id':'nearBy', 'userGroupInstance':userGroupInstance]" />
                                        </div>
                                        
                                    </div>
                                    <%
                                    def annotations = observationInstance.fetchChecklistAnnotation()
                                    %>
                                    <g:if test="${annotations?.size() > 0}">
                                    <div class="sidebar_section">
                                        <h5><g:message code="heading.annotations" /></h5>
                                        <div>
                                            <obv:showAnnotation model="[annotations:annotations]" />
                                        </div>
                                    </div>  
                                    </g:if>
                                    
 				
                                    
                                    <%--                    <div class="sidebar_section">--%>
                                        <%--                        <h5>Top 5 Contributors of ${observationInstance.group.name}</h5>--%>
                                        <%--                        <chart:showStats model="['title':'Top 5 Contributors', statsType:ChartService.USER_OBSERVATION_BY_SPECIESGROUP,  speciesGroupId:observationInstance.group.id, hAxisTitle:'User', hideBarChart:true, width:300, hideTitle:true]"/>--%>
                                        <%--                    </div>--%>
                                    <!-- obv:showTagsSummary model="['observationInstance':observationInstance]" /-->
                                    <!-- obv:showObvStats  model="['observationInstance':observationInstance]"/-->

                                </div>


                            </div>
<script type="text/javascript">
$(document).ready(function(){
    window.params.observation.getRecommendationVotesURL = "${uGroup.createLink(controller:'observation', action:'getRecommendationVotes',userGroupWebaddress:params.webaddress) }";
});
</script>


    <r:script>
    
   
    var observationId = ${observationInstance.id};
    $(document).ready(function(){
<%--        initRelativeTime("${uGroup.createLink(controller:'activityFeed', action:'getServerTime')}");--%>
<%--        dcorateCommentBody($('.yj-message-body')); --%>

        $("#seeMoreMessage").hide();
        $(".readmore").readmore({
            substr_len : 400,
            more_link : '<a class="more readmore">&nbsp;More</a>'
        });
        
        $('#gallery1').galleria({
            height : 400,
            preload : 1,
            lightbox: false,
            carousel : true,
            transition : 'pulse',
            image_pan_smoothness : 5,
            showInfo : true,
            dataSelector : ".galleryImage",
            debug : false,
            thumbQuality : false,
            maxScaleRatio : 1,
            minScaleRatio : 1,
            _toggleInfo: false,
            thumbnails:true,
            showCounter:true,
            idleMode:false,
            youtube : {
                            modestbranding: 1,
                            autohide: 1,
                            color: 'white',
                            hd: 1,
                            rel: 0,
                            showinfo: 1
            },
            dataConfig : function(img) {
                            return {
                                // tell Galleria to grab the content from the .desc div as caption
                                description : $(img).parent().next('.notes').html(),
                                _biodiv_url:$(img).data('original')
                            };
            },
            extend : function(options) {
                            this.bind('image', function(e) {
                                $(e.imageTarget).click(this.proxy(function() {
                                    window.open(Galleria.get(0).getData()._biodiv_url);
                                    //this.openLightbox();
                                }));
                            });
                            
                            this.bind('loadfinish', function(e){
                                galleryImageLoadFinish();
                                var galleriaInfo = $(".galleria-info");
                                galleriaInfo.css('cssText', 'top : 350px !important');
                                var galleriaSlideUp = $(".galleria-info .slideUp");
                                galleriaSlideUp.trigger('click');
                            });

                            this.bind('lightbox_image', function(e){
                                //$(".galleria-lightbox-title").append('<a target="_blank" href="'+Galleria.get(0).getData()._biodiv_url+'"><g:message code="show.view.full.image" /> </a>');
                            })

                        }
                });
                Galleria.ready(function() {
                    
                    $("#gallerySpinner").hide();
                    $("#gallery1").css('visibility', 'visible');
                    //$(".galleria-thumbnails-container").hide();

                });
    

        
        $('#voteCountLink').click(function() {
            $('#voteDetails').show();
        });

        $('#voteDetails').mouseout(function(){
            $('#voteDetails').hide();
        });
                        
        $("ul[name='tags']").tagit({select:true,  tagSource: "${uGroup.createLink(controller:params.controller, action: 'tags')}"});
         
        $(".view_obv_tags li.tagit-choice").click(function(){
            var tg = $(this).contents().first().text();
            window.location.href = "${uGroup.createLink(controller:'observation', action: 'list')}?tag=" + tg ;
         });
         
             
        $('#addRecommendation').bind('submit', function(event) {
            $(this).ajaxSubmit({ 
                url:"${uGroup.createLink(controller:'observation', action:'addRecommendationVote')}",
                dataType: 'json', 
                type: 'GET',
                beforeSubmit: function(formData, jqForm, options) {
                    updateCommonNameLanguage();
                    return true;
                }, 
                success: function(data, statusText, xhr, form) {
                    if(data.status == 'success' || data.success == true) {
                        if(data.canMakeSpeciesCall === 'false'){
                            $('#selectedGroupList').modal('show');
                        } else{                             
                            preLoadRecos(3, 0, false,observationId);
                            updateUnionComment(null, "${uGroup.createLink(controller:'comment', action:'getAllNewerComments')}");
                            updateFeeds();
                            setFollowButton();
                            showUpdateStatus(data.msg, data.success?'success':'error');
                        }
                    } else {
                        showUpdateStatus(data.msg, data.success?'success':'error');
                    }
                    $("#addRecommendation")[0].reset();
                    $("#canName").val("");
                    return false;
                },
                error:function (xhr, ajaxOptions, thrownError){
                    //successHandler is used when ajax login succedes
                    var successHandler = this.success, errorHandler = showUpdateStatus;
                    handleError(xhr, ajaxOptions, thrownError, successHandler, errorHandler);
                } 
            });
            event.preventDefault();
        });
        
        $(".nav a.disabled").click(function() {
            return false;
        })

        $("#seeMore").click(function(){
            preLoadRecos(-1, 3, true,observationId);
        });

        preLoadRecos(3, 0, false,observationId);
        //loadObjectInGroups();
        var obvLock = ${obvLock};
        if(obvLock){
            showUpdateStatus('This species ID has been validated by a species curator and is locked!', 'success');
            $('.nameContainer input').attr("disabled", "disabled");
            $('.iAgree button').addClass("disabled");
        }
        else{
            $('.nameContainer input').removeAttr("disabled");
            $('.iAgree button').removeClass("disabled");
        } 
        initializeLanguage(); 
         $(".CustomField_multiselectcombo").multiselect();

         // For Open Tag

        $('.add_obv_tags').click(function(){
            $('.view_obv_tags, .add_obv_tags').hide();
            $('.add_obv_tags_wrapper').show();
        });

        $('.cancel_open_tags').click(function(){
            $('.view_obv_tags, .add_obv_tags').show();
            $('.add_obv_tags_wrapper').hide();
        });

         $('#addOpenTags').bind('submit', function(event) {

                 $(this).ajaxSubmit({ 
                    url: "${uGroup.createLink(controller:'observation', action:'updateOraddTags')}",
                    dataType: 'json', 
                    type: 'GET',                
                    success: function(data, statusText, xhr, form) {
                        console.log("data "+data +" statusText = "+statusText+" xhr = "+xhr+" form = "+form);
                        console.log(data);
                        var tagsData = data;
                        if(tagsData.success){
                            var outHtml = '';
                            console.log(tagsData.hasOwnProperty("model"));
                            //console.log(Object.keys(data.model).length);
                            if(tagsData.hasOwnProperty("model")){
                                if(Object.keys(data.model).length > 0){
                                    $.each(data.model, function( index, value ) {
                                        outHtml+= '<li class="tagit-choice" style="padding:0 5px;">';
                                        outHtml+= index;
                                        outHtml+= '&nbsp;<span class="tag_stats">'+value +'</span>';
                                        outHtml+= '</li>';
                                    });
                                    $('.tagitAppend').html(outHtml);                        
                                    $('.view_obv_tags, .add_obv_tags').show();
                                    $('.add_obv_tags_wrapper').hide();                                    
                                }
                            }else{
                                $('.tagitAppend').empty();
                                $('.view_obv_tags, .add_obv_tags_wrapper').hide();
                                $('.add_obv_tags').show();
                            }
                            updateFeeds();
                        }
                        return false;
                    },
                    error:function (xhr, ajaxOptions, thrownError){
                        //successHandler is used when ajax login succedes
                        var successHandler = this.success, errorHandler = showUpdateStatus;
                        handleError(xhr, ajaxOptions, thrownError, successHandler, errorHandler);
                    } 

                 });    
               
            event.preventDefault(); 
        });
        $(".obvCreateTags").tagit({
            select:true, 
            allowSpaces:true, 
            placeholderText:$(".obvCreateTags").attr('rel'),//'Add some tags',
            fieldName: 'tags', 
            autocomplete:{
                source: '/observation/tags'
            }, 
            triggerKeys:['enter', 'comma', 'tab'], 
            maxLength:30
        });

        /* Added for  Species Update*/
        var group_icon = $('.group_icon_show');
        var group_icon_show_wrap = $('.group_icon_show_wrap');
        //var habitat_icon = $('.habitat_icon_show');
        var label_group = $('label.group');
        var propagateGrpHab = $('.propagateGrpHab');
        $('.propagateGrpHab .control-group  label').hide();

        $('.edit_group_btn').click(function(){            
            group_icon_show_wrap.hide();
            //habitat_icon.hide();
            label_group.hide();
            propagateGrpHab.show();

        });        
   

    $('#updateSpeciesGrp').bind('submit', function(event) {

         $(this).ajaxSubmit({ 
                    url: "${uGroup.createLink(controller:'observation', action:'updateSpeciesGrp')}",
                    dataType: 'json', 
                    type: 'GET',  
                    beforeSubmit: function(formData, jqForm, options) {
                        /*console.log(formData);
                        if(formData.group_id == formData.prev_group){
                            alert("Nothing Changes!");
                            return false;
                        }*/
                    },               
                    success: function(data, statusText, xhr, form) {
                            console.log(data);
                            group_icon.removeClass(data.model.prevgroupIcon).addClass(data.model.groupIcon).attr('title',data.model.groupName);                           
                            group_icon_show_wrap.show();
                            //habitat_icon.show();
                            propagateGrpHab.hide();
                            updateFeeds();
                    },
                    error:function (xhr, ajaxOptions, thrownError){
                        //successHandler is used when ajax login succedes
                        var successHandler = this.success, errorHandler = showUpdateStatus;
                        handleError(xhr, ajaxOptions, thrownError, successHandler, errorHandler);
                    } 

                 });    
               
            event.preventDefault(); 
        });



    });
    function deleteObservation(){
        var test="${message(code: 'default.observatoin.delete.confirm.message', default: 'This observation will be deleted. Are you sure ?')}";

        if(confirm(test)){
            document.forms.deleteForm.submit();
        }
    }

</r:script>


</body>
</html>
