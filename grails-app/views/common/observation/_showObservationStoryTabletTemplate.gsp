
<%@page import="species.utils.Utils"%>
<%@page import="species.utils.ImageType"%>
<div class="observation_story tablet">
        
        <h5><obv:showSpeciesName model="['observationInstance':observationInstance, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress]" /></h5>
        <div class="icons-bar">
            <div class="observation-icons">
                    <span class="group_icon species_groups_sprites active ${observationInstance.group.iconClass()}" title="${observationInstance.group?.name}"></span>

                    <g:if test="${observationInstance.habitat}">
                           <span class="habitat_icon group_icon habitats_sprites active ${observationInstance.habitat.iconClass()}"
                                    title="${observationInstance.habitat.name}" ></span>
                                    
                    </g:if>

            </div>

            <div class="user-icon">
            		<g:if test="${userGroup }">
            			<g:set var="userUrl" value="${createLink(mapping:'userGroupModule', controller:'SUser', action:'show', id:observationInstance.author.id, params:['webaddress':userGroup.webaddress])}"/>
            		</g:if>
            		<g:elseif test="${userGroupWebaddress }">
						<g:set var="url" value="${createLink(mapping:'userGroupModule', controller:'SUser', action:'show', id:observationInstance.author.id, params:['webaddress':userGroupWebaddress]) }"/>
					</g:elseif>
            		<g:else>
            			<g:set var="userUrl" value="${createLink(controller:'SUser', action:'show', id:observationInstance.author.id)}"/>
            		</g:else>
                    <a href="${userUrl}"> <img
                            src="${observationInstance.author.icon()}" class="small_profile_pic"
                            title="${observationInstance.author.name}" /> </a>
                    
            </div>
        </div>

       <obv:showFooter model="['observationInstance':observationInstance, 'showDetails':showDetails]"/>
</div>
