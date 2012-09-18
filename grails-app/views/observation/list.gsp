<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'observation.label', default: 'Observations')}" />
<title><g:message code="default.list.label" args="[entityName]" />
</title>
<script src="http://maps.google.com/maps/api/js?sensor=true"></script>
<r:require modules="observations_list" />
</head>
<body>
	<div class="container outer-wrapper">
		<div class="row">
			<div class="span12">
				<obv:showSubmenuTemplate model="['entityName':entityName]"/>
				<obv:showObservationsListWrapper />
			</div>
		</div>
	</div>
	<g:javascript>
		$(document).ready(function() {
			window.params = {
			<%
				params.each { key, value ->
					println '"'+key+'":"'+value+'",'
				}
			%>
				"tagsLink":"${g.createLink(action: 'tags')}",
				"queryParamsMax":"${queryParams?.max}"
			}
		});
	</g:javascript>
	<r:script>
		$( "#search" ).unbind('click');
		$( "#search" ).click(function() {          
			var target = "${createLink(action:'search')}" + window.location.search;
			updateGallery(target, ${queryParams.max}, 0, undefined, false);
        	return false;
		});
	</r:script>
</body>
</html>
