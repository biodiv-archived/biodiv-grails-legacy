<%@ page import="species.dataset.DataPackage.DataTableType"%>
<style>
    .reco-comment-table {
    left:auto;
    right:0;
    }
</style>
<div class="resizable sidebar_section" style="border:1px solid; overflow:auto;max-height:400px;margin-bottom:0px;">
    <table class="table table-striped table-hover tablesorter checklist-data" style="margin-left: 0px;">

            <% 
            def columnNames = dataTableInstance.fetchColumnNames();
            if(dataTableInstance.dataTableType == DataTableType.FACTS) {
                def c =  [];
                columnNames.each {
                    if(it[1] == 'sci name' || it[1] == 'taxonid' || it[1] == 'attribution' || it[1] == 'contributor' || it[1] == 'license') {
                    } else {
                    c << it;
                    }
                }
                columnNames = c;
            }
            %>
        <thead>
            <tr class="filters">
                <th title="Title">Title</th>
                <g:each in="${columnNames}" var="cName">
                <th title="${cName[1]}">${cName[1]}</th>
                </g:each>
            </tr>
        </thead>
        <tbody class="mainContentList rowlink">
            <g:each in="${dataObjects}" var="dataObject">
            <%def checklistAnnotations = dataObject.fetchChecklistAnnotation();%>
            <tr>
                <td>
                    <a href="${uGroup.createLink(action:'show', controller:checklistAnnotations['type'], id:checklistAnnotations['id'], 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
                        ${raw(checklistAnnotations['title'])}
                    </a>

                </td>      
 
                <g:each in="${columnNames}" var="cName">
                    <g:if test="${cName[0].equalsIgnoreCase('http://rs.tdwg.org/dwc/terms/scientificName')}">
                        <td class="nameColumn">
                        <a href="${uGroup.createLink(action:'show', controller:'observation', id:dataObject.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"></a>
                        <g:if test="${dataObject.maxVotedReco?.taxonConcept && dataObject.maxVotedReco.taxonConcept?.canonicalForm != null}">
                        <a href="${uGroup.createLink(action:'show', controller:'species', id:dataObject.maxVotedReco.taxonConcept.findSpeciesId(), 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
                            <i> ${dataObject.maxVotedReco.taxonConcept.canonicalForm}</i>
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
           </tr>
            </g:each>	
        </tbody>
    </table>
    <g:if test="${dataObjectsCount > (params.max?params.int('max'):10)}">
    <div class="centered">
        <div class="btn loadMore">
            <span class="progress" style="display: none;"><g:message code="msg.loading" /> </span> <span
                class="buttonTitle"><g:message code="msg.load.more" /></span>
        </div>
    </div>
    </g:if>
    <div class="paginateButtons" style="visibility: hidden; clear: both">
        <p:paginate total="${dataObjectsCount?:0}" action="${'dataObjects'}" controller="${'dataTable'}"
        userGroup="${userGroupInstance}" userGroupWebaddress="${userGroupWebaddress?:params.webaddress}"
        max="${params.max?params.int('max'):10}"  params="${[id:dataTableInstance.id]}"/>
    </div>

                                
</div>
<asset:script>
$(document).ready(function() {
    $('tbody.rowlink').rowlink()
});
</asset:script>
