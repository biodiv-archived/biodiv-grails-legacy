
<%@page import="species.utils.Utils"%>
<%@page import="species.utils.ImageType"%>
<div class="observation_story tablet">
        
        <h5><obv:showSpeciesName model="['observationInstance':observationInstance]" /></h5>
        <div class="icons-bar">
            <div class="observation-icons">
                    <span class="group_icon species_groups_sprites active ${observationInstance.group.iconClass()}" title="${observationInstance.group?.name}"></span>

                    <g:if test="${observationInstance.habitat}">
                           <span class="habitat_icon group_icon habitats_sprites active ${observationInstance.habitat.iconClass()}"
                                    title="${observationInstance.habitat.name}" ></span>
                                    
                    </g:if>

            </div>

            <div class="user-icon">
                    <a href=/biodiv/SUser/show/${observationInstance.author.id}> <img
                            src="${observationInstance.author.icon()}" class="small_profile_pic"
                            title="${observationInstance.author.name}" /> </a>
            </div>
        </div>

       <obv:showFooter model="['observationInstance':observationInstance, 'showDetails':showDetails]"/>
</div>
