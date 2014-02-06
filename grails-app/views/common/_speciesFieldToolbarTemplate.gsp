
<%@ page import="species.SpeciesField"%>
<!-- field toolbar -->
<g:if test="${speciesFieldInstance}">
	<div style="clear:both;"></div>
	
	<div class="toolbar">

		<div class="span8 pull-left" style="height:40px;margin-left:0px;overflow:hidden">
			<g:set var="ca" value="${speciesFieldInstance?.attributors?:speciesFieldInstance?.contributors }"></g:set>
			<g:if test="${ca}">
				<ul class="tagit" style="list-style:none;display:inline-block;margin-left:0px;width:100%">
					<li class="pull-left"><span class="name" style="color: #b1b1b1;"><i
					class="icon-user"></i> by </span></li>
					<g:each in="${ca}"
						var="attributor">
                                                <g:if test="${attributor}">
						<li class="contributor_ellipsis" title="${attributor.name.trim()}" style="float:none;">${attributor.name.trim()}</li>
                                                </g:if>
					</g:each>
				</ul>
			</g:if>
		</div>
		<div class="span3 pull-right">
                        <obv:rating model="['resource':speciesFieldInstance]"/>
			<g:if test="${speciesFieldInstance?.licenses.size() > 0}">
				<g:each in="${speciesFieldInstance?.licenses}" var="license">
					<a class="license" href="${license?.url}" target="_blank"><img
						class="icon"
						src="${createLinkTo(dir:'images/license', file: license?.name.value().toLowerCase().replaceAll('\\s+','')+'.png', absolute:true)}"
						alt="${license?.name.value()}" /> </a>
				</g:each>
			</g:if>			

			<button type="button" class="btn btn-link pull-right"
				data-toggle="collapse-next" data-target=".helpContent"
				title="About field" style="padding: 2px 2px;">
				<i class="icon-question-sign"></i>
			</button>
			<button type="button" class="btn btn-link pull-right"
				data-toggle="collapse-next" data-target=".attributionContent"
				title="Show details" style="padding: 2px 2px;">
				<i class=" icon-info-sign"></i>
			</button>
			
		</div>
		<div class="clearfix"></div>
		<g:showSpeciesFieldAttribution
			model="['speciesFieldInstance':speciesFieldInstance, 'isContributor':isContributor]" />
                <g:if test="${speciesFieldInstance instanceof SpeciesField}">
        		<g:showSpeciesFieldHelp
                            model="['field':speciesFieldInstance.field]" />
                </g:if>
	</div>
	
</g:if>
