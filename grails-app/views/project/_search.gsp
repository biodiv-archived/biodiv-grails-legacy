<g:if test="${params.sort=='lastUpdated'}">
	<g:link class="pull-right"
		url="${uGroup.createLink(controller:'project', action:'list', 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }?sort=granteeOrganization">
Sort by Grantee
	</g:link>
</g:if>
<g:else>
	<g:link class="pull-right"

		url="${uGroup.createLink(controller:'project', action:'list', 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }?sort=lastUpdated">
Sort by most recent update
	</g:link>

</g:else>

<div id="searchContainer" class="sidebar_section"
	style="left: 0px; margin: 10px 0px; clear: both;">
	<a data-toggle="collapse" data-parent="#searchContainer"
		href="#searchBox" class="collapsed"><h5>
			<i class=" icon-search"></i>Search/Filter
		</h5> </a>
	<div id="searchBox" class="collapse"
		style="height: 0px; overflow: hidden;">
		<g:render template="/project/searchForm" />
	</div>

</div>