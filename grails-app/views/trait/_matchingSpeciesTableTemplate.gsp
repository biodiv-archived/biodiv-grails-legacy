<%@ page import="species.participation.DownloadLog.DownloadType"%>
<%@ page import="species.trait.Trait"%>

<div id="matchingSpeciesList" class="sidebar_section span12" style="clear:both; border:1px solid #CECECE">
    <div id="matchingSpeciesFilterMsg" style="clear:both;">
    <%params.action='matchingSpecies'%>
        <obv:showObservationFilterMessage
            model="['observationInstanceList':matchingSpeciesList, 'instanceTotal':totalCount, 'queryParams':queryParams, resultType:'species', 'hideId':true]" />
        <div style="clear: both;"></div>
    </div>


     <span class='pull-right'>
            <obv:download
            model="['source':'Matching Species', 'requestObject':request, 'downloadTypes':[DownloadType.CSV], 'onlyIcon': 'true', 'downloadFrom' : 'matchingSpecies']" />
        </span>

    <h5><g:message code="trait.matchingspecies" /><span class="matchingSpeciesHeading">${totalCount?' (' + totalCount + ')' :''}</span>
    </h5>
       <table id="matchingSpeciesTable" class="table table-bordered table-condensed jcarousel-skin-ie7" style="background-color:white;">
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
        </tr>
        </g:each>
    </tbody>
</table>
<button id="matchingSpeciesTableAction" class="btn btn-mini pull-right" data-offset='0'><g:message code="msg.load.more" /> </button>
</div>
<script>
$(document).ready(function(){
    $("#matchingSpeciesTableAction").click(loadMatchingSpeciesList);
});


</script>
