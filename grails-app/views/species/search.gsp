<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.ImageUtils"%>
<%@ page import="species.Species"%>
<%@ page import="species.Language"%>
<%@ page import="species.CommonNames"%>
<html>
<head>

<meta name="layout" content="main" />
<r:require modules="species"/>
<title>Search Species</title>

<r:script>

$(document).ready(function(){
	//$(".readmore").readmore({
	//	substr_len : 300,
	//	more_link : '<a class="more readmore">&nbsp;More</a>'
	//});
	
	$("#removeQueryFilter").live('click', function(){
           	$( "#searchTextField" ).val('');
          	$("#search").click();
           	return false;
    });

});
$( "#search" ).unbind('click');
	
	$( "#search" ).click(function() {
		$("#searchBox").submit();
	});


</r:script>
</head>
<body>
	<div class="container big_wrapper outer_wrapper">
		<s:showSubmenuTemplate/>
		<div class="page-header clearfix">
			<search:searchResultsHeading/>
		</div>

			<g:if test="${flash.message}">
				<div class="message">
					${flash.message}
				</div>
			</g:if>




			<div class="searchResults clearfix">
				<div class="row">
					<!-- main_content -->
					<div class="list span12">
						<div class="observations thumbwrap">

							<div class="controls info-message">

								<div>
									<g:if test="${!total}">
										<search:noSearchResults/>
									</g:if>

									<g:set var="start"
										value="${Integer.parseInt(responseHeader?.params?.start?:'0') }" />
									<g:set var="rows"
										value="${Integer.parseInt(responseHeader?.params?.rows?:'0') }" />

									 <g:if test="${total>0 }">
									<span class="name" style="color: #b1b1b1;"><i
										class="icon-search"></i>
												Showing
												${start+1}-${Math.min(start+rows, total)}
												of
												${total}
										
									</span> species
									<g:if test="${responseHeader?.params?.q}">
									for search key <span class="highlight"> <g:link
												controller="species" action="search"
												params="[query: responseHeader?.params?.q]">
												${responseHeader?.params?.q}
												<a id="removeQueryFilter" href="#">[X]</a>
											</g:link> </span>
									</g:if>
									</g:if>
								</div>
								<g:if test="${total > 0}">
									<ul>
										<g:each in="${speciesInstanceList}" status="i"
											var="speciesInstance">
											<li
												style="list-style: none; margin: 5px; padding: 10px; height: 220px; overflow: hidden; border-bottom: 1px solid #c6c6c6; background-color: #fdfdfd;">

												<div class="figure"
													style="clear: both; float: left; max-height: 220px; max-width: 200px; padding: 10px;">
													<g:link action="show" controller="species"
														id="${speciesInstance.id}">

														<g:set var="mainImage"
															value="${speciesInstance.mainImage()}" />
														<%def thumbnailPath = ImageUtils.getFileName(mainImage?.fileName, ImageType.NORMAL, null)%>

														<span class="wrimg"> <span></span> <g:if
																test="${thumbnailPath }">
																<img
																	src="${createLinkTo( base:grailsApplication.config.speciesPortal.resources.serverURL,
											file: thumbnailPath)}"
																	title="${speciesInstance.taxonConcept.name }" />
															</g:if> <g:else>
																<img class="group_icon" style="opacity: 0.4;"
																	title="${speciesInstance.taxonConcept.name}"
																	src="${createLinkTo(dir:'images', file: speciesInstance.fetchSpeciesGroupIcon(ImageType.NORMAL)?.fileName, absolute:true)}" />
															</g:else> </span>

													</g:link>
												</div>
												<div class="searchSnippet">
													<h6>
														<g:link action="show" controller="species"
															id="${speciesInstance.id}">
															${speciesInstance.taxonConcept.italicisedForm }
														</g:link>
													</h6>
													<%def engCommonName=CommonNames.findByTaxonConceptAndLanguage(speciesInstance.taxonConcept, Language.findByThreeLetterCode('eng'))?.name%>
													<g:if test="${engCommonName}">
														<b class="commonName"> ${engCommonName} </b>
													</g:if>
													<div class="icons">
														<g:collect in="${speciesInstance}"
															expr="${it.fields.resources}" var="resourcesCollection">
															<g:each in="${resourcesCollection}" var="rs">
																<g:each in="${rs}" var="r">
																	<g:if
																		test="${r.type == species.Resource.ResourceType.ICON}">
																		<img class="icon" href="${href}"
																			src="${createLinkTo(dir: 'images/icons', file: r.fileName.trim(), absolute:true)}"
																			title="${r?.description}" />
																	</g:if>
																</g:each>
															</g:each>
														</g:collect>


														<g:each in="${speciesInstance.fetchTaxonomyRegistry()}">
															<span> <a
																class="taxaHierarchy icon ui-icon-control"
																title="${it.key.name}"></a> <%def sortedTaxon = it.value.sort {it.rank} %>
																<div
																	class="ui-corner-all toolbarIconContent attribution"
																	style="display: none;">
																	<a class="ui-icon ui-icon-close" style="float: right;"></a>
																	<g:each in="${sortedTaxon}" var="taxonDefinition">
																		<span class='rank${taxonDefinition.rank} '> ${taxonDefinition.italicisedForm}
																		</span>
																		<g:if test="${taxonDefinition.rank<8}">></g:if>
																	</g:each>
																</div> </span>
														</g:each>


														<img class="group_icon species_group_icon"
															title="${speciesInstance.fetchSpeciesGroup()?.name}"
															src="${createLinkTo(dir: 'images', file: speciesInstance.fetchSpeciesGroupIcon(ImageType.VERY_SMALL).fileName, absolute:true)}" />

														<g:if
															test="${speciesInstance.taxonConcept.threatenedStatus}">
															<s:showThreatenedStatus
																model="['threatenedStatus':speciesInstance.taxonConcept.threatenedStatus]" />
														</g:if>
													</div>

													<div class="readmore">
														<g:set var="summary"
															value="${speciesInstance.findSummary()}"></g:set>
														<g:if test="${summary != null && summary.length() > 300}">
															${summary[0..300] + ' ...'}
														</g:if>
														<g:else>
															${summary?:''}
														</g:else>
													</div>

												</div>
											</li>
										</g:each>
									</ul>
									<div class="paginateButtons" style="clear: both;">
										<g:paginateOnSearchResult total="${total}" action="search"
											params="[query:responseHeader.params.q, fl:responseHeader.params.fl]" />
									</div>
								</g:if>

							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
</body>
</html>
