
<%@ page import="species.participation.Observation"%>
<%@ page import="species.participation.Recommendation"%>
<%@ page import="species.participation.RecommendationVote"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'observation.label', default: 'Observation')}" />
<title><g:message code="default.show.label" args="[entityName]" />
</title>
<link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'js/galleria/1.2.6/themes/classic/',file:'galleria.classic.css', absolute:true)}" />

<g:javascript src="jsrender.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}"></g:javascript>

<g:javascript src="galleria/1.2.6/galleria-1.2.6.min.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}" />

</head>
<body>
	<div class="container_16 big_wrapper">
		<div class="observation  grid_16">
			<!--h1>
				<g:message code="default.show.label" args="[entityName]" />
			</h1-->

			<g:if test="${flash.message}">
				<div class="message">
					${flash.message}
				</div>
			</g:if>
			<br />

			<div class="grid_10">
				<div class="grid_10">
					<div id="resourceTabs">
						<ul>
							<li><a href="#resourceTabs-1" style="height: 0px"></a></li>
						</ul>
						<div id="resourceTabs-1">
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
										src="${createLinkTo(dir: 'images/', file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}"
										title="You can contribute!!!" />

								</g:else>

							</div>

						</div>
					</div>

				</div>

				<!--  static species content -->
				<obv:showStory model="['observationInstance':observationInstance]" />

				<div class="grid_10 comments">
					<fb:like send="true" width="450" show_faces="true"></fb:like>
					<div class="fb-comments grid_10"
						data-href="${createLink(controller:'observation', action:'show', id:observationInstance.id)}"
						data-num-posts="10"></div>
				</div>
			</div>


			<div class="grid_5">

				<obv:showLocation
					model="['observationInstance':observationInstance]" />

				<div class="grid_5 recommendations">
					<div class="recoSummary">
						<%
					//TODO:move this piece to taglib and place this code in service
					def result = RecommendationVote.createCriteria().list { 
                    	projections {
                        	groupProperty("recommendation")
                            count 'id', 'voteCount'
                        }
                        eq('observation', observationInstance)
                        order 'voteCount', 'desc'
                    }%>

						<g:if test="${result}">
							<g:message code="recommendations.no.message"
								,  args="[result.size()]" />
							<ul>
								<g:set var="index" value="0" />
								<g:each in="${result}" var="r">
									<li><g:if test="${r[0]?.taxonConcept?.canonicalForm}">
											<g:link controller="species" action="show"
												id="${r[0]?.taxonConcept?.findSpeciesId()}">
												${r[0]?.taxonConcept?.canonicalForm}
											</g:link>
										</g:if> <g:else>
											${r[0].name}
										</g:else> By <%def recoVote = RecommendationVote.withCriteria {
											eq('recommendation', r[0])
											eq('observation', observationInstance)
											min('votedOn')
											
										}
										recoVote = recoVote.getAt(0)
										%> <g:link controller="sUser" action="show"
											id="${recoVote?.author.id}">
											${recoVote?.author.username}
										</g:link> on <g:formatDate format="MMMMM dd, yyyy"
											date="${recoVote?.votedOn}" /> with <g:javascript>
                                                                                    $(document).ready(function(){
                                                                                        $('#voteCountLink_${index}').click(function() {
                                                                                                $('#voteDetails_${index}').show();
                                                                                        });

                                                                                        $('#voteDetails_${index}').mouseout(function(){
                                                                                                $('#voteDetails_${index}').hide();
                                                                                                });
                                                                                    });
                                            </g:javascript> <span
										id="voteCountLink_${index}">
										votes <span
													id="votes_${index}"> ${r[1]} </span></span>
										<span
										style="float: right;">
										<g:remoteLink
												action="addAgreeRecommendationVote" controller="observation"
												params="['obvId':observationInstance.id, 'recoId':r[0].id, 'currentVotes':r[1]]"
												onSuccess="jQuery('#votes_${index}').html(data.votes);return false;"
												onFailure="console.log (XMLHttpRequest); if(XMLHttpRequest.status == 401 || XMLHttpRequest.status == 200) {
	    		show_login_dialog();
	    	} else {	    
	    		alert(errorThrown);
	    	}return false;">I agree</g:remoteLink>
									</span>
									</li>
									<g:set var="index" value="${index + 1}" />
								</g:each>

							</ul>
						</g:if>
						<g:else>
							<g:message code="recommendations.zero.message" />
						</g:else>
						<br />
						<div>
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

							<form id="addRecommendation"
								action="${createLink(controller:'observation', action:'addRecommendationVote')}"
								method="GET">
								<reco:create
									model="['recommendationInstance':recommendationInstance]" />
								<input type="hidden" name='obvId'
									value="${observationInstance.id}" /> <input type="submit"
									value="Add" />
							</form>
						</div>
					</div>
				</div>

				<!-- obv:showRating model="['observationInstance':observationInstance]" /-->

			</div>


		</div>
	</div>
	<g:javascript>
	
	Galleria.loadTheme('${resource(dir:'js/galleria/1.2.6/themes/classic/',file:'galleria.classic.min.js', absolute:true)}');
	
	$(document).ready(function(){
		
		$("#resourceTabs").tabs();		
		
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
	});
</g:javascript>

</body>
</html>
