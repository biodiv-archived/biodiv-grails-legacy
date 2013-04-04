
<%@ page import="species.participation.Observation"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.Species"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<r:require modules="observations_list" />
<g:set var="entityName"
	value="${message(code: 'observation.label', default: 'Related Observations')}" />
<title><g:message code="default.list.label" args="[entityName]" />
</title>

<style>
.observations_list {
	top: 0;
}
</style>
<g:javascript>
	$(document).ready(function() {

		window.params = {
			'offset':"${params.offset}",
			'isGalleryUpdate':"${params.isGalleryUpdate}",	
			"tagsLink":"${uGroup.createLink(controller:'observation', action: 'tags')}",
			"queryParamsMax":"${queryParams?.max}",
			'speciesName':"${params.speciesName }",
			'isFlagged':"${params.isFlagged }"
		}
	});
</g:javascript>

</head>
<body>
	<div class="span12">
		<obv:showSubmenuTemplate model="['entityName':'Related Observations']" />

		<div>
			<%--			<div class="tags_section span3" style="float: right;">--%>
			<%--				<obv:showAllTags--%>
			<%--					model="['tagFilterByProperty':'Related', 'relatedObvParams':initialParams, 'isAjaxLoad':false]" />--%>
			<%--			</div>--%>

			<div class="list">

				<div class="observations thumbwrap ">
					<div class="thumbnail clearfix"
						style="background-color: #FFFFFF; display: table; width: 894px;">
						<g:if test="${params.parentType?.equalsIgnoreCase('observation')}">

							<obv:showSnippet
								model="['observationInstance':Observation.get(parentId)]"></obv:showSnippet>

						</g:if>
						<g:elseif test="${params.parentType?.equalsIgnoreCase('species')}">
							<s:showSnippet model="['speciesInstance':Species.read(parentId)]" />
						</g:elseif>
					</div>
					<h5 style="position: relative; top: 40px; clear: both">
						<g:if test="${filterProperty == 'nearBy'}">
										Observations nearby
									</g:if>
						<g:elseif test="${filterProperty == 'speciesName'}">
										Observations of same species
									</g:elseif>
					</h5>
					<div>
						<obv:showObservationsList />
					</div>
				</div>
			</div>
		</div>
	</div>

	<r:script>
	$(document).ready(function() {
		initRelativeTime("${uGroup.createLink(controller:'activityFeed', action:'getServerTime')}");
		$('#tc_tagcloud a').click(function(){
			var tg = $(this).contents().first().text();
			window.location.href = "${uGroup.createLink(controller:'observation', action: 'list')}?tag=" + tg ;
	    	return false;
	 	});
	});
</r:script>
</body>
</html>
