<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.participation.Recommendation"%>
<%@ page import="species.participation.RecommendationVote"%>
<html>
<head>
<meta property="og:type" content="article" />
<meta property="og:title" content="${observationInstance.title()}"/>
<meta property="og:url" content="${createLink(controller:'observation', action:'show', id:observationInstance.id, base:Utils.getDomainServerUrl(request))}" />
<g:set var="fbImagePath" value="" />
<%
def r = observationInstance.mainImage();
fbImagePath = r.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix)
%>
<meta property="og:image" content="${createLinkTo(file: fbImagePath, base:grailsApplication.config.speciesPortal.observations.serverURL)}" />
<meta property="og:site_name" content="${Utils.getDomainName(request)}" />

<g:set var="domain" value="${Utils.getDomain(request)}" />
<%
		String fbAppId;
		if(domain.equals(grailsApplication.config.wgp.domain)) {
			fbAppId = grailsApplication.config.speciesPortal.wgp.facebook.appId;
		} else if(domain.equals(grailsApplication.config.ibp.domain)) {
			fbAppId =  grailsApplication.config.speciesPortal.ibp.facebook.appId;
		}
		
		//description = observationInstance.notes.trim() ;
		String location = "Observed at '" + (observationInstance.placeName.trim()?:observationInstance.reverseGeocodedName) +"'"
		String desc = "- "+ location +" by "+observationInstance.author.name.capitalize()+" in species group "+observationInstance.group.name + " and habitat "+ observationInstance.habitat.name;
%>
<g:set var="description" value="${desc }" />

<meta property="fb:app_id" content="${fbAppId }" />
<meta property="fb:admins" content="581308415,100000607869577" />
<meta property="og:description"
          content='${description}'/>
<meta property="og:latitude" content="${observationInstance.latitude}"/>
<meta property="og:longitude" content="${observationInstance.longitude }"/>

<meta name="layout" content="main" />
<script src="http://maps.google.com/maps/api/js?sensor=true"></script>
<r:require modules="observations_show"/>
<link rel="image_src" href="${createLinkTo(file: gallImagePath, base:grailsApplication.config.speciesPortal.observations.serverURL)}" />

<g:set var="entityName"
	value="${message(code: 'observation.label', default: 'Observation')}" />
<title><g:message code="default.show.label" args="[entityName]" />
</title>


<style>
#nameContainer {
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

