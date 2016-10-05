<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@page import="species.auth.SUser"%>
<%@page import="species.participation.Featured"%>
<%@page import="species.groups.UserGroup"%>
<div class="thumbnail clearfix ${showDetails ? '' : 'signature'}"
    style="margin-left: 0px; height: 32px;">
    <div class="snippet tablet"
        style="display: table; width:100%;height: ${showDetails ? '100px;':'40px;'}">

            <span class="badge ${featured?'featured':''}" style="display:inherit;"  title="${(featureCount>0) ? 'Featured in this group':''}"> </span>
            <div class="figure pull-left" style="display: table; width:32px;height: ${showDetails ? '100px;':'40px;'};">
                    <g:if test="${featured}">
                    </g:if>
                    <g:else>
                    <img
                    class="${showDetails ? 'normal_profile_pic' : 'user-icon small_profile_pic'}"
                    src="${traitValue.mainImage()?.fileName}" title="${traitValue.value}"
                    alt="${traitValue.value}" /> 
                    </g:else>
            </div>

            <span class="ellipsis  ${showDetails ? 'multiline' : ''}" style="display: block;text-align:left;${showDetails ? 'width:auto' : 'width:100px'};float:left;margin-left:6px;" title="${traitValue.value}"> ${traitValue.value} </span> 
   </div>
</div>
