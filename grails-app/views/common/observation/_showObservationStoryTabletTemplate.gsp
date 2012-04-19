
<%@page import="species.utils.Utils"%>
<%@page import="species.utils.ImageType"%>
<div class="observation_story tablet">
        
        <h5><obv:showSpeciesName model="['observationInstance':observationInstance]" /></h5>
        <div class="icons-bar">
            <div class="observation-icons">
                    <img class="group_icon"
                            title="${observationInstance.group?.name}"  
							src="${createLinkTo(dir: 'images', file: observationInstance.group.icon(ImageType.VERY_SMALL)?.fileName?.trim(), absolute:true)}"/>

                    <g:if test="${observationInstance.habitat}">
                           <img class="habitat_icon group_icon"
                                    title="${observationInstance.habitat.name}" 
                                    src='${createLinkTo(dir: 'images', file:observationInstance.habitat.icon(ImageType.VERY_SMALL)?.fileName?.trim(), absolute:true)}'/>
                    </g:if>

            </div>

            <div class="user-icon">
                    <a href=/biodiv/SUser/show/${observationInstance.author.id}> <img
                            src="${observationInstance.author.icon()}" class="small_profile_pic"
                            title="${observationInstance.author.username}" /> </a>
            </div>
        </div>

        <div class="story-footer">
            <div class="footer-item"><i class="icon-eye-open"></i>${observationInstance.getPageVisitCount()}</div>
                
            <div class="footer-item"><i class="icon-comment"></i><fb:comments-count href="${createLink(controller:'observation', action:'show', id:observationInstance.id, base:Utils.getDomain(request))}"></fb:comments-count></div>
            
            <g:if test="${observationInstance.flagCount>0}">
				<div id="show-flag-count" class="footer-item"><i class="icon-flag"></i>${observationInstance.flagCount}</div>
			</g:if>
			
        </div>
</div>