</style>
</head>
<body>
	<div class="container outer-wrapper">
		<div class="row">
			<div class="observation  span12">
				<!--h1>
				<g:message code="default.show.label" args="[entityName]" />
			</h1-->

				<g:if test="${flash.message}">
					<div class="message">
						${flash.message}
					</div>
				</g:if>
				<br />

				<div class="page-header clearfix">
					<div style="width:100%;">
						<div class="span8 main_heading">
							<obv:showSpeciesName
								model="['observationInstance':observationInstance]" />
						</div>
					<div style="float:right;">
						<sUser:ifOwns model="['user':observationInstance.author]">
							
							<a class="btn btn-primary pull-right"
								href="${createLink(controller:'observation', action:'edit', id:observationInstance.id)}">
								Edit Observation </a>

								<a class="btn btn-danger btn-primary pull-right" style="margin-right: 5px;margin-bottom:10px;"
									href="${createLink(controller:'observation', action:'flagDeleted', id:observationInstance.id)}"
									onclick="return confirm('${message(code: 'default.observatoin.delete.confirm.message', default: 'This observation will be deleted. Are you sure ?')}');">Delete
									Observation </a>
									
						</sUser:ifOwns>
					</div>
					</div>
					<div style="clear:both;"></div>
					<g:if test="${params.pos && lastListParams}">
						<div class="nav" style="width:100%;">
							<g:link class="pull-left btn ${prevObservationId?:'disabled'}" action="show" controller="observation"
								id="${prevObservationId}" params="['pos':params.int('pos')-1]">Prev Observation</g:link>
							<g:link class="pull-right  btn ${nextObservationId?:'disabled'}"  action="show" controller="observation"
								id="${nextObservationId}" params="['pos':params.int('pos')+1]">Next Observation</g:link>
							<g:link class="btn" action="${lastListParams.action}" controller="observation" fragment="${params.pos}" params="${lastListParams}" style="text-align: center;display: block;width: 125px;margin: 0 auto;">List Observations</g:link>
							
						</div>
					</g:if>
				</div>
				
				<div class="span8 right-shadow-box" style="margin: 0;">


					<div id="gallery1">
						<g:if test="${observationInstance.resource}">
							<g:each in="${observationInstance.resource}" var="r">

								<%def gallImagePath = r.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.gallery.suffix)%>
								<%def gallThumbImagePath = r.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.galleryThumbnail.suffix)%>
								<a target="_blank"
									rel="${createLinkTo(file: r.fileName.trim(), base:grailsApplication.config.speciesPortal.observations.serverURL)}"
									href="${createLinkTo(file: gallImagePath, base:grailsApplication.config.speciesPortal.observations.serverURL)}">
									<img class="galleryImage"
									src="${createLinkTo(file: gallThumbImagePath, base:grailsApplication.config.speciesPortal.observations.serverURL)}"
									title="${r?.description}" /> </a>

								<g:imageAttribution model="['resource':r]" />
							</g:each>
						</g:if>
						<g:else>
							<img class="galleryImage"
								src="${createLinkTo(file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}"
								title="You can contribute!!!" />

						</g:else>

					</div>



					<obv:showStory
						model="['observationInstance':observationInstance, 'showDetails':true]" />
					
					<div class="recommendations sidebar_section" style="overflow:visible;">
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
								action="${createLink(controller:'observation', action:'addRecommendationVote')}"
								method="GET" class="form-horizontal">
								<div class="reco-input">
								<reco:create
									model="['recommendationInstance':recommendationInstance]" />
									<input type="hidden" name='obvId'
											value="${observationInstance.id}" />
									
									<g:if test="${customsecurity.hasPermissionToMakeSpeciesCall([object:observationInstance,
										permission:org.springframework.security.acls.domain.BasePermission.WRITE]).toBoolean()}">
										 <input type="submit"
											value="Add" class="btn btn-primary btn-small pull-right" style="position: relative;top: -30px; border-radius:4px" />
									</g:if><g:else>
										<a href="#"
											title="Protected to group members/experts. Need to join any of the user groups this observation belongs to inorder to add a species call" class="btn btn-primary btn-small disabled pull-right" style="position: relative;top: -30px;">Join Groups / Be an expert</a>
									</g:else>
								</div>
							</form>
						
						</div>
						
					</div>
			    	
					<div class="union-comment" style="clear: both;">
					<%
						def canPostComment = customsecurity.hasPermissionAsPerGroups([object:observationInstance, permission:org.springframework.security.acls.domain.BasePermission.WRITE]).toBoolean()
					%>
					<comment:showAllComments model="['commentHolder':observationInstance, commentType:'super', 'canPostComment':canPostComment]" />
					
