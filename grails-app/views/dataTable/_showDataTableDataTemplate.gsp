<style>
    .reco-comment-table {
    left:auto;
    right:0;
    }
</style>
<div class="resizable sidebar_section" style="border:1px solid; overflow:auto;max-height:400px;margin-bottom:0px;">
    <table class="table table-striped table-hover tablesorter checklist-data" style="margin-left: 0px;">

        <thead>
            <tr class="filters">
                <g:each in="${dataTableInstance.fetchColumnNames()}" var="cName">
                <th title="${cName[1]}">${cName[1]}</th>
                </g:each>
                <th title="${g.message(code:'observation.label')}"><g:message code="default.observation.label" /></th>
                <th title="${g.message(code:'default.comments.label')}"><g:message code="default.comments.label" /></th>
            </tr>
        </thead>
        <tbody class="mainContentList rowlink">
            <g:each in="${observations}" var="observation">
            <tr class="${'mainContent ' + observation?.maxVotedReco?.name?.replaceAll(' ', '_')}">
            <% def checklistAnnotations = observation.fetchChecklistAnnotation(); %>
                <g:each in="${dataTableInstance.fetchColumnNames()}" var="cName">
                    <g:if test="${cName[0].equalsIgnoreCase('http://rs.tdwg.org/dwc/terms/scientificName')}">
                        <td class="nameColumn">
                        <a href="${uGroup.createLink(action:'show', controller:'observation', id:observation.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"></a>
                        <g:if test="${observation.maxVotedReco?.taxonConcept && observation.maxVotedReco.taxonConcept?.canonicalForm != null}">
                        <a href="${uGroup.createLink(action:'show', controller:'species', id:observation.maxVotedReco.taxonConcept.findSpeciesId(), 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
                            <i> ${observation.maxVotedReco.taxonConcept.canonicalForm}</i>
                        </a>
                        </g:if>
                        <g:else>
                        <i>${checklistAnnotations[cName[1]]}</i>
                        </g:else>
                        </td>
                    </g:if>
                    <g:else>
                    <td>
                        ${checklistAnnotations[cName[1]]?:''}
                    </td>
                    </g:else>
                </g:each>
		<td>
                    <g:render template="/observation/showObservationImagesList" model="['observationInstance':observation]"/>
                </td>
                
                <td class="nolink">
                    <comment:showCommentPopup model="['commentHolder':observation, 'rootHolder':dataTableInstance]" />
                </td>
            </tr>
            </g:each>	
        </tbody>
    </table>
    <g:if test="${observationsCount > (params.max?params.int('max'):10)}">
    <div class="centered">
        <div class="btn loadMore">
            <span class="progress" style="display: none;"><g:message code="msg.loading" /> </span> <span
                class="buttonTitle"><g:message code="msg.load.more" /></span>
        </div>
    </div>
    </g:if>
    <div class="paginateButtons" style="visibility: hidden; clear: both">
        <p:paginate total="${observationsCount?:0}" action="${'observationData'}" controller="${params.controller?:'checklist'}"
        userGroup="${userGroupInstance}" userGroupWebaddress="${userGroupWebaddress?:params.webaddress}"
        max="${params.max?params.int('max'):10}"  params="${[id:dataTableInstance.id]}"/>
    </div>

                                
</div>
<asset:script>
$(document).ready(function() {
    $('tbody.rowlink').rowlink()
});
</asset:script>
