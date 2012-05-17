<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.participation.Recommendation"%>
<%@ page import="species.participation.RecommendationVote"%>
<html>
<head>
<meta property="og:type" content="article" />
<meta property="og:title" content="${(!observationInstance.maxVotedSpeciesName?.equalsIgnoreCase('Unknown'))?observationInstance.maxVotedSpeciesName:'Help Identify'}"/>
<meta property="og:url" content="${createLink(controller:'observation', action:'show', id:observationInstance.id, base:Utils.getDomainServerUrl(request))}" />
<g:set var="fbImagePath" value="" />
<%
def r = observationInstance.mainImage();
fbImagePath = r.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix)
%>
<meta property="og:image" content="${createLinkTo(file: fbImagePath, base:grailsApplication.config.speciesPortal.observations.serverURL)}" />
<meta property="og:site_name" content="${Utils.getDomainName(request)}" />
<g:set var="description" value="" />
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
		description += "- "+ location +" by "+observationInstance.author.name.capitalize()+" in species group "+observationInstance.group.name + " and habitat "+ observationInstance.habitat.name;
%>

<meta property="fb:app_id" content="${fbAppId }" />
<meta property="fb:admins" content="581308415,100000607869577" />
<meta property="og:description"
          content='${description}'/>
<meta property="og:latitude" content="${observationInstance.latitude}"/>
<meta property="og:longitude" content="${observationInstance.longitude }"/>

<meta name="layout" content="main" />
<link rel="image_src" href="${createLinkTo(file: gallImagePath, base:grailsApplication.config.speciesPortal.observations.serverURL)}" />

<g:set var="entityName"
	value="${message(code: 'observation.label', default: 'Observation')}" />
<title><g:message code="default.show.label" args="[entityName]" />
</title>


<link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'js/galleria/1.2.4/themes/classic/',file:'galleria.classic.css')}" />
<link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'js/jquery/jquery.jcarousel-0.2.8/themes/classic/',file:'skin.css')}" />

<link rel="stylesheet"
	href="${resource(dir:'css',file:'tagit/tagit-custom.css')}"
	type="text/css" media="all" />

<g:javascript src="jsrender.js"></g:javascript>

<g:javascript src="galleria/1.2.4/galleria-1.2.4.min.js"/>

<g:javascript src="tagit.js"></g:javascript>

<g:javascript src="jquery/jquery.jcarousel-0.2.8/jquery.jcarousel.js"/>

<g:javascript src="species/carousel.js"/>

<g:javascript src="species/observations.js"/>


<style>
#nameContainer {
	float: left;
}

#name {
	width: 91%;
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

				<div class="page-header" style="overflow: auto;">
					<div class="span8">
						<h1>
							<obv:showSpeciesName
								model="['observationInstance':observationInstance]" />
						</h1>
					</div>
					<div class="span4" style="margin: 0;">
						<sUser:ifOwns model="['user':observationInstance.author]">
							<a class="btn btn-primary" style="float: right;"
								href="${createLink(controller:'observation', action:'edit', id:observationInstance.id)}">
								Edit Observation </a>

								<a class="btn btn-danger btn-primary" style="float: right; margin-right: 5px;"
									href="${createLink(controller:'observation', action:'flagDeleted', id:observationInstance.id)}"
									onclick="return confirm('${message(code: 'default.observatoin.delete.confirm.message', default: 'This observation will be deleted. Are you sure ?')}');">Delete
									Observation </a>
						</sUser:ifOwns>
					</div>

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
							<div id="seeMore" class="btn btn-mini">see all</div>
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
								
								<reco:create
									model="['recommendationInstance':recommendationInstance]" />
								<input type="hidden" name='obvId'
									value="${observationInstance.id}" /> <input type="submit"
									value="Add" class="btn" style="position: relative;top: -28px;float: right;" />
								
							</form>
						
						</div>
						
					</div>
			    	
			    	<obv:identificationByEmail model="['observationInstance':observationInstance, 'requestObject':request]" />
	            	
					<div class="comments-box sidebar_section" style="clear: both;">
						<fb:comments href="${createLink(controller:'observation', action:'show', id:observationInstance.id, base:Utils.getDomainServerUrl(request))}"
							num_posts="10" width="620" colorscheme="light"  notify="true"></fb:comments>
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
					<!-- obv:showTagsSummary model="['observationInstance':observationInstance]" /-->
					<!-- obv:showObvStats  model="['observationInstance':observationInstance]"/-->

				</div>


			</div>
		</div>
	</div>
	<g:javascript>
	
	Galleria.loadTheme('${resource(dir:'js/galleria/1.2.4/themes/classic/',file:'galleria.classic.min.js')}');
	
	$(document).ready(function(){
		
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
         
         function preLoadRecos(max){
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
					if(uniqueVotes > 3){
						$("#seeMore").show();
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
         
         var isSeeAllClicked = false;
         $("#seeMore").click(function(){
         	preLoadRecos(100);
         	$("#seeMore").hide();
		 });
         
         preLoadRecos(3);
         
     	$('#addRecommendation').bind('submit', function(event) {
     		$(this).ajaxSubmit({ 
	         	url:"${createLink(controller:'observation', action:'addRecommendationVote')}",
				dataType: 'json', 
				clearForm: true,
				resetForm: true,
				type: 'GET',
				beforeSubmit: function(formData, jqForm, options) {
					return true;
				}, 
	            success: function(data, statusText, xhr, form) {
	            	showRecos(data, null);
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
	});
	
</g:javascript>

</body>
</html>
