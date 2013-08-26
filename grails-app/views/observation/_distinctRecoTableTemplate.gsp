
<%@ page import="species.participation.Recommendation"%>
<table  class="table table-bordered table-condensed table-striped">
    <thead>
        <tr>
            <th>Species</th>
            <th>No</th>
        </tr>
    </thead>
    <tbody>
        <g:each in="${distinctRecoList}" var="r">
        <g:set var="reco" value="${Recommendation.read(r[0])}"/>
        <tr>
            <td>
                <g:if test="${reco.isScientificName}">
                <div class="sci_name ellipsis" title="${reco.name}">
                    ${reco.name}
                </div>
                </g:if>
                <g:else>
                <div class="ellipsis" title="${reco.name}">
                    ${reco.name}
                </div>
                </g:else>
            </td>
            <td>${r[1]}</td>
        </tr>
        </g:each>
    </tbody>
</table>
