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
            } else if(dataTableInstance.dataTableType == DataTableType.TRAITS) {
                columnNames =  [['','traitTypes',1],['','dataTypes',1],['','units',1],['','values',1],['','field',1],['','taxon',1],['','isNotObservationTrait',1],['','isParticipatory',1],['','showInObservation',1]];
            } else if(dataTableInstance.dataTableType == DataTableType.DOCUMENTS) {
                columnNames =  [['','url',1]];
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
        <tbody data-link="row" class="mainContentList rowlink">
            <g:each in="${dataObjects}" var="dataObject">
            <%def checklistAnnotations = dataObject.fetchChecklistAnnotation();%>
            <tr class="mainContent">
                <td>
                    <a href="${uGroup.createLink(action:'show', controller:checklistAnnotations['type'], id:checklistAnnotations['id'], 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
                        ${raw(checklistAnnotations['title'])}
                    </a>

                </td>      
 
                <g:each in="${columnNames}" var="cName">
                    <td>
                        ${checklistAnnotations[cName[1]]?:''}
                    </td>
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
//    $('tbody.rowlink').rowlink()
});
</asset:script>
