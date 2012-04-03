<%@ page import="species.Species"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html" />
<meta name="layout" content="main" />
<title>Species Edit</title>
<link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'js/galleria/1.2.6/themes/classic/',file:'galleria.classic.css', absolute:true)}" />
<g:javascript src="galleria/1.2.6/galleria-1.2.6.min.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}" />

<g:javascript>
Galleria.loadTheme('${resource(dir:'js/galleria/1.2.6/themes/classic/',file:'galleria.classic.min.js', absolute:true)}');
$(document).ready(function(){
	$(".caption").click(function() {
		$.ajax({
  			url: '${createLink(controller:'species', action:'resources')}',
  			success: function(){
  				$('#gallery1').galleria({
				height : 300,
				preload : 1,
				carousel : true,
				transition : 'pulse',
				image_pan_smoothness : 5,
				showInfo : true,
				dataSource : data,
				debug : false,
				thumbQuality : false,
				maxScaleRatio : 1,
				minScaleRatio : 1
			});
  			}
		});			
	});
	
});
</g:javascript>
</head>
<body>
	<div class="container_16 big_wrapper">
		<div class="grid_16" align="center">
			<g:if test="${flash.message}">
				<div class="message">
					${flash.message}
				</div>
			</g:if>
			<div class="list">
				<table>
					<thead>
						<tr>
							<g:sortableColumn property="id"
								title="${message(code: 'species.id.label', default: 'Id')}" />

							<g:sortableColumn property="title"
								title="${message(code: 'species.title.label', default: 'Title')}" />

							<g:sortableColumn property="percentOfInfo"
								title="${message(code: 'species.percentOfInfo.label', default: 'Percent of Info')}" />

							<g:sortableColumn property="reprImage"
								title="${message(code: 'species.reprImage.label', default: 'Representative Image')}" />

						</tr>
					</thead>
					<tbody>
						<g:each in="${speciesInstanceList}" status="i"
							var="speciesInstance">
							<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

								<td><g:link action="show" id="${speciesInstance.id}">
										${fieldValue(bean: speciesInstance, field: "id")}
									</g:link></td>

								<td><g:link controller="species" action="show"
										id="${speciesInstance.id}">
										${speciesInstance.title}
									</g:link></td>

								<td>
									${fieldValue(bean: speciesInstance, field: "percentOfInfo")}
								</td>

								<td><g:link action="show" id="${speciesInstance.id}">

										<g:set var="mainImage" value="${speciesInstance.mainImage()}" />
										<%def thumbnailPath = mainImage?.fileName?.replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.galleryThumbnail.suffix)%>

										<img class="icon" style="float: right;"
											src="${createLinkTo( base:grailsApplication.config.speciesPortal.resources.serverURL,
											file: thumbnailPath)}"
											title=" ${speciesInstance.taxonConcept.name}" />

										<p class="caption">
											Change
										</p>
									</g:link></td>

							</tr>
						</g:each>
					</tbody>
				</table>
			</div>
			<div id="gallery1"/>
			<div class="paginateButtons">
				<g:paginate total="${speciesInstanceTotal}" />
			</div>
		</div>
	</div>

</body>
</html>
