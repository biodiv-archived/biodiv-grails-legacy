

<html>
<head>
<g:set var="title" value="Admin Console"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
</head>
<body>
	<div class="container_16">
		<g:if test="${flash.message}">
			<div
				class="ui-state-highlight ui-corner-all grid_10 prefix_3 suffix_3">
				<span class="ui-icon-info" style="float: left; margin-right: .3em;"></span>
				${flash.message}
			</div>
		</g:if>

		<div class="grid_16">
			<div>
			<ul>
							
			</ul>
			</div>
		</div>
	</div>
</body>
</html>
