<style>
    .reco-comment-table {
    left:auto;
    right:0;
    }
</style>
<div class="resizable" style="overflow:auto;maxHeight:400px;">
    <table class="table table-hover tablesorter checklist-data" style="margin-left: 0px;">

        <thead>
            <tr>
                <g:each in="${checklistInstance.fetchColumnNames()}" var="cName">
                <th title="${cName}">${cName.replaceAll("_", " ")}</th>
                </g:each>
                <th title="Observation">Observation</th>
                <th title="Comments">Comments</th>
            </tr>
        </thead>
        <tbody class="mainContentList" name="p${params?.offset}">
            <g:each in="${observations}" var="observation">
            <tr class="${'mainContent ' + observation?.maxVotedReco?.name?.replaceAll(' ', '_')}">
                <g:each in="${observation.fetchChecklistAnnotation()}" var="annot">
                <td>
                    <g:if test="${annot.key.equalsIgnoreCase(checklistInstance.sciNameColumn)}">
                    <g:if test="${observation.maxVotedReco?.taxonConcept && observation.maxVotedReco.taxonConcept?.canonicalForm != null}">
                    <a href="${uGroup.createLink(action:'show', controller:'species', id:observation.maxVotedReco.taxonConcept.findSpeciesId(), 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
                        <i> ${observation.maxVotedReco.taxonConcept.canonicalForm}</i>
                    </a>
                    </g:if>
                    <g:else>
                    <i>${annot.value}</i>
                    </g:else>
                    </g:if>
                    <g:else>
                    ${annot.value}
                    </g:else>
                </td>
                </g:each>
                <td>
                    <a href="${uGroup.createLink(action:'show', controller:'observation', id:observation.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
                        url</a>
                </td>
                <td>
                    <g:render template="/observation/showObservationImagesList" model="['observationInstance':observation]"/>
               </td>
                <td>
                    <comment:showCommentPopup model="['commentHolder':observation, 'rootHolder':checklistInstance]" />
                </td>
            </tr>
            </g:each>	
        </tbody>
    </table>
    <g:if test="${checklistInstance.speciesCount > (params.max?:0)}">
    <div class="centered">
        <div class="btn loadMore">
            <span class="progress" style="display: none;">Loading ... </span> <span
                class="buttonTitle">Load more</span>
        </div>
    </div>
    </g:if>

    <div class="paginateButtons" style="visibility: hidden; clear: both">
        <p:paginate total="${checklistInstance.speciesCount?:0}" action="${'observationData'}" controller="${params.controller?:'checklist'}"
        userGroup="${userGroupInstance}" userGroupWebaddress="${userGroupWebaddress?:params.webaddress}"
        max="${params.max}"  params="${[id:checklistInstance.id]}"/>
    </div>
</div>
