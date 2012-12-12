<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.ImageUtils"%>
<%@ page import="species.Species"%>
<%@ page import="species.Language"%>
<%@ page import="species.CommonNames"%>
<html>
<head>

<meta name="layout" content="main" />
<r:require modules="species" />
<title>Search Species</title>

<r:script>

$(document).ready(function(){
		
	$("#removeQueryFilter").live('click', function(){
           	$( "#searchTextField" ).val('');
          	$("#search").click();
           	return false;
    });
});
	$( "#search" ).unbind('click');
	
	$( "#search" ).click(function() {
		$("#searchBox").attr("action", '/'+$('#category').val()+'/search');
		$("#searchBox").submit();
	});


</r:script>
</head>
<body>
	<div class="span12">
		<div class="outer_wrapper">
			<s:showSubmenuTemplate />
			<div class="page-header clearfix">
				<search:searchResultsHeading />
			</div>
			<uGroup:rightSidebar />
			<!-- main_content -->
			<div class="list span9" style="margin-left: 0px;">
				<div class="observations thumbwrap">

					<div class="controls info-message">

						<div>
							<g:if test="${!total}">
								<search:noSearchResults />
							</g:if>

							<g:set var="start"
								value="${Integer.parseInt(responseHeader?.params?.start?:'0') }" />
							<g:set var="rows"
								value="${Integer.parseInt(responseHeader?.params?.rows?:'0') }" />

							<g:if test="${total>0 }">
								<span class="name" style="color: #b1b1b1;"><i
									class="icon-search"></i> Showing ${start+1}-${Math.min(start+rows, total)}
									of ${total} </span> species
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
								<g:each in="${speciesInstanceList}" status="i"
									var="speciesInstance">

										<div class="media" style=" margin: 5px; padding: 10px; height: 220px; overflow: hidden; border-bottom: 1px solid #c6c6c6; background-color: #fdfdfd;">
											<a class="pull-left figure" style="max-height: 220px; max-width: 200px;"
												href="${uGroup.createLink(action:'show', controller:'species', id:speciesInstance.id)}">
												<g:set var="mainImage"
													value="${speciesInstance.mainImage()}" /> <%def thumbnailPath = ImageUtils.getFileName(mainImage?.fileName, ImageType.NORMAL, null)%>

												<g:if test="${thumbnailPath }">
													<img class="media-object"
														src="${createLinkTo( base:grailsApplication.config.speciesPortal.resources.serverURL,
											file: thumbnailPath)}"
														title="${speciesInstance.taxonConcept.name }" />
												</g:if> <g:else>
													<img class="group_icon media-object" style="opacity: 0.4;" 
														title="${speciesInstance.taxonConcept.name}"
														src="${createLinkTo(dir:'images', file: speciesInstance.fetchSpeciesGroupIcon(ImageType.NORMAL)?.fileName, absolute:true)}" />
												</g:else> </a>
											<div class="media-body">
												<h6 class="media-heading">
													<g:link class="ellipsis"
														url="${uGroup.createLink(action:'show', controller:'species', id:speciesInstance.id)}">
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
														<div class="dropdown icon">
															<a href="#" class="dropdown-toggle icon taxaHierarchy ellipsis"
																data-toggle="dropdown" title="${it.key.name}"></a>
															<%def sortedTaxon = it.value.sort {it.rank} %>
															<div class="dropdown-menu toolbarIconContent">
																<g:each in="${sortedTaxon}" var="taxonDefinition">
																	<span class='rank${taxonDefinition.rank} '> ${taxonDefinition.italicisedForm}
																	</span>
																	<g:if test="${taxonDefinition.rank<8}">></g:if>
																</g:each>
															</div>
														</div>

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

												<div class="ellipsis multiline">
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
										</div>
								</g:each>
							<div class="paginateButtons" style="clear: both;">
								<center>
									<p:paginateOnSearchResult total="${total}" action="search"
										params="[query:responseHeader.params.q, fl:responseHeader.params.fl]" />
								</center>
							</div>
						</g:if>

					</div>
				</div>
			</div>

		</div>
	</div>

</body>
</html>
