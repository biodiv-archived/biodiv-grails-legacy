
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
