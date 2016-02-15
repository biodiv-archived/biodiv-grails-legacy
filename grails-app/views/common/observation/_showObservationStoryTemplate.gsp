<%@page import="species.utils.Utils"%>
<%@page import="species.Species"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.UtilsService"%>
<div class="observation_story">
    <div style="height:27px;">
        <g:if test="${showDetails && !showFeatured}">
        <%
        def speciesInstance = Species.read(observationInstance.maxVotedReco?.taxonConcept?.findSpeciesId())
        %>

        <s:showSpeciesExternalLink model="['speciesInstance':speciesInstance]"/>
            </g:if>
            <div class="observation-icons">

                <g:if test="${showDetails && maxVotedReco?.taxonConcept?.threatenedStatus}">
                <div style="float:left;">
                    <s:showThreatenedStatus model="['threatenedStatus':maxVotedReco.taxonConcept?.threatenedStatus]"/>
                    </div>
                    </g:if>


                    <g:if test="${observationInstance.habitat}">
                    <div style="float: left;">
                        <span
                            class="habitat_icon_show group_icon habitats_sprites active ${observationInstance.habitat.iconClass()}"
                            title="${observationInstance.habitat.name}"></span>
                    </div>
                    </g:if>


                    <div class="group_icon_show_wrap" id="group_icon_show_wrap_${observationInstance.id}">
                        <span
                            class="group_icon group_icon_show_${observationInstance.id} species_groups_sprites active ${observationInstance.group.iconClass()}"
                            title="${observationInstance.group?.name}"></span>
                        <g:if test="${showDetails && !showFeatured}">        
                        <div class="btn btn-small btn-primary edit_group_btn">Edit
                        </div>
                        </g:if>    
                    </div>


                    <g:if test="${showFeatured}">
                    <span class="featured_details btn" style="display:none;"><i class="icon-list"></i></span>
                    </g:if>
                    <g:if test="${!showFeatured}">
                    <div class="column propagateGrpHab" id="propagateGrpHab_${observationInstance.id}">
                        <form id="updateSpeciesGrp"  name="updateSpeciesGrp"                              
                            method="GET">
                            <g:render template="/common/speciesGroupDropdownTemplate" model="['observationInstance':observationInstance]"/>
                            <input type="hidden" name="prev_group" value="${observationInstance?.group?.id}" />
                            <input type="hidden" name="observationId" value="${observationInstance?.id}"> 
                            <input type="submit" class="btn btn-small btn-primary save_group_btn" style="display:none;" value="Save" />
                        </form>
                    </div>
                    </g:if>


                </div>
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
                <g:render template="/common/featureNotesTemplate" model="['instance':observationInstance, 'featuredNotes':featuredNotes, 'userLanguage': userLanguage]"/>
            </div>
            </g:if>
            <g:else>


            <div class="observation_story_body ${showFeatured?'toggle_story':''}" style=" ${showFeatured?'display:none;':''}">

                <g:if test="${observationInstance.dataset}">
                <g:render template="/datasource/showDatasourceSignatureTemplate" model="['instance':observationInstance.dataset.datasource, 'showDetails':true]"/>
                </g:if>

                <div class="prop">
                    <g:if test="${showDetails}">
                    <span class="name"><i class="icon-share-alt"></i><g:message code="default.name.label" /></span>
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
                    <span class="name"><i class="icon-map-marker"></i><g:message code="default.place.label" /></span>
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
                    </div>
                </div>

                <div class="prop">
                    <g:if test="${showDetails}">
                    <span class="name"><i class="icon-time"></i><g:message code="default.observed.on.label" /></span>
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
                    <span class="name"><i class="icon-time"></i><g:message code="default.submitted.label" /></span>
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
                    <span class="name"><i class="icon-time"></i><g:message code="default.updated.label" /></span>
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
                    <span class="name"><i class="icon-info-sign"></i><g:message code="default.attribution.label" /></span>
                    <div class="value linktext">
                        ${observationInstance.fetchAttributions()}
                    </div>
                </div>
                </g:if>

                <g:if test="${observationInstance.isChecklist && observationInstance.sourceText}" >
                <div class="prop">
                    <span class="name"><i class="icon-info-sign"></i><g:message code="default.source.label" /></span>
                    <div class="value linktext">
                        ${observationInstance.sourceText}
                    </div>
                </div>
                </g:if>
                </g:if>

                <g:if test="${observationInstance.notes}">
                <div class="prop">
                    <g:if test="${showDetails}">
                    <span class="name"><i class="icon-info-sign"></i><g:message code="default.notes.label" /></span>
                    <div class="value notes_view linktext">                        
                        <%  def styleVar = 'block';
                        def clickcontentVar = '' 
                        %> 
                        <g:if test="${observationInstance?.language?.id != userLanguage?.id}">
                        <%  
                        styleVar = "none"
                        clickcontentVar = '<a href="javascript:void(0);" class="clickcontent btn btn-mini">'+observationInstance?.language?.threeLetterCode?.toUpperCase()+'</a>';
                        %>
                        </g:if>

                        ${raw(clickcontentVar)}
                        <div style="display:${styleVar}">${raw(Utils.linkifyYoutubeLink(observationInstance.notes))}</div>

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
                    <span class="name"><i class="icon-info-sign"></i><g:message code="default.references.label" /></span>
                    <div class="value linktext">
                        ${raw(checklistInstance.refText)}
                    </div>		
                </div>
                </g:if>

                <g:if test="${observationInstance.dataset}" >
                <div class="prop">
                    <span class="name"><i class="icon-info-sign"></i><g:message code="default.citeas.label" /></span>
                    <div class="value linktext">
                        ${observationInstance.dataset.datasource.title} (${UtilsService.formatDate(observationInstance.dataset.publicationDate)}) ${observationInstance.dataset.title}
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
                        <g:if test="${!observationInstance.dataset}">
                        <sUser:showUserTemplate
                        model="['userInstance':observationInstance.author, 'userGroup':userGroup]" />
                        </g:if>
                    </div>
                </div>

            </div>
            </g:else>
        </div>

        <g:if test="${!showFeatured}">

        <script type="text/javascript">

            $(document).ready(function(){
                    var group_icon = $('.group_icon_show');
                    var group_icon_show_wrap = $('.group_icon_show_wrap');
                    var label_group = $('label.group');
                    var propagateGrpHab = $('.propagateGrpHab');
                    $('.propagateGrpHab .control-group  label').hide();

                    $('.edit_group_btn').click(function(){            
                        group_icon_show_wrap.hide();
                        label_group.hide();
                        propagateGrpHab.show();

                        }); 
                    });
</script>
</g:if>
