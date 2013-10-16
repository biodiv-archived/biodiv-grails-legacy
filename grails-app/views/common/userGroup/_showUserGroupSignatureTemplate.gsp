<%@page import="species.utils.ImageType"%>

<div class="thumbnail clearfix ${showDetails ? '' : 'signature'}"
    style="margin-left: 0px;width:${showDetails?'auto':'200px;'}">
    <div class="snippet tablet "
        style="display: table; width:100%;height: ${showDetails ? '100px;':'40px;'}">

        <g:if test="${userGroup.id}">
            <div class="figure pull-left" style="display: table; width:32px;height: ${showDetails ? '100px;':'40px;'};">
                <a
                    href="${uGroup.createLink(mapping:'userGroup', controller:'userGroup', action:'show', base:userGroup.domainName, 'userGroup':userGroup, 'pos':pos)}">
                    <img
                    class="${showDetails ? 'normal_profile_pic' : 'user-icon small_profile_pic'}"
                    src="${userGroup.mainImage()?.fileName}" title="${userGroup.name}"
                    alt="${userGroup.name}" /> </a>
            </div>

            <a 
                href="${uGroup.createLink(mapping:'userGroup', controller:'userGroup', action:'show', base:userGroup.domainName, 'userGroup':userGroup, 'pos':pos)}">
                <span class="ellipsis  ${showDetails ? 'multiline' : ''}" style="display: block;text-align:left;${showDetails ? 'width:auto' : 'width:120px'};"
                    title="${userGroup.name}"> ${userGroup.name} </span> </a>
            <g:if test="${!showDetails}">
            <div class="pull-left">
                <i class="icon-user"></i>
                ${userGroup.getAllMembersCount()}
            </div>
            </g:if>
        </g:if>

        <g:else>
        <!-- parentportal-->${userGroup.name}
        <div class="figure pull-left" style="display: table; width:32px;height: ${showDetails ? '100px;':'40px;'};">
            <a
                href="${uGroup.createLink(controller:'userGroup', action:'show')}">
                <img
                class="${showDetails ? 'normal_profile_pic' : 'user-icon small_profile_pic'}"
                src="${userGroup.mainImage()?.fileName}" title="${userGroup.name}"
                alt="${userGroup.name}" /> </a>
        </div>


        <a 
            href="${uGroup.createLink(controller:'userGroup', action:'show')}">
            <span class="ellipsis  ${showDetails ? 'multiline' : ''}" style="display: block;text-align:left;${showDetails ? 'width:auto' : 'width:120px'};"
                title="${userGroup.name}"> ${userGroup.name} </span> </a>

        </g:else>


    </div>
</div>
