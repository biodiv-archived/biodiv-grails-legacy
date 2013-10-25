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
<g:set var="title" value="${(!observationInstance.fetchSpeciesCall()?.equalsIgnoreCase('Unknown'))?observationInstance.fetchSpeciesCall():'Help Identify'}"/>

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
	
String location = "Observed at '" + (observationInstance.placeName.trim()?:observationInstance.reverseGeocodedName) +"'"
String desc = "- "+ location +" by "+observationInstance.author.name.capitalize()+" on "+observationInstance.fromDate.format('dd/MM/yyyy');
%>
<g:set var="description" value="${Utils.stripHTML(observationInstance.notes?observationInstance.notes+' '+desc:desc)?:'' }" />

<g:render template="/common/titleTemplate" model="['title':title, 'description':description, 'canonicalUrl':canonicalUrl, 'imagePath':imagePath, 'videoPath':videoPath]"/>

<r:require modules="observations_show"/>

<style>
.nameContainer {
	float: left;
}


.textbox input{
	text-align: left;
	width: 290px;
	padding:5px;
}

.btn .combobox-clear {
    margin-top: 3px;
}
.btn .caret {
    margin-top: 3px;
}
#commonName {
	width: 200px;
}
.nameContainer .combobox-container {
	left:198px;
}
 
.combobox-container .add-on {
	right: -91px;
}
.observation_story .notes_view {
    margin-bottom:50px;
}