<%--					<customsecurity:isPermittedAsPerGroups object='${observationInstance}'--%>
<%--							permission='${org.springframework.security.acls.domain.BasePermission.WRITE}'--%>
<%--							property='allowNonMembersToComment'>--%>
<%--						<comment:postComment model="['commentHolder':observationInstance, 'rootHolder':observationInstance, commentType:'super']" />--%>
<%--					</customsecurity:isPermittedAsPerGroups>--%>
<%--	--%>
<%--				    <comment:showAllComments model="['commentHolder':observationInstance, commentType:'super']" />--%>
<%--						<fb:comments href="${createLink(controller:'observation', action:'show', id:observationInstance.id, base:Utils.getDomainServerUrl(request))}"--%>
<%--							num_posts="10" width="620" colorscheme="light"  notify="true"></fb:comments>--%>
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
						<div class="sidebar_section tile" style="clear: both">
							<div class="title">Other observations of the same species</div>
							<obv:showRelatedStory
								model="['observationInstance':observationInstance, 'observationId': observationInstance.id, 'controller':'observation', 'action':'getRelatedObservation', 'filterProperty': 'speciesName', 'id':'a']" />
						</div>
						<div class="sidebar_section tile">
							<div class="title">Observations nearby</div>
							<obv:showRelatedStory
								model="['observationInstance':observationInstance, 'observationId': observationInstance.id, 'controller':'observation', 'action':'getRelatedObservation', 'filterProperty': 'nearBy', 'id':'nearBy']" />
						</div>

					</div>
					
					<g:if test="${observationInstance.userGroups}">
					<div class="sidebar_section">
						<h5>Observation is in groups</h5>
						<div class="sidebar_section" style="clear: both">
							<!-- div class="title">This observation belongs to following groups</div-->
							<ul class="thumbnails">
							<g:each in="${observationInstance.userGroups}" var="userGroup">
								<li class="thumbnail"><g:link controller="userGroup" action="show" id="${userGroup.id}"><img class="logo" src="${userGroup.icon().fileName}" title="${userGroup.name}" alt="${userGroup.name}"></g:link></li>
							</g:each>
							</ul>
							<!-- obv:showRelatedStory
								model="['observationInstance':observationInstance, 'observationId': observationInstance.id, 'controller':'userGroup', 'action':'getRelatedUserGroups', 'filterProperty': 'obvRelatedUserGroups', 'id':'relatedGroups']" /-->
						</div>
					</div>
					</g:if>
					
					<!-- obv:showTagsSummary model="['observationInstance':observationInstance]" /-->
					<!-- obv:showObvStats  model="['observationInstance':observationInstance]"/-->

				</div>


			</div>
		</div>
	</div>
	<r:script>
	
	Galleria.loadTheme('${resource(dir:'js/galleria/1.2.7/themes/classic/',file:'galleria.classic.min.js')}');
	
	$(document).ready(function(){
		dcorateCommentBody($('.comment .yj-message-body'));
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
			dataSelector : "img.galleryImage",
			debug : false,
			thumbQuality : false,
			maxScaleRatio : 1,
			minScaleRatio : 1,
	
			dataConfig : function(img) {
				return {
					// tell Galleria to grab the content from the .desc div as caption
					description : $(img).parent().next('.notes').html()
				};
			},
			extend : function(options) {
				// listen to when an image is shown
				this.bind('image', function(e) {
					// lets make galleria open a lightbox when clicking the main
					// image:
					$(e.imageTarget).click(this.proxy(function() {
						this.openLightbox();
					}));
				});
			}
		});

        $('#voteCountLink').click(function() {
        	$('#voteDetails').show();
        });

        $('#voteDetails').mouseout(function(){
        	$('#voteDetails').hide();
        });
                        
        $("ul[name='tags']").tagit({select:true,  tagSource: "${g.createLink(action: 'tags')}"});
     	 
     	$("li.tagit-choice").click(function(){
	    	var tg = $(this).contents().first().text();
	        window.location.href = "${g.createLink(controller:'observation', action: 'list')}?tag=" + tg ;
	     });
         
       
         
         $("#seeMore").click(function(){
         	preLoadRecos(-1, hide);
		 });
         
         preLoadRecos(3, false);
         
     	$('#addRecommendation').bind('submit', function(event) {
     		$(this).ajaxSubmit({ 
	         	url:"${createLink(controller:'observation', action:'addRecommendationVote')}",
				dataType: 'json', 
				clearForm: true,
				resetForm: true,
				type: 'GET',
				beforeSubmit: function(formData, jqForm, options) {
					updateCommonNameLanguage();
					return true;
				}, 
	            success: function(data, statusText, xhr, form) {
	            	showRecos(data, null);
	            	$('#canName').val('');
	            	updateUnionComment(null, "${createLink(controller:'comment', action:'getAllNewerComments')}");
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
	  				url: "${createLink(action:'newComment')}",
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
	  				url: "${createLink(action:'removeComment')}",
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
				    name: "${(!observationInstance.maxVotedSpeciesName?.equalsIgnoreCase('Unknown'))?observationInstance.maxVotedSpeciesName:'Help Identify'}",
				    link: "${createLink(controller:'observation', action:'show', id:observationInstance.id, base:Utils.getDomainServerUrl(request))}",
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
	});
	  function preLoadRecos(max, seeAllClicked){
         	$("#seeMoreMessage").hide();
         	$("#seeMore").hide();
         	
        	$.ajax({
         		url: "${createLink(controller:'observation', action:'getRecommendationVotes', id:observationInstance.id) }",
				method: "POST",
				dataType: "json",
				data: {max:max , offset:0},	
				success: function(data) {
					$("#recoSummary").html(data.recoHtml);
					var uniqueVotes = parseInt(data.uniqueVotes);
					if(uniqueVotes > 3 && !seeAllClicked){
						$("#seeMore").show();
					} else {
						$("#seeMore").hide();
					}
				}, error: function(xhr, status, error) {
	    			handleError(xhr, status, error, undefined, function() {
		    			var msg = $.parseJSON(xhr.responseText);
		    			if(msg.info) {
		    				showRecoUpdateStatus(msg.info, 'info');
		    			}else if(msg.success){
		    				showRecoUpdateStatus(msg.success, 'success');
						} else {
							showRecoUpdateStatus(msg.error, 'error');
						}
					});
			   	}
			});
         }
</r:script>

</body>
</html>
