<%@page import="species.utils.Utils"%>
<%@page import="species.utils.ImageType"%>

<div class="observation_story">
	<div class="observation-icons">
		<span
			class="group_icon species_groups_sprites active ${observationInstance.group.iconClass()}"
			title="${observationInstance.group?.name}"></span>

		<g:if test="${observationInstance.habitat}">
			<span
				class="habitat_icon group_icon habitats_sprites active ${observationInstance.habitat.iconClass()}"
				title="${observationInstance.habitat.name}"></span>
		</g:if>
	</div>

	<dl class="dl-horizontal">
		<dt style="${showDetails?:'width:auto'}">
			<g:if test="${showDetails}">
				<span class="name"><i class="icon-share-alt"></i>Species Name</span>
			</g:if>
			<g:else>
				<i class="pull-left icon-share-alt"></i>
			</g:else>
		</dt>
		<dd style="${showDetails?:'margin-left:0px'}">
			<obv:showSpeciesName
				model="['observationInstance':observationInstance, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress]" />
		</dd>

		<dt style="${showDetails?:'width:auto'}">
			<g:if test="${showDetails}">
				<span class="name"><i class="icon-map-marker"></i>Place</span>
			</g:if>
			<g:else>
				<i class="pull-left icon-map-marker"></i>
			</g:else>
		</dt>
		<dd class="ellipsis" style="${showDetails?:'margin-left:0px'}">
			<g:if test="${observationInstance.placeName == ''}">
				${observationInstance.reverseGeocodedName}
			</g:if>
			<g:else>
				${observationInstance.placeName}
			</g:else>
		</dd>
		<dt style="${showDetails?:'width:auto'}">
			<g:if test="${showDetails}">
				<span class="name"><i class="icon-time"></i>Observed on</span>
			</g:if>
			<g:else>
				<i class="pull-left icon-time"></i>
			</g:else>
		</dt>
		<dd style="${showDetails?:'margin-left:0px'}">
			<time class="timeago"
				datetime="${observationInstance.observedOn.getTime()}"></time>
		</dd>

		<dt style="${showDetails?:'width:auto'}">
			<g:if test="${showDetails}">
				<span class="name"><i class="icon-time"></i>Submitted</span>
			</g:if>
			<g:else>
				<i class="pull-left icon-time"></i>
			</g:else>
		</dt>
		<dd style="${showDetails?:'margin-left:0px'}">
			<time class="timeago"
				datetime="${observationInstance.createdOn.getTime()}"></time>
		</dd>
		<dt style="${showDetails?:'width:auto'}">
			<g:if test="${showDetails}">
				<span class="name"><i class="icon-time"></i>Updated</span>
			</g:if>
			<g:else>
				<i class="pull-left icon-time"></i>
			</g:else>
		</dt>
		<dd style="${showDetails?:'margin-left:0px'}">
			<time class="timeago"
				datetime="${observationInstance.lastRevised?.getTime()}"></time>
		</dd>
	</dl>

	<g:if test="${!showDetails }">
		<div class="prop">
			<i class="pull-left icon-eye-open"></i>
			<div class="value">
				${observationInstance.getPageVisitCount()}
			</div>
		</div>

		<g:if test="${observationInstance.flagCount>0}">
			<div id="show-flag-count" class="prop">
				<i class="pull-left icon-flag"></i>
				<div class="value">
					${observationInstance.flagCount}
				</div>
			</div>
		</g:if>
	</g:if>


	<g:if test="${showDetails}">
		<div>
			<g:if test="${observationInstance.notes}">
				<div class="prop">
					<span class="name"><i class="icon-info-sign"></i>Notes</span>
					<div class="notes_view">
						${observationInstance.notes}
					</div>
				</div>
			</g:if>

		</div>
		<obv:showTagsSummary
			model="['observationInstance':observationInstance, 'isAjaxLoad':false]" />
		<div
			style="display: block; width: 100%; overflow: auto; margin-bottom: 10px;">
			<div style="float: right; clear: both;">
				<sUser:showUserTemplate
					model="['userInstance':observationInstance.author]" />
			</div>
		</div>
	</g:if>
	<obv:showFooter
		model="['observationInstance':observationInstance, 'showDetails':showDetails]" />

</div>
