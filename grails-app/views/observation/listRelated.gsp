
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
	href="${resource(dir:'css',file:'tagit/tagit-custom.css', absolute:true)}"
	type="text/css" media="all" />
<g:javascript src="tagit.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}"></g:javascript>
<g:javascript src="jquery.autopager-1.0.0.js"
	base="${grailsApplication.config.grails.serverURL+'/js/jquery/'}"></g:javascript>
	<style>
	.observations_list {
		top:0;
	}
	</style>
</head>
<body>
	<div class="container outer-wrapper">
		<div class="row">
			<div class="span12">
				<div class="page-header">
					<h1>
						<g:if test="${filterProperty == 'nearBy'}">
							Observations at near by locations :${parentObservation.location}
						</g:if>
						<g:elseif test="${filterProperty == 'speciesName'}">
							Observations of species : ${parentObservation.maxVotedSpeciesName}
						</g:elseif>
					</h1>
				</div>

				<g:if test="${flash.message}">
					<div class="message">
						${flash.message}
					</div>
				</g:if>

				<div class="span12">
					<div class="tags_section span3" style="float: right;">
						<obv:showAllTags
							model="['tagFilterByProperty':'Related', 'relatedObvParams':initialParams]" />
					</div>

					<div class="row">
						<div class="list span9">

							<div class="observations thumbwrap">
								<div>
									<obv:showSnippet
										model="['observationInstance':parentObservation]"></obv:showSnippet>
								</div>
								<div  style="clear: both;"></div>
								<obv:showObservationsList />
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>
