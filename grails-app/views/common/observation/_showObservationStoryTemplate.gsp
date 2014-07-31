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
<div class="observation_story">
    	<%
        def speciesInstance = Species.read(observationInstance.maxVotedReco?.taxonConcept?.findSpeciesId())
        %>
        <g:if test="${showDetails && !showFeatured}">
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

                <g:if test="${showFeatured}">
                    <span class="featured_details btn" style="display:none;"><i class="icon-list"></i></span>
                </g:if>
            </div>
            <g:if test="${showFeatured}">
            <div class="featured_body">
                <div class="featured_title ellipsis"> 
                    <div class="heading">
                        <g:if test="${observationInstance.isChecklist}">
                        <g:link url="${uGroup.createLink(controller:'checklist', action:'show', id:observationInstance.id, 'pos':pos, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }" name="l${pos}">
                            <span class="ellipsis">${raw(observationInstance.title)}</span>
                        </g:link>
                        </g:if>
                        <g:else>
                        <g:link url="${uGroup.createLink(controller:'observation', action:'show', id:observationInstance.id, 'pos':pos, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }" name="l${pos}">
                            <span class="ellipsis">${raw(observationInstance.fetchFormattedSpeciesCall())}</span>
                        </g:link>
                        </g:else>
                    </div>
                </div>
                <g:render template="/common/featureNotesTemplate" model="['instance':observationInstance, 'featuredNotes':featuredNotes]"/>
            </div>
            </g:if>
            <g:else>
        <div class="observation_story_body ${showFeatured?'toggle_story':''}" style=" ${showFeatured?'display:none;':''}">
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
                    <g:if test="${observationInstance.toDate && observationInstance.fromDate != observationInstance.toDate}">&nbsp;
                    <b>-</b>&nbsp; <time class="timeago" datetime="${observationInstance.toDate.getTime()}"></time>
                    </g:if>
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
            </g:if>

            <g:if test="${observationInstance.notes}">
                <div class="prop">
                    <g:if test="${showDetails}">
                    <span class="name"><i class="icon-info-sign"></i>Notes</span>
                    <div class="value notes_view linktext">                        
                        ${raw(Utils.linkifyYoutubeLink(observationInstance.notes))}
                    </div>
                    </g:if>
                    <g:else>
                    <div class="value notes_view linktext ${showDetails?'':'ellipsis'}">
                        ${raw(Utils.stripHTML(observationInstance.notes))}
                    </div>

                    </g:else>
                </div>
            </g:if>

            <g:if test="${showDetails}">
                <g:if test="${observationInstance.isChecklist && observationInstance.refText}" >
                <div class="prop">
                    <span class="name"><i class="icon-info-sign"></i>References</span>
                    <div class="value linktext">
                        ${raw(checklistInstance.refText)}
                    </div>		
                </div>
                </g:if>

                <div class="prop">
                    <obv:showTagsSummary
                    model="['observationInstance':observationInstance, 'isAjaxLoad':false]" />
                </div>

        </g:if>

        <div class="row observation_footer" style="margin-left:0px;">
            <obv:showFooter
                model="['observationInstance':observationInstance, 'showDetails':showDetails, 'showLike':true]" />

            <div class="story-footer" style="right:3px;">
                <sUser:showUserTemplate
                model="['userInstance':observationInstance.author, 'userGroup':userGroup]" />
            </div>
        </div>
        </div>
        </g:else>
    </div>
