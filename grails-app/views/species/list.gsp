<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>
<%@ page import="species.Species"%>
<%@ page import="species.groups.SpeciesGroup"%>
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
	<div class="container_16">

		<!-- div class="paginateButtons grid_16">
			<center>
				<g:paginateOnSpeciesGroup/>
			</center>
		</div-->

		<div class="paginateButtons grid_16">
			<center>
				<g:paginateOnAlphabet total="${speciesInstanceTotal}" />
			</center>
		</div>
		<br /> <br/><br/>

		<div class="grid_16" align="center">
			<g:set var="columnSize"
				value="${Math.ceil(speciesInstanceList.size()/3)}" />

			<g:each in="${speciesInstanceList}" status="i" var="speciesInstance">

				<g:if test="${i%columnSize == 0}">
					<ul class="speciesList thumbwrap grid_5" style="list-style: none;text-align:left">
				</g:if>
				<li class="grid_5"><g:link action="show"
						id="${speciesInstance.id}">

						<g:set var="mainImage" value="${speciesInstance.mainImage()}" />
						<%def thumbnailPath = mainImage?.fileName?.replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.galleryThumbnail.suffix)%>

						<img class="icon" style="float: right;"
							src="${createLinkTo( base:grailsApplication.config.speciesPortal.resources.serverURL,
											file: thumbnailPath)}" title=" ${speciesInstance.taxonConcept.italicisedForm}"/>
						
						<p class="caption" style="margin-left:50px;"> ${speciesInstance.taxonConcept.italicisedForm}
						</p>
					</g:link>
				</li>
				<g:if test="${(i+1)%columnSize == 0}">
					</ul>
				</g:if>
			</g:each>
			</ul>
		</div>
		<br />

		<div class="paginateButtons  grid_16">
			<center>
				<g:paginate total="${speciesInstanceTotal}"
					params="['startsWith':params.startsWith]" max="50" maxsteps="10" />
			</center>
		</div>
		<br />
		
		<div class="paginateButtons grid_16">
			<center>
				<g:paginateOnAlphabet total="${speciesInstanceTotal}" />
			</center>
		</div>

	</div>
	</div>

</body>
</html>
