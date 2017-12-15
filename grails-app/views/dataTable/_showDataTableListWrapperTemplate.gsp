<div class="">

	<!-- main_content -->
	<div class="list" style="margin-left:0px;clear:both">
		<div class="filters" style="position: relative;">
		</div>
		<div class="observation thumbwrap">
			<div class="observation">
				<div>
					<obv:showObservationFilterMessage
						model="['instanceList':instanceList, 'instanceTotal':instanceTotal, 'queryParams':queryParams, resultType:'datatable']" />
						
				</div>
				<div style="clear: both;"></div>
				

                <div class="btn-group pull-left" style="z-index: 10">
                    <button id="selected_sort" class="btn dropdown-toggle"
                        data-toggle="dropdown" href="#" rel="tooltip"
                        data-original-title="${g.message(code:'showobservationlistwrapertemp.sort')}">
                        <g:if test="${params.sort == 'createdOn'}">
                        <g:message code="button.latest" />
                        </g:if>
                        <g:else>
                        <g:message code="button.last.updated" />
                        </g:else>
                        <span class="caret"></span>
                    </button>
                    <ul id="sortFilter" class="dropdown-menu" style="width: auto;">
                        <li class="group_option"><a class=" sort_filter_label"
                            value="createdOn"> <g:message code="button.latest" /> </a></li>
						<li class="group_option"><a class=" sort_filter_label"
							value="lastRevised"> <g:message code="button.last.updated" /> </a></li>
					</ul>


				</div>

				
			</div>
            <div class="span12" style="margin:0px;clear:both;">
                <g:render template="/dataTable/showDataTableListTemplate"/>
            </div>
        </div>
    </div>

	<!-- main_content end -->
</div>

