<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>
<%@ page import="species.Species"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'species.label', default: 'Species')}" />
<title>Species List</title>
<g:javascript>

$(document).ready(function(){
 
	
});
</g:javascript>
</head>
<body>
	<div class="container_12">

		<div class="grid_12">

			<div class="paginateButtons">
				<center>
					<g:paginateOnAlphabet total="${speciesInstanceTotal}" />
				</center>
			</div>
			<br />
			<ul class="thumbwrap" style="width: 100%; text-align: center;">
				<g:each in="${speciesInstanceList}" status="i" var="speciesInstance">

					<li class="figure" style="max-height: 220px;"><g:link
							action="show" id="${speciesInstance.id}">
							<g:set var="mainImage" value="${speciesInstance.mainImage()}" />
							<%def thumbnailPath = mainImage?.fileName?.replaceFirst(/\.[a-zA-Z]{3,4}$/, '_th.jpg')%>
							<span class="wrimg"> <span></span> <g:if
									test="${(new File(grailsApplication.config.speciesPortal.images.rootDir+thumbnailPath)).exists()}">
									<img
										src="${createLinkTo(dir: 'images/resources/', base:grailsApplication.config.speciesPortal.images.serverURL,
											file: thumbnailPath)}" />
								</g:if>
								<g:else>
									<img class="galleryImage"
										src="${createLinkTo(dir: 'images/', file:"no-image.jpg", base:grailsApplication.config.speciesPortal.images.serverURL)}"
										title="You can contribute!!!" />
								</g:else> </span>
						</g:link> <span class='caption'> ${speciesInstance.taxonConcept.italicisedForm}
					</span></li>
				</g:each>
			</ul>
			<br />
			<div class="paginateButtons">
				<center>
					<g:paginateOnAlphabet total="${speciesInstanceTotal}" />
				</center>
			</div>
		</div>
	</div>

</body>
</html>
