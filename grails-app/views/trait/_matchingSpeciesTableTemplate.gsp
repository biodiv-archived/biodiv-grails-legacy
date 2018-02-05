<%@ page import="species.participation.DownloadLog.DownloadType"%>
<%@ page import="species.trait.Trait"%>

<div id="matchingSpeciesList" class="sidebar_section span12" style="clear:both; border:1px solid #CECECE">
    <div id="matchingSpeciesFilterMsg" style="clear:both;">
    <%params.action=matchingAction?:'matchingSpecies'%>
        <obv:showObservationFilterMessage
            model="['observationInstanceList':matchingSpeciesList, 'instanceTotal':totalCount, 'queryParams':queryParams, resultType:resultType?:'species', 'hideId':true]" />
        <div style="clear: both;"></div>
    </div>


     <span class='pull-right'>
            <obv:download
            model="['source':'Matching '+resultType, 'requestObject':request, 'downloadTypes':[DownloadType.CSV], 'onlyIcon': 'true', 'downloadFrom' : 'matching'+resultType]" />
        </span>

    <h5>Matching ${resultType?resultType.capitalize():'Species'}${totalCount?' (' + totalCount + ')' :''}
    </h5>
       <table id="matchingSpeciesTable" class="table table-bordered table-condensed jcarousel-skin-ie7" style="background-color:white;">
       <thead>
       <th> ${resultType?resultType.capitalize():'Species'}</th>
       <th>Traits</th>
       
       <g:if test="${resultType == 'species'}">
       <th>No of Observations</th>
       </g:if>
       </thead>
    <tbody>
        <g:each in="${matchingSpeciesList}" var="r">
        <g:set var="reco" value="${r[0]}"/>
        <tr>
            <td></td>
            <td>
                <g:if test="${r[1]}">
                <div class="sci_name ellipsis" title="${r[0]}">
                    ${r[1]}
                </div>
                </g:if>
                <g:else>
                <div class="ellipsis" title="${r[0]}">
                    ${r[1]}
                </div>
                </g:else>
            </td>
            <td>${r[8]}</td>
        </tr>
        </g:each>
    </tbody>
</table>
<button id="matchingSpeciesTableAction" class="btn btn-mini pull-right" data-max='10' data-offset='0'><g:message code="msg.load.more" /> </button>
</div>
<script>
$(document).ready(function(){
    $("#matchingSpeciesTableAction").click(${loadListAction?:'loadMatchingSpeciesList'});
});


</script>
