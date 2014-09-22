<g:if test="${params.sort=='lastUpdated'}">
	<g:link class="pull-right"
		url="${uGroup.createLink(controller:'project', action:'list', 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }?sort=granteeOrganization">
<g:message code="project.search.sort.by.grantee" />
	</g:link>
</g:if>
<g:else>
	<g:link class="pull-right"

		url="${uGroup.createLink(controller:'project', action:'list', 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }?sort=lastUpdated">
<g:message code="project.search.sort.by.recent" />
	</g:link>

</g:else>

<div id="searchContainer" class="sidebar_section"
	style="left: 0px; margin: 10px 0px; clear: both;">
	<a data-toggle="collapse" data-parent="#searchContainer"
		href="#searchBox" class="collapsed"><h5>
			<i class=" icon-search"></i><g:message code="button.search.filter" />
		</h5> </a>
	<div id="searchBox" class="collapse"
		style="height: 0px; overflow: hidden;">
		<g:render template="/project/advSearchForm" />
	</div>

</div>
