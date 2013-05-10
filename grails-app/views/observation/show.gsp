<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.participation.Recommendation"%>
<%@ page import="species.participation.RecommendationVote"%>
<%@page import="species.Resource.ResourceType"%>
<%@page import="species.Resource"%>

<html>
<head>
<link rel="canonical" href="${Utils.getIBPServerDomain() + uGroup.createLink(controller:'observation', action:'show', id:observationInstance.id)}" />
<meta property="og:type" content="article" />
<meta property="og:title" content="${(!observationInstance.fetchSpeciesCall()?.equalsIgnoreCase('Unknown'))?observationInstance.fetchSpeciesCall():'Help Identify'}"/>
<meta property="og:url" content="${uGroup.createLink([controller:'observation', action:'show', id:observationInstance.id, base:Utils.getDomainServerUrl(request), 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress])}" />
<g:set var="fbImagePath" value="" />
<%
def r = observationInstance.mainImage();

def thumbnail = r.thumbnailUrl()?:null;
def imagePath = '';
if(r && thumbnail) {
	if(r.type == ResourceType.IMAGE) {
		imagePath = g.createLinkTo(base:grailsApplication.config.speciesPortal.observations.serverURL,	file: thumbnail)
	} else if(r.type == ResourceType.VIDEO){
		imagePath = g.createLinkTo(base:thumbnail,	file: '')
	}
}

%>
<meta property="og:image" content="${imagePath}" />
<meta property="og:site_name" content="${Utils.getDomainName(request)}" />

<g:set var="domain" value="${Utils.getDomain(request)}" />
<g:set var="fbAppId"/>
<%
		
		if(domain.equals(grailsApplication.config.wgp.domain)) {
			fbAppId = grailsApplication.config.speciesPortal.wgp.facebook.appId;
		} else { //if(domain.equals(grailsApplication.config.ibp.domain)) {
			fbAppId =  grailsApplication.config.speciesPortal.ibp.facebook.appId;
		}
		
		//description = observationInstance.notes.trim() ;
		String location = "Observed at '" + (observationInstance.placeName.trim()?:observationInstance.reverseGeocodedName) +"'"
		String desc = "- "+ location +" by "+observationInstance.author.name.capitalize()+" in species group "+observationInstance.group.name + " and habitat "+ observationInstance.habitat.name;
%>
<g:set var="description" value="${observationInstance.description?observationInstance.description+" "+desc:desc }" />

<meta property="fb:app_id" content="${fbAppId }" />
<meta property="fb:admins" content="581308415,100000607869577" />
<meta property="og:description"
          content='${description}'/>
<meta property="og:latitude" content="${observationInstance.latitude}"/>
<meta property="og:longitude" content="${observationInstance.longitude }"/>

<meta name="layout" content="main" />
<r:require modules="observations_show"/>
<link rel="image_src" href="${createLinkTo(file: gallImagePath, base:grailsApplication.config.speciesPortal.observations.serverURL)}" />

<g:set var="entityName"
	value="${message(code: 'observation.label', default: 'Observation')}" />
