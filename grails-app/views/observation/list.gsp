<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'observation.label', default: 'Observations')}" />
<title><g:message code="default.list.label" args="[entityName]" />
</title>
<link rel="stylesheet"
	href="${resource(dir:'css',file:'tagit/tagit-custom.css')}"
	type="text/css" media="all" />
<script type="text/javascript"
	src="http://maps.google.com/maps/api/js?sensor=true"></script>
<g:javascript src="location/google/markerclusterer.js"></g:javascript>

<g:javascript src="tagit.js"></g:javascript>
<g:javascript src="jquery/jquery.autopager-1.0.0.js"></g:javascript>
<g:javascript
	src="jquery/jquery-history-1.7.1/scripts/bundled/html4+html5/jquery.history.js" />

</head>
<body>
	<div class="container outer-wrapper">
		<div class="row">
			<div class="span12">
				<div class="page-header clearfix">
						<h1>
							<g:message code="default.observation.heading" args="[entityName]" />
						</h1>
				</div>

				<g:if test="${flash.message}">
					<div class="message alert alert-info">
						${flash.message}
					</div>
				</g:if>

				<obv:showObservationsListWrapper />


			</div>
		</div>
	</div>
	<g:javascript>
		$( "#search" ).unbind('click');
		$( "#search" ).click(function() {          
			var target = "${createLink(action:'search')}" + window.location.search;
			updateGallery(target, ${queryParams.max}, 0, undefined, false);
        	return false;
		});
	</g:javascript>
</body>
</html>
