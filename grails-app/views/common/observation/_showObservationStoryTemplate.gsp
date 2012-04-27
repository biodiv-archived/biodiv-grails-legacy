
<%@page import="species.utils.Utils"%>
<%@page import="species.utils.ImageType"%>

<div class="observation_story">
	<div class="observation-icons">
			<img class="group_icon" title="${observationInstance.group?.name}"
				src="${createLinkTo(dir:'images', file: observationInstance.group.icon(ImageType.VERY_SMALL)?.fileName?.trim(), absolute:true)}" />
	
			<g:if test="${observationInstance.habitat}">
				<img class="habitat_icon group_icon"
					title="${observationInstance.habitat.name}"
					src="${createLinkTo(dir: 'images', file:observationInstance.habitat.icon(ImageType.VERY_SMALL)?.fileName?.trim(), absolute:true)}" />
			</g:if>
	</div>	

	<div>

		<div class="prop">
			<span class="name"><i class="icon-share-alt"></i>Species Name</span>
			<div class="value">
				<obv:showSpeciesName
					model="['observationInstance':observationInstance]" />
<%--				<i class="icon-ok"></i>--%>
			</div>
		</div>


		<div class="prop">
			<span class="name"><i class="icon-map-marker"></i>Place</span>
			<div class="value">
				<g:if test="${observationInstance.placeName == ''}">
					${observationInstance.reverseGeocodedName}
				</g:if>
				<g:else>
					${observationInstance.placeName}
				</g:else>
				<!-- <br /> Lat:
				<g:formatNumber number="${observationInstance.latitude}"
					type="number" maxFractionDigits="2" />
				, Long:
				<g:formatNumber number="${observationInstance.longitude}"
					type="number" maxFractionDigits="2" />
				-->

			</div>
		</div>

		<%--		<div class="prop">--%>
		<%--			<span class="name">Recommendations</span>--%>
		<%--			<div class="value">--%>
		<%--				${observationInstance.getRecommendationCount()}--%>
		<%--			</div>--%>
		<%--		</div>--%>

		<div class="prop">
			<span class="name"><i class="icon-time"></i>Submitted</span>
			<obv:showDate
				model="['observationInstance':observationInstance, 'propertyName':'createdOn']" />

		</div>

		<div class="prop">
			<span class="name"><i class="icon-time"></i>Updated</span>
			<obv:showDate
				model="['observationInstance':observationInstance, 'propertyName':'lastRevised']" />
		</div>

		<g:if test="${observationInstance.notes && showDetails}">
			<div class="prop">
				<span class="name"><i class="icon-info-sign"></i>Notes</span>
                                <div class="value">
					${observationInstance.notes}
                                </div>        
			</div>
		</g:if>

	</div>

	
		<obv:showTagsSummary
			model="['observationInstance':observationInstance]" />

		<div style="display: block; width:100%;overflow:auto;margin-bottom:10px">
			<div style="float: right; clear: both;">
				<sUser:showUserTemplate
					model="['userInstance':observationInstance.author]" />
			</div>

		</div>
	
	
	<div class="story-footer" style="width:100%">
			<div class="footer-item">
				<i class="icon-eye-open"></i>
				${observationInstance.getPageVisitCount()}
				
			</div>
			
			<div class="footer-item">
				<i class="icon-comment"></i>
				<fb:comments-count
					href="${createLink(controller:'observation', action:'show', id:observationInstance.id, base:Utils.getDomainServerUrl(request))}"></fb:comments-count>
				
			</div>
			
			<g:if test="${showDetails}">
<%--				<div class="footer-item"">--%>
<%--					<obv:showFlags model="['observationInstance':observationInstance]" />--%>
<%--	            </div>--%>
<%--	            --%>
	            <div class="footer-item"">
	            	<obv:addFlag model="['observationInstance':observationInstance]" />
	            </div>
            </g:if>
            <g:else>
             	<g:if test="${observationInstance.flagCount>0}">
					<div id="show-flag-count" class="footer-item"><i class="icon-flag"></i>${observationInstance.flagCount}</div>
				</g:if>
            </g:else>
            
            <g:if test="${showDetails}">
			<div class="footer-item" style="width:100px">
				<fb:like layout="button_count"
					href="${createLink(controller:'observation', action:'show', id:observationInstance.id, base:Utils.getDomainServerUrl(request))}"
					width="450" show_faces="true"></fb:like>
			</div>
			</g:if>
			
                
	</div>
</div>