<title><g:message code="default.show.label" args="[entityName]" />
</title>


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
	left:210px;
}
.observation .union-comment {
	width:99%
}
</style>
</head>
<body>
	
			<div class="observation  span12">
				<obv:showSubmenuTemplate/>

				<div class="page-header clearfix">
					<div style="width:100%;">
						<div class="span8 main_heading" style="margin-left:0px;">
							<obv:showSpeciesName
								model="['observationInstance':observationInstance, 'isHeading':true]" />
						</div>
							<a class="btn btn-success pull-right"
				href="${uGroup.createLink(
						controller:'observation', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"
				class="btn btn-info" style="margin-top: 10px;margin-bottom:-1px; margin-left: 5px;"> <i class="icon-plus"></i>Add an Observation</a>
						<div style="float:right;margin:10px 0;">
							<sUser:ifOwns model="['user':observationInstance.author]">
								
								<a class="btn btn-primary pull-right"
									href="${uGroup.createLink(controller:'observation', action:'edit', id:observationInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
									<i class="icon-edit"></i>Edit</a>
	
									<a class="btn btn-danger btn-primary pull-right" style="margin-right: 5px;margin-bottom:10px;"
										href="${uGroup.createLink(controller:'observation', action:'flagDeleted', id:observationInstance.id)}"
										onclick="return confirm('${message(code: 'default.observatoin.delete.confirm.message', default: 'This observation will be deleted. Are you sure ?')}');"><i class="icon-trash"></i>Delete</a>
										
							</sUser:ifOwns>
						</div>
					</div>
					<div style="clear:both;"></div>
					<g:if test="${params.pos && lastListParams}">
						<div class="nav" style="width:100%;margin-top: 10px;">
							<g:if test="${test}">
								<a class="pull-left btn ${prevObservationId?:'disabled'}" href="${uGroup.createLink([action:"show", controller:"observation", id:prevObservationId, 'pos':params.int('pos')-1, 'userGroupWebaddress':(userGroup?userGroup.webaddress:userGroupWebaddress)])}"><i class="icon-backward"></i>Prev</a>
								<a class="pull-right  btn ${nextObservationId?:'disabled'}"  href="${uGroup.createLink([action:"show", controller:"observation",
									id:nextObservationId, 'pos':params.int('pos')+1, 'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress])}">Next <i style="margin-right: 0px; margin-left: 3px;" class="icon-forward"></i></a>
								<%lastListParams.put('userGroupWebaddress', userGroup?userGroup.webaddress:userGroupWebaddress);
									lastListParams.put('fragment', params.pos);
								 %>
								<a class="btn" href="${uGroup.createLink(lastListParams)}" style="text-align: center;display: block;width: 30pxpx;margin: 0 auto;">List</a>
							</g:if>
							<g:else>
								<a class="pull-left btn ${prevObservationId?:'disabled'}" href="${uGroup.createLink([action:"show", controller:"observation",
									id:prevObservationId, 'pos':params.int('pos')-1, 'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress])}"><i class="icon-backward"></i>Prev</a>
								<a class="pull-right  btn ${nextObservationId?:'disabled'}"  href="${uGroup.createLink([action:"show", controller:"observation",
									id:nextObservationId, 'pos':params.int('pos')+1, 'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress])}">Next<i style="margin-right: 0px; margin-left: 3px;" class="icon-forward"></i></a>
								<%lastListParams.put('userGroupWebaddress', userGroup?userGroup.webaddress:userGroupWebaddress);
								lastListParams.put('fragment', params.pos);	 
								%>
								<a class="btn" href="${uGroup.createLink(lastListParams)}" style="text-align: center;display: block;margin: 0 auto;">List</a>
							</g:else>
						</div>
					</g:if>
                                    </div>
				
				<div class="span8 right-shadow-box" style="margin: 0;">


					<div id="gallery1">
						<g:if test="${observationInstance.resource}">
							<g:each in="${observationInstance.listResourcesByRating()}" var="r">
								<g:if test="${r.type == ResourceType.IMAGE}">
								<%def gallImagePath = r.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.gallery.suffix)%>
								<%def gallThumbImagePath = r.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.galleryThumbnail.suffix)%>
								<a target="_blank"
									rel="${createLinkTo(file: r.fileName.trim(), base:grailsApplication.config.speciesPortal.observations.serverURL)}"
									href="${createLinkTo(file: gallImagePath, base:grailsApplication.config.speciesPortal.observations.serverURL)}">
									<img class="galleryImage"
									src="${createLinkTo(file: gallThumbImagePath, base:grailsApplication.config.speciesPortal.observations.serverURL)}"
									title="${r?.description}" /> </a>

								<g:imageAttribution model="['resource':r]" />
								</g:if>
								<g:elseif test="${r.type == ResourceType.VIDEO}">
									<a href="${r.url }"><span class="video galleryImage">Watch this at YouTube</span></a>
									<g:imageAttribution model="['resource':r]" />
								</g:elseif>
	

                                                                </g:each>
                                                
						</g:if>
						<g:else>
							<img class="galleryImage"
								src="${createLinkTo(file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}"
								title="You can contribute!!!" />

						</g:else>

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
						<div class="input-append">
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
					<div class="union-comment">
					<feed:showAllActivityFeeds model="['rootHolder':observationInstance, feedType:'Specific', refreshType:'manual', 'feedPermission':'editable']" />
					<%
						def canPostComment = customsecurity.hasPermissionAsPerGroups([object:observationInstance, permission:org.springframework.security.acls.domain.BasePermission.WRITE]).toBoolean()
					%>
					<comment:showAllComments model="['commentHolder':observationInstance, commentType:'super', 'canPostComment':canPostComment, 'showCommentList':false]" />
					</div>
				</div>

				<div class="span4">
					
					<div class="sidebar_section">
						<obv:showLocation
							model="['observationInstance':observationInstance]" />
					</div>

					<!-- obv:showRating model="['observationInstance':observationInstance]" /-->
					<!--  static species content -->

					<div class="sidebar_section">
						<h5>Related observations</h5>
						<div class="tile" style="clear: both">
							<div class="title">Other observations of the same species</div>
							<obv:showRelatedStory
								model="['observationInstance':observationInstance, 'observationId': observationInstance.id, 'controller':'observation', 'action':'getRelatedObservation','filterProperty': 'speciesName', 'id':'a','userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress]" />
						</div>
						<div class="tile">
							<div class="title">Observations nearby</div>
							<obv:showRelatedStory
								model="['observationInstance':observationInstance, 'observationId': observationInstance.id, 'controller':'observation', 'action':'getRelatedObservation', 'filterProperty': 'nearBy', 'id':'nearBy', 'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress]" />
						</div>

					</div>
					
					<g:if test="${observationInstance.userGroups}">
						<div class="sidebar_section">
							<h5>Observation is in groups</h5>
								<!-- div class="title">This observation belongs to following groups</div-->
								<ul class="tile" style="list-style:none; padding-left: 10px;">
									<g:each in="${observationInstance.userGroups}" var="userGroup">
										<li class="">
											<uGroup:showUserGroupSignature  model="[ 'userGroup':userGroup]" />
										</li>
									</g:each>
								</ul>
								<!-- obv:showRelatedStory
									model="['observationInstance':observationInstance, 'observationId': observationInstance.id, 'controller':'userGroup', 'action':'getRelatedUserGroups', 'filterProperty': 'obvRelatedUserGroups', 'id':'relatedGroups']" /-->
						</div>
					</g:if>
					
					<div class="sidebar_section">
						<h5>Actions</h5>
						<div class="tile" style="clear: both">
							<feed:follow model="['sourceObject':observationInstance]" />
						</div>
					</div>	
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
                        thumbnails:false,
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
                                description : $(img).parent().next('.notes').html()
                            };
			},
			extend : function(options) {
                            this.bind('image', function(e) {
                                $(e.imageTarget).click(this.proxy(function() {
                                        this.openLightbox();
                                }));
                            });
                            
                            this.bind('loadfinish', function(e){
                                galleryImageLoadFinish();
                            })
                        }
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
		             	}else{
		             		showRecos(data, null);
		            		updateUnionComment(null, "${uGroup.createLink(controller:'comment', action:'getAllNewerComments')}");
		            		updateFeeds();
		            		setFollowButton();
		            		showRecoUpdateStatus(data.msg, data.status);
		            	}
	            	} else {
         				showRecoUpdateStatus(data.msg, data.status);
         			}
         			$("#addRecommendation")[0].reset();
         			$("#canName").val("");
	            	return false;
	            },
	            error:function (xhr, ajaxOptions, thrownError){
	            	//successHandler is used when ajax login succedes
	            	var successHandler = this.success, errorHandler = showRecoUpdateStatus;
	            	handleError(xhr, ajaxOptions, thrownError, successHandler, errorHandler);
				} 
	     	});
	     	event.preventDefault();
     	});
     	
     	
     	window.fbEnsure(function() {
			FB.Event.subscribe('comment.create', function(response) {
				//console.log(response);
	  			/*FB.api('comments', {'ids': response.href}, function(res) {
	  			 	console.log(res);
			        var data = res[response.href].comments.data;
			        console.log(data);
			        console.log(data.pop().from.name);
			    });*/
	  			$.ajax({
	  				url: "${uGroup.createLink(controller:params.controller, action:'newComment')}",
	  				method:"POST",
	  				dataType:'json',
	  				data:{'obvId':${observationInstance.id}, 'href':response.href, 'commentId':response.commentID},
					error: function (xhr, status, thrownError){
						console.log("Error while callback to new comment"+xhr.responseText)
					}
				});
			});
			
			FB.Event.subscribe('comment.remove', function(response) {
	  			//console.log(response);
	  			$.ajax({
	  				url: "${uGroup.createLink(controller:params.controller, action:'removeComment')}",
	  				method:"POST",
	  				data:{'obvId':${observationInstance.id}, 'href':response.href, 'commentId':response.commentID},
					error: function (xhr, status, thrownError){
						console.log("Error while callback to remove comment"+xhr.responseText)
					}
				});
			});
			
			if('${params.postToFB}' === 'on') {
				FB.ui(
				  {
				    method: 'feed',
				    name: "${(!observationInstance.fetchSpeciesCall()?.equalsIgnoreCase('Unknown'))?observationInstance.fetchSpeciesCall():'Help Identify'}",
				    link: "${uGroup.createLink(controller:'observation', action:'show', id:observationInstance.id, base:Utils.getDomainServerUrl(request), 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress)}",
				    picture: "${createLinkTo(file: fbImagePath, base:grailsApplication.config.speciesPortal.observations.serverURL)}",
				    caption: "${Utils.getDomainName(request)}",
				    description: "${description.replaceAll("\\n", " ")}"
				  },
				  function(response) {
				    if (response && response.post_id) {
				      //alert('Post was published.');
				    } else {
				      //alert('Could not published to the FB wall.');
				    }
				  }
				);
			}
		});
		
		$(".nav a.disabled").click(function() {
			return false;
		})

                $("#seeMore").click(function(){
                    preLoadRecos(-1, 3, true);
                });

                preLoadRecos(3, 0, false);
	});
</r:script>
<g:javascript>
$(document).ready(function(){
    window.params.observation.getRecommendationVotesURL = "${uGroup.createLink(controller:'observation', action:'getRecommendationVotes', id:observationInstance.id, userGroupWebaddress:params.webaddress) }";
});
</g:javascript>

</body>
</html>
