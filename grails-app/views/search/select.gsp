

<%@page import="species.utils.ImageType"%>
<%@ page import="species.Species"%>
<%@ page import="species.Language"%>
<%@ page import="species.CommonNames"%>
<html>
<head>

<meta name="layout" content="main" />

<title>Search Species</title>

<g:javascript>

$(document).ready(function(){
	//$(".readmore").readmore({
	//	substr_len : 300,
	//	more_link : '<a class="more readmore">&nbsp;More</a>'
	//});

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
					<!-- div class="facets grid_2">
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
					</div-->
					<ul class="thumbwrap grid_12" style="margin-top:10px; margin-bottom: 10px;">
						<g:each in="${speciesInstanceList}" status="i"
							var="speciesInstance">
							<li style="list-style: none; margin-left: 0px; height: 220px; overflow: hidden; border-bottom: 1px solid #c6c6c6; background-color: #fdfdfd;">

								<div class="figure"
									style="clear: both; float: left; max-height: 220px; max-width: 200px; padding: 10px;">
									<g:link action="show" controller="species"
										id="${speciesInstance.id}">
										
										<g:set var="mainImage" value="${speciesInstance.mainImage(ImageType.NORMAL)}" />
										<%def thumbnailPath = mainImage?.fileName%>

										<span class="wrimg"> <span></span> <g:if
												test="${thumbnailPath }">
												<img
													src="${createLinkTo( base:grailsApplication.config.speciesPortal.resources.serverURL,
											file: thumbnailPath)}"
													title="${speciesInstance.taxonConcept.name }" />
											</g:if>
											<g:else>
												<img class="group_icon" style="opacity:0.4;"
													title="${speciesInstance.taxonConcept.name}"
													src="${createLinkTo(dir:'images', file: speciesInstance.fetchSpeciesGroupIcon(ImageType.NORMAL)?.fileName, absolute:true)}"/>
											</g:else> </span>

									</g:link>
								</div>
								<div class="searchSnippet">
									<h6>
										<g:link action="show" controller="species"
											id="${speciesInstance.id}">
											${speciesInstance.taxonConcept.italicisedForm }
										</g:link>
									</h6> <%def engCommonName=CommonNames.findByTaxonConceptAndLanguage(speciesInstance.taxonConcept, Language.findByThreeLetterCode('eng'))?.name%>
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
										
		
										<g:each in="${speciesInstance.fetchTaxonomyRegistry()}">
											<span>
											<a  class="taxaHierarchy icon ui-icon-control" title="${it.key.name}"></a>
											<%def sortedTaxon = it.value.sort {it.rank} %>
											<div class="ui-corner-all toolbarIconContent attribution" style="display: none;">
												<a class="ui-icon ui-icon-close" style="float: right;"></a> 
												<g:each in="${sortedTaxon}" var="taxonDefinition">											
														<span class='rank${taxonDefinition.rank} breadcrumb'> ${taxonDefinition.italicisedForm}
														</span>
														<g:if test="${taxonDefinition.rank<8}">></g:if>
												</g:each> 
											</div>
											</span>
										</g:each>
										
										
										<img class="group_icon species_group_icon" 
										  	title="${speciesInstance.fetchSpeciesGroup()?.name}"
										  	src="${createLinkTo(dir: 'images', file: speciesInstance.fetchSpeciesGroupIcon(ImageType.VERY_SMALL).fileName, absolute:true)}"/>
										  
										  <g:if test="${speciesInstance.taxonConcept.threatenedStatus}">
										  		<s:showThreatenedStatus model="['threatenedStatus':speciesInstance.taxonConcept.threatenedStatus]"/>
										  </g:if>
									</div>
	
									<div class="readmore">
									      <g:set var="summary" value="${speciesInstance.findSummary()}"></g:set>
										  <g:if test="${summary != null && summary.length() > 300}">
												${summary[0..300] + ' ...'}
										  </g:if>
										  <g:else>
												${summary?:''}
										  </g:else>
									</div>
	
									<div class="breadcrumb">
										
									</div>
								</div>
							</li>
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
