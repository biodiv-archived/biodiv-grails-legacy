<div class="page-header clearfix">
        <div class="navbar">
            <h4>
                <ul class="nav">
                    <li class="${params.isChecklistOnly?'':'active'}"><a
                        href="${uGroup.createLink(
                        controller:'observation', action:'list', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress, params:queryParams)}"
                        > <g:message code="heading.browse.observations" /></a>
                    </li>
                    <li class="divider-vertical"></li>
                    <%def cQueryParams = queryParams.clone();
                    cQueryParams['isChecklistOnly'] = true
                    cQueryParams['isMediaFilter'] = false%>
                    <li class="${params.isChecklistOnly?'active':''}"><a
                        href="${uGroup.createLink(
                        controller:'observation', action:'list', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress, 'params':cQueryParams)}"
                        > <g:message code="heading.browse.checklists" /></a>
                    </li>
                </ul>
            </h4>
        </div>
</div>


