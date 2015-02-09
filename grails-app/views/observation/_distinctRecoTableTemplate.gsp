<%@ page import="species.participation.DownloadLog.DownloadType"%>
<%@ page import="species.participation.Recommendation"%>

<div id="distinctRecoList" class="sidebar_section" style="clear:both; border:1px solid #CECECE">
    <h5><g:message code="distinctrecotable.unique.species" /><span class="distinctRecoHeading">${totalCount?' (' + totalCount + ')' :''}</span>
        <span class='pull-right'>
            <obv:download
            model="['source':'Unique Species', 'requestObject':request, 'downloadTypes':[DownloadType.CSV], 'onlyIcon': 'true', 'downloadFrom' : 'uniqueSpecies']" />
        </span>
</h5>
    <table id="distinctRecoTable" class="table table-bordered table-condensed table-striped">
    <tbody>
        <g:each in="${distinctRecoList}" var="r">
        <g:set var="reco" value="${r[0]}"/>
        <tr>
            <td>
                <g:if test="${r[1]}">
                <div class="sci_name ellipsis" title="${r[0]}">
                    ${r[0]}
                </div>
                </g:if>
                <g:else>
                <div class="ellipsis" title="${r[0]}">
                    ${r[0]}
                </div>
                </g:else>
            </td>
            <td>${r[2]}</td>
        </tr>
        </g:each>
    </tbody>
</table>
<button id="distinctRecoTableAction" class="btn btn-mini pull-right" data-offset='0'><g:message code="msg.load.more" /> </button>
</div>
