

<%@ page import="species.Species"%>
<html>
<head>

<meta name="layout" content="main" />
<r:require module="jquery-ui"/>

<title>Search Species</title>
<g:javascript src="readmore/readmore.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}" />
<g:javascript>

$(document).ready(function(){
	
	$(".readmore").readmore({
		substr_len : 400,
		more_link : '<a class="more readmore">&nbsp;More</a>'
	});

});


</g:javascript>
</head>
<body>
	<div class="container_12">

		<div class="searchResults">
			<g:if test="${speciesInstanceList}">

				<div class="grid_12">
					<g:set var="start"
						value="${Integer.parseInt(responseHeader.params.start) }" />
					<g:set var="rows"
						value="${Integer.parseInt(responseHeader.params.rows) }" />
					<div style="float: right; color: #999; text-decoration: underline;">
						Showing
						${start+1}-${Math.min(start+rows, total)}
						of
						${total}
						results

					</div>
					<br />
					<div class="facets grid_2">
						<g:each in="${facets}" var="facet">
							<g:if test="${facet.getValues()}">
								<div class="facet_name">
									${facet.getName()}
								</div>
								<ul>
									<g:each in="${facet.getValues()}" var="facetValue">
										<li><g:link action="select" controller="search" params='[query:facetValue.getName(), fl:responseHeader.params.fl]'>
												${facetValue.getName()}</g:link> (${facetValue.getCount()})
										</li>
									</g:each>
								</ul>
								<br/>
							</g:if>
						</g:each>
					</div>
					<ul class="thumbwrap grid_9">

						<g:each in="${speciesInstanceList}" status="i"
							var="speciesInstance">
							<li style="list-style: none; margin-left: 0px;">

								<div class="figure"
									style="float: left; max-height: 220px; max-width: 200px">
									<g:link action="show" controller="species"
										id="${speciesInstance.id}">
										<g:set var="mainImage" value="${speciesInstance.mainImage()}" />
										<span class="wrimg"> <span></span> <img
											src="${createLinkTo(dir: 'images/', base:grailsApplication.config.speciesPortal.images.serverURL,
											file: mainImage?.fileName.replace('.', '_200x200.'))}" />
										</span>
									</g:link>
								</div>
								<h6>
									<g:link action="show" controller="species"
										id="${speciesInstance.id}">
										${speciesInstance.getName() }
									</g:link>
								</h6> <%def engCommonName=speciesInstance.commonNames?.find{name -> name.language.threeLetterCode.equals('eng')}?.name%>
								<g:if test="${engCommonName}">
									<b class="commonName"> ${engCommonName} </b>
								</g:if>
								<div class="icons">
									<g:collect in="${speciesInstance}"
										expr="${it.fields.resources}" var="resourcesCollection">
										<g:each in="${resourcesCollection}" var="rs">
											<g:each in="${rs}" var="r">
												<g:if test="${r.type == species.Resource.ResourceType.ICON}">
													<img class="icon" href="${href}"
														src="${createLinkTo(dir: 'images/icons', file: r.fileName.trim(), absolute:true)}"
														title="${r?.description}" />
												</g:if>
											</g:each>
										</g:each>
									</g:collect>
								</div>

								<div class="readmore">
									${speciesInstance.findSummary()}
								</div>

								<div class="breadcrumb">
									<%def sortedTaxonReg = speciesInstance.taxonomyRegistry.asList().sort{it.taxonDefinition.rank} %>
									<g:each in="${sortedTaxonReg}" var="taxonReg">
										<g:if
											test="${taxonReg.field.category.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY) }">
											<span class='rank${taxonReg.taxonDefinition.rank}'> ${taxonReg.taxonDefinition.name}
											</span>
											<g:if test="${taxonReg.taxonDefinition.rank<8}">></g:if>

										</g:if>
									</g:each>
								</div>
							</li>
							<hr />
						</g:each>
					</ul>
				</div>
				<div class="paginateButtons" style="clear: both;">
					<g:paginateOnSearchResult total="${total}" action="select"
						params="[query:responseHeader.params.q, fl:responseHeader.params.fl]" />
				</div>

			</g:if>
			<g:else>
				<div style="float: right; color: #999; text-decoration: underline;">No
					search results found. Please refine/relax the search query.</div>
			</g:else>
		</div>
	</div>
</body>
</html>
