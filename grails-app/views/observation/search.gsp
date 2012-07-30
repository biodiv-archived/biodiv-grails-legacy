<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<r:require modules="observations_list"/>
<g:set var="entityName"
	value="${message(code: 'observation.label', default: 'Search Results')}" />
<title><g:message code="default.list.label" args="[entityName]" />
</title>
<script type="text/javascript"
	src="http://maps.google.com/maps/api/js?sensor=true"></script>
</head>
<body>
	<div class="container outer-wrapper">
		<div class="row">
			<div class="span12">
				<div class="page-header clearfix">
						<search:searchResultsHeading/>
				</div>

				<g:if test="${flash.message}">
					<div class="message">
						${flash.message}
					</div>
				</g:if>
				
				<div class="searchResults">
					<obv:showObservationsListWrapper />
				</div>


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
</body>
</html>
