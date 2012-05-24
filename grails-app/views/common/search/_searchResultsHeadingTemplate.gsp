<style>
.navbar {
    margin-bottom: 0px;
}

.navbar .brand {
	color: #000;
}
</style>

<div class="navbar">
	<div class="">
		<div class="container">
			<h1 class="brand">
				<g:message code="default.search.heading" default="Search Results" />
			</h1>
			<ul id="searchResultsTabs" class=" nav">
				<li><a
					href="${createLink(controller:'species', action:'search')}"
					data-toggle="tab">Species</a></li>
				<li><a
					href="${createLink(controller:'observation', action:'search')}"
					data-toggle="tab">Observations</a></li>
				<li><a
					href="${createLink(controller:'SUser', action:'search')}"
					data-toggle="tab">Users</a></li>
			</ul>
		</div>
	</div>
</div>