<div class="filters" style="position: relative;">
	<% params['isGalleryUpdate'] = true; %>
	<div class="paginateButtons">
		<center>
		<p:paginateOnAlphabet controller="species" action="list"
			total="${instanceTotal}" userGroup="${userGroupInstance }" params="${queryParams}"
			userGroupWebaddress="${userGroupWebaddress}"/>
		</center>
	</div>


	<div class="btn-group pull-right" style="z-index: 10">
        <button id="selected_sort" class="btn dropdown-toggle"
			data-toggle="dropdown" href="#" rel="tooltip"
			data-original-title="${g.message(code:'showobservationlistwrapertemp.sort')}">

			<g:if test="${params.sort == 'title'}">
            	<g:message code="default.name.label" />
            </g:if>
            <g:elseif test="${params.sort == 'lastrevised' || params.sort == 'lastUpdated'}">
				<g:message code="button.last.updated" /> 
            </g:elseif>
			<g:elseif test="${params.sort == 'score'}">
				<g:message code="button.relevancy" />
            </g:elseif>
			<g:else>
            	<g:message code="button.last.updated" />
            </g:else>
			<span class="caret"></span>
		</button>
		<ul id="sortFilter" class="dropdown-menu">
			<g:if test="${isSearch}">
				<li class="group_option"><a class=" sort_filter_label"
					value="score"> <g:message code="button.relevancy" /> </a>
				</li>
			</g:if>
			<g:else>
				<li class="group_option"><a class=" sort_filter_label"
					value="title"> <g:message code="default.name.label" /> </a></li>
				<li class="group_option"><a class=" sort_filter_label"
					value="percentOfInfo"> <g:message code="speciesfilter.richness" /> </a></li>
				<li class="group_option"><a class=" sort_filter_label"
					value="lastrevised"> <g:message code="button.last.updated" /> </a></li>
			</g:else>
		</ul>
	</div>
	<div class="btn-group pull-right" style="z-index: 10">
        <button id="has_media" class="btn dropdown-toggle"
			data-toggle="dropdown" href="#" rel="tooltip"
			data-original-title="${g.message(code:'has.media.filter')}">

			<g:if test="${params.hasMedia == 'true'}">
            	<g:message code="with.media" />
            </g:if>
            <g:elseif test="${params.hasMedia == 'false'}">
				<g:message code="without.media" /> 
            </g:elseif>
			<g:else>
            	<g:message code="all.small" />
            </g:else>
			<span class="caret"></span>
		</button>
		<ul id="hasMediaFilter" class="dropdown-menu">
				<li class="hasMediaFilterOptions"><a class=" hasMedia_filter_label"
					value="true"> <g:message code="with.media" /> </a></li>
				<li class="hasMediaFilterOptions"><a class=" hasMedia_filter_label"
					value="false"> <g:message code="without.media" /> </a></li>
				<li class="hasMediaFilterOptions"><a class=" hasMedia_filter_label"
					value="all"> <g:message code="all.small" /> </a></li>
		</ul>
    </div>
	<obv:showGroupFilter model="['hideHabitatFilter':true]" />
	
</div>
<obv:showObservationFilterMessage
	model="['instanceTotal':instanceTotal, 'queryParams':queryParams, resultType:'species']" />
