<%@page import="species.utils.ImageType"%>

<div class="media signature thumbnail ${showDetails ? '' : 'span3'}" style="margin-left:0px">
	<div class="figure pull-left" style="display:table;height:40px;">
	<a 
		href="${uGroup.createLink(mapping:'userGroup', controller:'userGroup', action:'show', base:userGroup.domainName, 'userGroup':userGroup, 'pos':pos)}">
		<img
		class="media-object ${showDetails ? 'normal_profile_pic' : 'user-icon small_profile_pic'}"
		
		src="${userGroup.mainImage()?.fileName}" title="${userGroup.name}"
		alt="${userGroup.name}" /> </a>
		</div>
	<div class="media-body">
		<div class="media-heading"  style="text-align:left;">
			<a
				href="${uGroup.createLink(mapping:'userGroup', controller:'userGroup', action:'show', base:userGroup.domainName, 'userGroup':userGroup, 'pos':pos)}">
				<span class="ellipsis" title="${userGroup.name}">${userGroup.name}</span> </a>
		</div>


		<!-- Nested media object -->
		<div class="pull-left">
			<g:if test="${!showDetails}">
				<g:link
					url="${uGroup.createLink(mapping:"userGroup", action:"members", absolute:'true', params=['webaddress':userGroup.webaddress])}"
					title="No ofMembers">
					<i class="icon-user"></i>
					${userGroup.getAllMembersCount()}
				</g:link>
			</g:if>
		</div>
	</div>
</div>