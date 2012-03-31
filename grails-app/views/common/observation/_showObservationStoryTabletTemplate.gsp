
<div class="observation_story tablet">
        
        <h5><obv:showSpeciesName model="['observationInstance':observationInstance]" /></h5>
        <div class="icons-bar">
            <div class="observation-icons">
                    <img class="species_group_icon"
                            src="${createLinkTo(file: observationInstance.group.icon()?.fileName?.trim(), base:grailsApplication.config.speciesPortal.resources.serverURL)}"
                            title="${observationInstance.group?.name}" />

                    <g:if test="${observationInstance.habitat}">
                            <img class="habitat_icon species_group_icon"
                                    src="${createLinkTo(dir: 'group_icons', file:'All.png', base:grailsApplication.config.speciesPortal.resources.serverURL)}"
                                    title="${observationInstance.habitat}" />
                    </g:if>

            </div>

            <div class="user-icon">
                    <a href=/biodiv/SUser/show/${observationInstance.author.id}> <img
                            src="${observationInstance.author.icon()}" class="small_profile_pic"
                            title="${observationInstance.author.username}" /> </a>
            </div>
        </div>

        <div class="stats-box">
            <span><i class="icon-eye-open"></i>${observationInstance.getPageVisitCount()} views</span>
                
            <span><i class="icon-comment"></i><fb:comments-count href="${createLink(controller:'observation', action:'show', id:observationInstance.id, base:grailsApplication.config.grails.domainServerURL)}"></fb:comments-count> comments</span>
        </div>
</div>


