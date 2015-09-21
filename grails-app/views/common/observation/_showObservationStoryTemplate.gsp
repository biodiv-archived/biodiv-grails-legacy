<%@page import="species.utils.Utils"%>
<%@page import="species.Species"%>
<%@page import="species.utils.ImageType"%>
<style>
    <g:if test="${!showDetails}">

    .observation .prop .value {
        margin-left:260px;
    }
    .group_icon_show_wrap{
        float:left;
    }
    </g:if>
    <g:if test="${!showFeatured}">
    li.group_option{
        height:30px;
    }
    li.group_option span{
        padding: 0px;
        float: left;
    }
    .groups_super_div{
        margin-top: -15px;
        margin-right: 10px;
    }
    .groups_div > .dropdown-toggle{
          height: 25px;
    }
    .group_options, .group_option{
          min-width: 110px;
    }
    .save_group_btn{
        float: right;
        margin-right: 11px;
          margin-top: -9px;
    }
    .group_icon_show_wrap{
        border: 1px solid #ccc;
        float: right;
        height: 33px;
        margin-right: 4px;
    }
    .edit_group_btn{
        top: -10px;
        position: relative;
        margin-right: 12px;
    }
    .propagateGrpHab{
        display:none;
        float: right;
        margin-top: -5px;
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

                <g:if test="${showDetails && speciesInstance && speciesInstance.taxonConcept?.threatenedStatus}">
                <div style="float:left;">
                    <s:showThreatenedStatus model="['threatenedStatus':speciesInstance.taxonConcept?.threatenedStatus]"/>
                    </div>
                </g:if>


            	<g:if test="${observationInstance.habitat}">
                <div style="float: left;">
                <span
                    class="habitat_icon_show group_icon habitats_sprites active ${observationInstance.habitat.iconClass()}"
                    title="${observationInstance.habitat.name}"></span>
                </div>
                </g:if>


                <div class="group_icon_show_wrap">
                    <span
                        class="group_icon group_icon_show species_groups_sprites active ${observationInstance.group.iconClass()}"
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
                 <div class="column propagateGrpHab">
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


<g:if test="${!showFeatured}">

<script type="text/javascript">

$(document).ready(function(){
     /* Added for  Species Update*/
        var group_icon = $('.group_icon_show');
        var group_icon_show_wrap = $('.group_icon_show_wrap');
        //var habitat_icon = $('.habitat_icon_show');
        var label_group = $('label.group');
        var propagateGrpHab = $('.propagateGrpHab');
        $('.propagateGrpHab .control-group  label').hide();

        $('.edit_group_btn').click(function(){            
            group_icon_show_wrap.hide();
            //habitat_icon.hide();
            label_group.hide();
            propagateGrpHab.show();

        });        
   

    $('#updateSpeciesGrp').bind('submit', function(event) {

         $(this).ajaxSubmit({ 
                    url: "${uGroup.createLink(controller:'observation', action:'updateSpeciesGrp')}",
                    dataType: 'json', 
                    type: 'GET',  
                    beforeSubmit: function(formData, jqForm, options) {
                        /*console.log(formData);
                        if(formData.group_id == formData.prev_group){
                            alert("Nothing Changes!");
                            return false;
                        }*/
                    },               
                    success: function(data, statusText, xhr, form) {
                            console.log(data);
                            group_icon.removeClass(data.model.prevgroupIcon).addClass(data.model.groupIcon).attr('title',data.model.groupName);                           
                            group_icon_show_wrap.show();
                            //habitat_icon.show();
                            propagateGrpHab.hide();
                            updateFeeds();
                    },
                    error:function (xhr, ajaxOptions, thrownError){
                        //successHandler is used when ajax login succedes
                        var successHandler = this.success, errorHandler = showUpdateStatus;
                        handleError(xhr, ajaxOptions, thrownError, successHandler, errorHandler);
                    } 

                 });    
               
            event.preventDefault(); 
        });
});
</script>
</g:if>