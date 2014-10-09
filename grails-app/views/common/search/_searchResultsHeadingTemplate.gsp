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
			<g:message code="default.search.heading"  />
		</h1>
	</div>
</div>
</div>

<ul id="searchResultsTabs" class=" nav nav-tabs">
			<li class="${params.controller=='species'?'active':'' }" data-toggle="tab"><a
				href="${uGroup.createLink(controller:'species', action:'search')}"
				data-toggle="tab"><g:message code="default.species.label" /></a>
			</li>
			<li class="${params.controller=='observation'?'active':'' }" data-toggle="tab"><a
				href="${uGroup.createLink(controller:'observation', action:'search')}"
				data-toggle="tab"><g:message code="default.observation.label" /></a>
			</li>
<%--			<li class="${params.controller=='checklist'?'active':'' }" data-toggle="tab"><a href="${uGroup.createLink(controller:'checklist', action:'search')}"--%>
<%--				data-toggle="tab">Checklists</a>--%>
<%--			</li>--%>
			<!-- >li class="${params.controller=='userGroup'?'active':'' }" data-toggle="tab"><a href="${uGroup.createLink(controller:'userGroup', action:'search')}"
				data-toggle="tab">Groups</a>
			</li-->
			<li class="${params.controller=='newsletter'?'active':'' }" data-toggle="tab"><a href="${uGroup.createLink(controller:'newsletter', action:'search')}"
				data-toggle="tab"><g:message code="default.pages.label" /></a>
			</li>
			<li class="${params.controller=='SUser'?'active':'' }" data-toggle="tab"><a href="${uGroup.createLink(controller:'SUser', action:'search')}"
				data-toggle="tab"><g:message code="default.members.label" /></a>
			</li>
		
		</ul>
<r:script>
	$(document).ready(function() {
		var t = "${uGroup.createLink(controller:params.controller?:'search', action:params.action?:'') }";
		$("#searchResultsTabs a[href='"+t+"']").parent().addClass("active");
			
		$('#searchResultsTabs a').click(function (e) {
			updateGallery($(this).attr('href'), undefined, undefined, undefined, false, undefined, true);
			e.preventDefault();
		})
	});
</r:script>
