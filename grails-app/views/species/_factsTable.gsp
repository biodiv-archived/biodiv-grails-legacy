<g:if test="${factsList.size() > 0 }">
<div>
    <table class="table table-hover tablesorter span12" style="margin-left: 0px;">
        <thead>
            <tr>
                <g:if test="${!hideSubject}">
                <th><g:message code="default.subject.label" /></th>
                </g:if>
                <th><g:message code="default.predicate.label" /> </th>
                <th><g:message code="default.value.type" /> </th>
                <th><g:message code="default.metadata.type" /> </th>
            </tr>
        </thead>
        <tbody class="mainContentList">
            <g:each in="${factsList}" status="i"
            var="fact">
            <tr class="mainContent">
                <g:if test="${!hideSubject}">
                <td class="linktext">${fact.species}</td>
                </g:if>
                <td>${fact.predicate}</td>
                <td>${fact.value}
                    <g:if test="${fact.icon}">
                    <img class="icon" src="${fact.icon}"/>
                    </g:if>
                </td>
                <td>
                    <g:if test="${fact.metadata}">
                    <a class="btn-link" data-toggle="collapse" data-target=".collapseme">
                        <i class="icon-info"></i> 
                    </a>
                    <div class="collapseme collapse out">
                        <div>
                            <ul>
                                <g:each in="${fact.metadata}" var="m">
                                <li>${m.predicate} : ${m.value}</li>
                                </g:each>
                            </ul>
                        </div>
                    </div>
                    </g:if>
                </td>
            </tr>
            </g:each>
        </tbody>
    </table>
</div>
</g:if>

