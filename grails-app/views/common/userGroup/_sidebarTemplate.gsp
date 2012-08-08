<%@page import="species.utils.ImageType"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>

<div class="span4 sidebar left-sidebar">

	<div class="super-section">
		<div class="section">
			<h5>
				<i class="icon-user"></i>Founders
			</h5>
			<g:each in="${userGroupInstance.getFounders(5,0)}" var="userInstance">
				<g:link controller="SUser" action="show" id="${userInstance.id}">
					<img
						style="float: left;" src="${userInstance.icon(ImageType.SMALL)}"
					class="small_profile_pic" title="${userInstance.name}" />	
				</g:link>
			</g:each>
			<g:link controller="userGroup" action="founders" id="${userGroupInstance.id}">...</g:link>
		</div>

		<div class="section">
			<h5>
				<i class="icon-user"></i>Members
			</h5>
			<g:each in="${userGroupInstance.getMembers(5,0)}" var="userInstance">
				<g:link controller="SUser" action="show" id="${userInstance.id}">
					<img
						style="float: left;" src="${userInstance.icon(ImageType.SMALL)}"
						class="small_profile_pic" title="${userInstance.name}" />
				</g:link>
			</g:each>
			<g:link controller="userGroup" action="members" id="${userGroupInstance.id}">...</g:link>
		</div>
	</div>

	<div class="super-section">
		<h3>
				Interested In
		</h3>
		<div class="section">
			<h5>
				<i class="icon-snapshot"></i>Species Groups
			</h5>
			<g:each in="${userGroupInstance.speciesGroups}" var="speciesGroup">
				<g:link controller="userGroup" action="list" params="['sGroup':speciesGroup.id]">
					<button class="btn species_groups_sprites ${speciesGroup.iconClass()}"
					id="${"group_" + speciesGroup.id}" value="${speciesGroup.id}"
					title="${speciesGroup.name}"></button>
				</g:link>
			</g:each>
		</div>

		<div class="section">
			<h5>
				<i class="icon-snapshot"></i>Habitat
			</h5>
			<g:each in="${userGroupInstance.habitats}" var="habitat">
				<g:link controller="userGroup" action="list" params="['habitat':habitat.id]">
					<button class="btn habitats_sprites ${habitat.iconClass()}"
					id="${"habitat_" + habitat.id}" value="${habitat.id}"
					title="${habitat.name}"
					data-content="${message(code: 'habitat.definition.' + habitat.name)}"
					rel="tooltip" data-original-title="A Title"></button>
				</g:link>
			</g:each>
			
		</div>
	</div>
	
	<div class="super-section">
		<div class="section">
			<uGroup:showAllTags
				model="['tagFilterByProperty':'UserGroup' , 'tagFilterByPropertyValue':userGroupInstance, 'isAjaxLoad':true]" />
		</div>
	</div>

	<div class="super-section">
		<div class="section">
			<div class="prop">
				<span class="name"><i class="icon-time"></i>Founded</span>
				<obv:showDate
					model="['userGroupInstance':userGroupInstance, 'propertyName':'foundedOn']" />
			</div>
		</div>
	</div>

	<div class="super-section">
		<div class="section">
			<g:link action="aboutUs" id="${userGroupInstance.id}">More about us here</g:link>
			or<br />
			<g:link action="aboutUs" id="${userGroupInstance.id}" fragment="contactEmail">Contact us here</g:link>
		</div>
	</div>

</div>