</style>
</head>
<body>
	
			<div class="observation  span12">
                            <obv:showSubmenuTemplate/>
                            
                        <g:if test="${observationInstance}">
                            <g:set var="featureCount" value="${observationInstance.featureCount}"/>
                            </g:if>
                            
                        <div class="page-header clearfix ">
                                    <div style="width:100%;">
                                        <div class="main_heading" style="margin-left:0px; position:relative">
                                            <span class="badge ${observationInstance.group.iconClass()} ${(featureCount>0) ? 'featured':''}" style="left:-50px">
                                            </span>

                                            <div class="pull-right">
                                                <sUser:ifOwns model="['user':observationInstance.author]">
                                                <a class="btn btn-primary pull-right" style="margin-right: 5px;"
                                                   href="${uGroup.createLink(controller:'observation', action:'edit', id:observationInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
                                                    <i class="icon-edit"></i>Edit</a>

                                                <a class="btn btn-danger btn-primary pull-right" style="margin-right: 5px;"
                                                    href="${uGroup.createLink(controller:'observation', action:'flagDeleted', id:observationInstance.id)}"
                                                    onclick="return confirm('${message(code: 'default.observatoin.delete.confirm.message', default: 'This observation will be deleted. Are you sure ?')}');"><i class="icon-trash"></i>Delete</a>

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
                                   model="['instance':observationInstance, 'href':canonicalUrl, 'title':title, 'description':description, 'showDetails':true,'hideDownload':true]" />
                               </div>

				<div class="span8 right-shadow-box" style="margin: 0;">
				<div style="height:400px;position:relative">
                                    <div class="story-footer" style="right:0;bottom:55px;z-index:5;background-color:whitesmoke" >
                                    <g:render template="/common/observation/noOfResources" model="['instance':observationInstance, 'bottom':'bottom:55px;']"/>
                                    </div>
                                    <center>
                                        <div id="gallerySpinner" class="spinner">
                                        <img src="${resource(dir:'images',file:'spinner.gif', absolute:true)}"
                                            alt="${message(code:'spinner.alt',default:'Loading...')}" />
                                        </div>
                                    </center>
                                     
					<div id="gallery1" style="visibility:hidden">

						<g:if test="${observationInstance.resource}">
							<g:each in="${observationInstance.listResourcesByRating()}" var="r">
								<g:if test="${r.type == ResourceType.IMAGE}">
								<%def gallImagePath = ImageUtils.getFileName(r.fileName.trim(), ImageType.LARGE)%>
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
									<a href="${r.url }"><span class="video galleryImage">Watch this at YouTube</span></a>
									<g:imageAttribution model="['resource':r]" />
								</g:elseif>
	

                                                                </g:each>
                                                
						</g:if>
						<g:else>
                                                <img class="galleryImage" style=" ${observationInstance.sourceId? 'opacity:0.7;' :''}"
                                                src="${observationInstance.mainImage()?.thumbnailUrl(null, !observationInstance.resource ? '.png' :null, ImageType.LARGE)}" />
						</g:else>

					</div>
                                </div>
					<obv:showStory
						model="['observationInstance':observationInstance, 'showDetails':true, 'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress]" />
			
					<div class="recommendations sidebar_section" style="overflow:visible;clear:both;">
						<div>
							<ul id="recoSummary" class="pollBars">

							</ul>
							<div id="seeMoreMessage" class="message"></div>
							<div id="seeMore" class="btn btn-mini">Show all</div>
						</div>
						<div class="input-append" style="width:100%;">
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
											value="Add" class="btn btn-primary btn-small pull-right" style="position: relative;top: -30px; border-radius:4px" />
								</div>
								
							</form>
							<uGroup:showUserGroupsListInModal model="['userGroupInstanceList':observationInstance.userGroups]" />
						</div>
						
                                            </div>
                                                                                       
					<uGroup:objectPostToGroupsWrapper 
										model="[canPullResource:canPullResource, 'observationInstance':observationInstance, 'objectType':observationInstance.class.canonicalName]" />

                                        <g:render template="/common/featureWrapperTemplate" model="['observationInstance':observationInstance]"/>

					<div class="union-comment">
					<feed:showAllActivityFeeds model="['rootHolder':observationInstance, feedType:'Specific', refreshType:'manual', 'feedPermission':'editable']" />
					<comment:showAllComments model="['commentHolder':observationInstance, commentType:'super','showCommentList':false]" />
					</div>
				</div>

                                <div class="span4">
                                        <obv:showLocation
                                        model="['observationInstance':observationInstance]" />

                                    <!-- obv:showRating model="['observationInstance':observationInstance]" /-->
                                    <!--  static species content -->

                                    <div class="sidebar_section">
                                        <h5>Related observations</h5>
                                        <div class="tile" style="clear: both">
                                            <div class="title">Other observations of the same species</div>
                                            <obv:showRelatedStory
                                            model="['observationInstance':observationInstance, 'observationId': observationInstance.id, 'controller':'observation', 'action':'getRelatedObservation','filterProperty': 'speciesName', 'id':'a','userGroupInstance':userGroupInstance]" />
                                        </div>
                                        <div class="tile">
                                            <div class="title">Observations nearby</div>
                                            <obv:showRelatedStory
                                            model="['observationInstance':observationInstance, 'observationId': observationInstance.id, 'controller':'observation', 'action':'getRelatedObservation', 'filterProperty': 'nearBy', 'id':'nearBy', 'userGroupInstance':userGroupInstance]" />
                                        </div>
                                        
                                    </div>
                                    <%
                                    def annotations = observationInstance.fetchChecklistAnnotation()
                                    %>
                                    <g:if test="${annotations?.size() > 0}">
                                    <div class="sidebar_section">
                                        <h5>Annotations</h5>
                                        <div>
                                            <obv:showAnnotation model="[annotations:annotations]" />
                                        </div>
                                    </div>	
                                    </g:if>
                                    <%--					<div class="sidebar_section">--%>
                                        <%--						<h5>Top 5 Contributors of ${observationInstance.group.name}</h5>--%>
                                        <%--						<chart:showStats model="['title':'Top 5 Contributors', statsType:ChartService.USER_OBSERVATION_BY_SPECIESGROUP,  speciesGroupId:observationInstance.group.id, hAxisTitle:'User', hideBarChart:true, width:300, hideTitle:true]"/>--%>
                                        <%--					</div>--%>
                                    <!-- obv:showTagsSummary model="['observationInstance':observationInstance]" /-->
                                    <!-- obv:showObvStats  model="['observationInstance':observationInstance]"/-->

                                </div>


                            </div>

	<r:script>
	
	Galleria.loadTheme('${resource(dir:'js/galleria/1.2.7/themes/classic/',file:'galleria.classic.min.js')}');
	
	$(document).ready(function(){
<%--		initRelativeTime("${uGroup.createLink(controller:'activityFeed', action:'getServerTime')}");--%>
<%--		dcorateCommentBody($('.yj-message-body')); --%>

		$("#seeMoreMessage").hide();
		
		$(".readmore").readmore({
			substr_len : 400,
			more_link : '<a class="more readmore">&nbsp;More</a>'
		});
		
		$('#gallery1').galleria({
			height : 400,
                        preload : 1,
                        lightbox: false,
			carousel : false,
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
                            });

                            this.bind('lightbox_image', function(e){
                            	//$(".galleria-lightbox-title").append('<a target="_blank" href="'+Galleria.get(0).getData()._biodiv_url+'">View Full Image</a>');
                            })

                        }
                });
                Galleria.ready(function() {
                    
                    $("#gallerySpinner").hide();
                    $("#gallery1").css('visibility', 'visible');
                    $(".galleria-thumbnails-container").hide();

                });
	

        
        $('#voteCountLink').click(function() {
        	$('#voteDetails').show();
        });

        $('#voteDetails').mouseout(function(){
        	$('#voteDetails').hide();
        });
                        
        $("ul[name='tags']").tagit({select:true,  tagSource: "${uGroup.createLink(controller:params.controller, action: 'tags')}"});
     	 
     	$("li.tagit-choice").click(function(){
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
	            	if(data.status == 'success') {
		             	if(data.canMakeSpeciesCall === 'false'){
		             		$('#selectedGroupList').modal('show');
		             	} else{
		             		showRecos(data, null);
		            		updateUnionComment(null, "${uGroup.createLink(controller:'comment', action:'getAllNewerComments')}");
		            		updateFeeds();
		            		setFollowButton();
		            		showUpdateStatus(data.msg, data.status);
		            	}
	            	} else {
         				showUpdateStatus(data.msg, data.status);
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
                    preLoadRecos(-1, 3, true);
                });

                preLoadRecos(3, 0, false);
                //loadObjectInGroups();
                $(".resource_in_groups li.featured").popover({ 
                    trigger:(is_touch_device ? "click" : "hover"),
                });
                
        });

</r:script>
<g:javascript>
$(document).ready(function(){
    window.params.observation.getRecommendationVotesURL = "${uGroup.createLink(controller:'observation', action:'getRecommendationVotes', id:observationInstance.id, userGroupWebaddress:params.webaddress) }";
});
</g:javascript>

</body>
</html>
