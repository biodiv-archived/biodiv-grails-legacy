<div class="filters" style="position: relative;">
	<% params['isGalleryUpdate'] = true; %>
	<div class="paginateButtons">
		<center>
			<p:paginateOnAlphabet controller="species"
				action="${params.action?:'list' }" total="${instanceTotal}"
				userGroup="${userGroup }" userGroupWebaddress="${userGroupWebaddress}" 
				params="${params}"/>
		</center>
	</div>

	<div class="btn-group pull-right" style="z-index: 10">
		<button id="selected_sort" class="btn dropdown-toggle"
			data-toggle="dropdown" href="#" rel="tooltip"
			data-original-title="Sort by">

			<g:if test="${params.sort == 'title'}">
            	Name
            </g:if>
            <g:elseif test="${params.sort == 'lastrevised' || params.sort == 'lastUpdated'}">
				Last Updated
            </g:elseif>
			<g:elseif test="${params.sort == 'score'}">
				Relevancy
            </g:elseif>
			<g:else>
            	Richness
            </g:else>
			<span class="caret"></span>
		</button>
		<ul id="sortFilter" class="dropdown-menu">
			<g:if test="${isSearch}">
				<li class="group_option"><a class=" sort_filter_label"
					value="score"> Relevancy </a>
				</li>
			</g:if>
			<g:else>
				<li class="group_option"><a class=" sort_filter_label"
					value="title"> Name </a></li>
				<li class="group_option"><a class=" sort_filter_label"
					value="percentOfInfo"> Richness </a></li>
				<li class="group_option"><a class=" sort_filter_label"
					value="lastrevised"> Last Updated </a></li>
			</g:else>
		</ul>
	</div>

	<obv:showGroupFilter model="['hideHabitatFilter':true]" />
	
</div>
<obv:showObservationFilterMessage
	model="['instanceTotal':instanceTotal, 'queryParams':queryParams, resultType:'species']" />
