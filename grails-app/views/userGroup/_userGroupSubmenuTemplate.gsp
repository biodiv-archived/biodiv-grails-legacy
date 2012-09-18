
<div class="tabbable tabs-left"
	style="margin-left: -167px; float: left;">
	<ul class="nav nav-tabs" style="margin-right: 0px">
		<sec:ifLoggedIn>
			<li><a href="${createLink(controller:'userGroup', action:'myGroups')}">My
				Groups</a></li>
		</sec:ifLoggedIn>
		
		<li class="${(params.action=='list')?'active':'' }"><a
			href="${createLink(action:'list')}">Browse User Groups</a>
		</li>
		<li class="${(params.action=='create')?'active':'' }"><a
			href="${createLink(action:'create')}">Create a New Group</a></li>
	</ul>
</div>

<g:if test="${entityName}">
	<div class="page-header">
		<h1>
			${entityName}
		</h1>
	</div>
</g:if>


<g:if test="${flash.error}">
	<div class="alertMsg alert alert-error" style="clear: both;">
		${flash.error}
	</div>
</g:if>

<div class="alertMsg ${(flash.message)?'alert':'' }">
	${flash.message}
</div>
