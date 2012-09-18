
<div class="tabbable tabs-left"
	style="margin-left: -163px; float: left;">
	<ul class="nav nav-tabs" style="margin-right: 0px">
		<li class="${(params.action=='list')?'active':'' }"><a
			href="${createLink(action:'list')}">Browse Species</a></li>
		<li class="${(params.action=='taxonBrowser')?'active':'' }"><a
			href="${createLink(action:'taxonBrowser')}">Taxonomy Browser</a>
		</li>
		<li class="${(params.action=='contribute')?'active':'' }"><a
			href="${createLink(action:'contribute')}">Contribute</a>
		</li>
	</ul>
</div>

<g:if test="${entityName}">
	<div class="page-header">
		<h1>
			${entityName}
		</h1>
	</div>
</g:if>

<g:if test="${flash.message}">
	<div class="message alert alert-info">
		${flash.message}
	</div>
</g:if>
<g:hasErrors bean="${speciesInstance}">
	<i class="icon-warning-sign"></i>
	<span class="label label-important"> <g:message
			code="fix.errors.before.proceeding" default="Fix errors" /> </span>
	<%--<g:renderErrors bean="${observationInstance}" as="list" />--%>
</g:hasErrors>
