<%@page import="species.auth.SUser"%>
<%@ page import="species.dataset.Datasource"%>

<div>
	<!-- main_content -->
	<div class="list">

		<div class="observations thumbwrap">
			<div class="observation">
				<div>
                <obv:showObservationFilterMessage
						model="['observationInstanceList':instanceList, 'instanceTotal':instanceTotal, 'queryParams':queryParams, 'resultType':'datasource']" />
				</div>
				<g:if test="${instanceTotal > 0}">
					<div class="btn-group pull-left" style="z-index: 10">
					</div>
				</g:if>
			</div>

            <g:render template="/datasource/showDatasourceListTemplate"/>

		</div>
	</div>

	<!-- main_content end -->
</div>

