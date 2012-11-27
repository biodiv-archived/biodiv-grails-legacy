
<%@page import="species.utils.Utils"%>
<%@page import="species.utils.ImageType"%>
        <h5><obv:showSpeciesName model="['observationInstance':observationInstance, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress]" /></h5>
        <div class="icons-bar clearfix">
            <div class="pull-left">
                    <span class="group_icon species_groups_sprites active ${observationInstance.group.iconClass()}" title="${observationInstance.group?.name}"></span>

                    <g:if test="${observationInstance.habitat}">
                           <span class="habitat_icon group_icon habitats_sprites active ${observationInstance.habitat.iconClass()}"
                                    title="${observationInstance.habitat.name}" ></span>
                                    
                    </g:if>

            </div>

            <div class="user-icon pull-right">
                    <a href="${uGroup.createLink(controller:'SUser', action:'show', id:observationInstance.author.id, 'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress)}"> <img
                            src="${observationInstance.author.icon()}" class="small_profile_pic"
                            title="${observationInstance.author.name}" /> </a>
                    
            </div>
        </div>

       <obv:showFooter model="['observationInstance':observationInstance, 'showDetails':showDetails]"/>
