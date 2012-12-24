<style>
.navbar {
	margin-bottom: 0px;
}

.navbar .brand {
	color: #000;
}
</style>
<div class="page-header clearfix">
<div class="navbar">
	<div class="container">
		<h1 class="brand">
			<g:message code="default.search.heading" default="Search Results" />
		</h1>
	</div>
</div>
</div>

<ul id="searchResultsTabs" class=" nav nav-tabs">
			<li class="${params.controller=='species'?'active':'' }" data-toggle="tab"><a
				href="${uGroup.createLink(controller:'species', action:'search')}"
				data-toggle="tab">Species</a>
			</li>
			<li class="${params.controller=='observation'?'active':'' }" data-toggle="tab"><a
				href="${uGroup.createLink(controller:'observation', action:'search')}"
				data-toggle="tab">Observations</a>
			</li>
			<!-- >li class="${params.controller=='userGroup'?'active':'' }" data-toggle="tab"><a href="${uGroup.createLink(controller:'userGroup', action:'search')}"
				data-toggle="tab">Groups</a>
			</li-->
			<li class="${params.controller=='newsletter'?'active':'' }" data-toggle="tab"><a href="${uGroup.createLink(controller:'newsletter', action:'search')}"
				data-toggle="tab">Pages</a>
			</li>
			<li class="${params.controller=='SUser'?'active':'' }" data-toggle="tab"><a href="${uGroup.createLink(controller:'SUser', action:'search')}"
				data-toggle="tab">Members</a>
			</li>
		
		</ul>
<r:script>
	$(document).ready(function() {
		$('.ellipsis.multiline').trunk8({
		  lines: 2
		});
	});
</r:script>