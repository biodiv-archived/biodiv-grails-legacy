
<%@page import="species.utils.Utils"%>
<%@page import="species.utils.ImageType"%>

<div class="observation_story">

	<div>


		<div class="prop">
			<span class="name"><i class="icon-time"></i>Founded on</span>
			<obv:showDate
				model="['userGroupInstance':userGroupInstance, 'propertyName':'foundedOn']" />

		</div>


		<g:if test="${userGroupInstance.description && showDetails}">
			<div class="prop">
				<span class="name"><i class="icon-info-sign"></i>Description</span>
				<div class="notes_view">
					${userGroupInstance.description}
				</div>
			</div>
		</g:if>

	</div>


	<obv:showTagsSummary
		model="['userGroupInstance':userGroupInstance, 'isAjaxLoad':false]" />



	<uGroup:showFooter
		model="['userGroupInstance':userGroupInstance, 'showDetails':showDetails]" />

</div>
