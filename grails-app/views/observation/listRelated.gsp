
<%@ page import="species.participation.Observation"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'observation.label', default: 'Related Observations')}" />
<title><g:message code="default.list.label" args="[entityName]" />
</title>
<link rel="stylesheet"
	href="${resource(dir:'css',file:'tagit/tagit-custom.css')}"
	type="text/css" media="all" />
<g:javascript src="tagit.js"></g:javascript>
<g:javascript src="jquery/jquery.autopager-1.0.0.js"></g:javascript>
<style>
.observations_list {
	top: 0;
}
</style>
</head>
<body>
	<div class="container outer-wrapper">
		<div class="row">
			<div class="span12">
				<div class="page-header">
					<h1>Related Observations</h1>

				</div>

				<g:if test="${flash.message}">
					<div class="message">
						${flash.message}
					</div>
				</g:if>

				<div>
					<div class="tags_section span3" style="float: right;">
						<obv:showAllTags
							model="['tagFilterByProperty':'Related', 'relatedObvParams':initialParams, 'isAjaxLoad':false]" />
					</div>

					<div class="row">
						<div class="list span9">

							<div class="observations thumbwrap">
								<obv:showSnippet
										model="['observationInstance':parentObservation]"></obv:showSnippet>
								<h5 style="position:relative; top:40px; clear:both">
									<g:if test="${filterProperty == 'nearBy'}">
										Observations nearby
									</g:if>
									<g:elseif test="${filterProperty == 'speciesName'}">
										Observations of same species
									</g:elseif>
								</h5>
								<obv:showObservationsList />
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
<g:javascript>
	$(document).ready(function() {
		$('#tc_tagcloud a').click(function(){
			var tg = $(this).contents().first().text();
			window.location.href = "${g.createLink(controller:'observation', action: 'list')}?tag=" + tg ;
	    	return false;
	 	});
	});
</g:javascript>	
</body>
</html>
