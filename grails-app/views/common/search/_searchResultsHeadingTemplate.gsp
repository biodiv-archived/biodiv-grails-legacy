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

    <div style="width:100%;">
        <obv:showObservationFilterMessage
        model="['observationInstanceList':instanceList, 'observationInstanceTotal':instanceTotal, 'queryParams':queryParams, resultType:'search result']" />
    </div>
</div>
