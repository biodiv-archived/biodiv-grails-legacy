
<%@ page import="species.participation.Observation"%>
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
	href="${resource(dir:'js/galleria/themes/classic/',file:'galleria.classic.css', absolute:true)}" />

<g:javascript src="jsrender.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}"></g:javascript>

<g:javascript src="galleria/galleria-1.2.4.min.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}" />

</head>
<body>
	<div class="container_16">
		<div class="observation  grid_16">
			<h1>
				<g:message code="default.show.label" args="[entityName]" />
			</h1>

			<g:if test="${flash.message}">
				<div class="message">
					${flash.message}
				</div>
			</g:if>

			<div class="grid_10">
				<div id="resourceTabs">
					<ul>
						<li><a href="#resourceTabs-1">Images</a>
						</li>
						<li><a href="#resourceTabs-2">Audio</a>
						</li>
						<li><a href="#resourceTabs-3">Video</a>
						</li>
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
					<div id="resourceTabs-2">There is no audio.</div>
					<div id="resourceTabs-3">There is no video.</div>
				</div>

			</div>
			<!--  static species content -->
			<obv:showStory model="['observationInstance':observationInstance]"/>
			
			<div class="grid_16 recommendations">
			<%
				def recos = RecommendationVote.createCriteria().list { 
					projections {
						groupProperty("recommendation")
						count("id")
					}
					eq('observation', observationInstance)
				}
			 %>
			 <g:if test="${recos}">
			 	<g:message code="recommendations.no.message",  args="[recos.size()]" />
				<g:each in="${recos}" var="reco">
					<reco:show model="['recommendationInstance':reco]"/>
				</g:each>
			</g:if>
			<g:else>
				<g:message code="recommendations.zero.message"/> 
			</g:else>
				<br/>
				<reco:create model="['recommendationInstance':recommendationInstance]"/>
			</div>
		</div>
	</div>
	<g:javascript>
	
	Galleria.loadTheme('${resource(dir:'js/galleria/themes/classic/',file:'galleria.classic.min.js', absolute:true)}');
	
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

	});
</g:javascript>

</body>
</html>
