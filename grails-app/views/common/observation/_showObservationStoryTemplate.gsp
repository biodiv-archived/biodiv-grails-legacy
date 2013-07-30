<%@page import="species.utils.Utils"%>
<%@page import="species.Species"%>
<%@page import="species.utils.ImageType"%>
<style>
    <g:if test="${!showDetails}">

    .observation .prop .value {
    margin-left:10px;
    }

    </g:if>
</style>
<div class="observation_story" style="${showDetails?'':'overflow:visible;width:100%'}">
    <div>
    	<%
        def speciesInstance = Species.read(observationInstance.maxVotedReco?.taxonConcept?.findSpeciesId())
        %>
        <g:if test="${showDetails}">
        	<s:showSpeciesExternalLink model="['speciesInstance':speciesInstance]"/>
        </g:if>
            <div class="observation-icons">
            	<g:if test="${observationInstance.habitat}">
                <span style="float:right;"
                    class="habitat_icon group_icon habitats_sprites active ${observationInstance.habitat.iconClass()}"
                    title="${observationInstance.habitat.name}"></span>
                </g:if>
                <span style="float:right;"
                    class="group_icon species_groups_sprites active ${observationInstance.group.iconClass()}"
                    title="${observationInstance.group?.name}"></span>
                
                <g:if test="${showDetails && speciesInstance && speciesInstance.taxonConcept?.threatenedStatus}">
            	 <span>
					<s:showThreatenedStatus model="['threatenedStatus':speciesInstance.taxonConcept?.threatenedStatus]"/>
				</span>
				</g:if>
                
            </div>
        </div>
        <div class="span7">

            <div class="prop">
                <g:if test="${showDetails}">
                <span class="name"><i class="icon-share-alt"></i>Name</span>
                </g:if>
                <g:else>
                <i class="pull-left icon-share-alt"></i>
                </g:else>
                <div class="value">
                    <obv:showSpeciesName
                    model="['observationInstance':observationInstance, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress, 'isListView':!showDetails]" />
                </div>
            </div>


            <div class="prop">
                <g:if test="${showDetails}">
                <span class="name"><i class="icon-map-marker"></i>Place</span>
                </g:if>
                <g:else>
                <i class="pull-left icon-map-marker"></i>
                </g:else>
                <div class="value ellipsis">
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
                <g:if test="${showDetails}">
                <span class="name"><i class="icon-time"></i>Observed on</span>
                </g:if>
                <g:else>
                <i class="pull-left icon-time"></i>
                </g:else>
                <div class="value">
                    <time class="timeago"
                    datetime="${observationInstance.fromDate.getTime()}"></time>
                </div>
            </div>

            <g:if test="${showDetails}">
                <div class="prop">
                    <g:if test="${showDetails}">
                    <span class="name"><i class="icon-time"></i>Submitted</span>
                    </g:if>
                    <g:else>
                    <i class="pull-left icon-time"></i>
                    </g:else>
                    <div class="value">
                        <time class="timeago"
                        datetime="${observationInstance.createdOn.getTime()}"></time>
                    </div>
                </div>

                <div class="prop">
                    <g:if test="${showDetails}">
                    <span class="name"><i class="icon-time"></i>Updated</span>
                    </g:if>
                    <g:else>
                    <i class="pull-left icon-time"></i>
                    </g:else>
                    <div class="value">
                        <time class="timeago"
                        datetime="${observationInstance.lastRevised?.getTime()}"></time>
                    </div>
                </div>
            </g:if>

            <g:if test="${observationInstance.notes}">
                <div class="prop">
                    <g:if test="${showDetails}">
                    <span class="name"><i class="icon-info-sign"></i>Notes</span>
                    <div class="notes_view linktext">
                        ${Utils.linkifyYoutubeLink(observationInstance.notes)}
                    </div>
                    </g:if>
                    <g:else>
                    <div class="notes_view linktext ${showDetails?'':'ellipsis multiline'}">
                        ${Utils.stripHTML(observationInstance.notes)}
                    </div>

                    </g:else>
                </div>
            </g:if>

            <g:if test="${showDetails}">
                <g:if test="${observationInstance.isChecklist && observationInstance.fetchAttributions()}">
                <div class="prop" >
                    <span class="name"><i class="icon-info-sign"></i>Attribution</span>
                    <div class="value linktext">
                        ${observationInstance.fetchAttributions()}
                    </div>
                </div>
                </g:if>

                <g:if test="${observationInstance.isChecklist && observationInstance.sourceText}" >
                <div class="prop">
                    <span class="name"><i class="icon-info-sign"></i>Source</span>
                    <div class="value linktext">
                        ${observationInstance.sourceText}
                    </div>
                </div>
                </g:if>
                <g:if test="${observationInstance.isChecklist && observationInstance.refText}" >
                <div class="prop">
                    <span class="name"><i class="icon-info-sign"></i>References</span>
                    <div class="value linktext">
                        ${checklistInstance.refText}
                    </div>		
                </div>
                </g:if>

                <div class="prop">
                    <obv:showTagsSummary
                    model="['observationInstance':observationInstance, 'isAjaxLoad':false]" />
                </div>


            </g:if>

        </div>
        <div class="row" style="margin-left:0px;">
            <obv:showFooter
            model="['observationInstance':observationInstance, 'showDetails':showDetails, 'showLike':true]" />
            <div style="float: right; clear: both;">
                <sUser:showUserTemplate
                model="['userInstance':observationInstance.author, 'userGroup':userGroup]" />
            </div>
        </div>






    </div>
