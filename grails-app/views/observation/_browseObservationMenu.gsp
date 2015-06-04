<div class="page-header clearfix">
        <div class="navbar">
            <h4>
                <ul class="nav">
                    <li class="${params.isChecklistOnly?'':'active'}"><a
                        href="${uGroup.createLink(
                        controller:'observation', action:'list', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"
                        > <g:message code="heading.browse.observations" /></a>
                    </li>
                    <li class="divider-vertical"></li>
                    <li class="${params.isChecklistOnly?'active':''}"><a
                        href="${uGroup.createLink(
                        controller:'observation', action:'list', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress, 'params':['isChecklistOnly':true, 'areaFilter':'all', 'isMediaFilter':false])}"
                        > <g:message code="heading.browse.checklists" /></a>
                    </li>
                </ul>
            </h4>
        </div>
</div>


