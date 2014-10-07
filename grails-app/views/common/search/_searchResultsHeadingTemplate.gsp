<style>
.navbar {
	margin-bottom: 0px;
}

.navbar .brand {
	color: #000;
}
</style>
<div class="page-header clearfix">
    <div style="width:100%;">
        <div class="main_heading" style="margin-left:0px;">

            <h1>Search Results</h1>

        </div>
        <obv:showObservationFilterMessage
        model="['observationInstanceList':instanceList, 'observationInstanceTotal':instanceTotal, 'queryParams':queryParams, resultType:'search result']" />
    </div>
</div>
