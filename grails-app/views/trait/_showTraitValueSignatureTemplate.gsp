<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@page import="species.auth.SUser"%>
<%@page import="species.participation.Featured"%>
<%@page import="species.groups.UserGroup"%>
<%@page import="species.trait.TraitValueTranslation"%>

<%                      
    def traitTrans = TraitValueTranslation.findByTraitValueAndLanguage(traitValue,userLanguage);
    traitValue.value = (traitTrans?.value)?:'';
    traitValue.description = (traitTrans?.description)?:'';
    traitValue.source = (traitTrans?.source)?:'';
%>
<div class="thumbnail clearfix ${showDetails ? '' : 'signature'} traitIcon"
    data-image-url="${traitValue.thumbnailUrl(null)}"
    data-traitValue="${traitValue.value}"
    data-trait="${traitValue.trait.name}"
    style="margin-left: 0px; height: 32px;">
    <div class="snippet tablet"
        style="display: table; width:100%;height: ${showDetails ? '100px;':'40px;'}">

            <span class="badge ${featured?'featured':''}" style="display:inherit;"  title="${(featureCount>0) ? 'Featured in this group':''}"> </span>
            <div class="figure pull-left" 
            style="display: table; width:32px;height: ${showDetails ? '100px;':'40px;'};">
                    <g:if test="${featured}">
                    </g:if>
                    <g:else>
                    <img
                    class="${showDetails ? 'normal_profile_pic' : 'user-icon small_profile_pic'}"
                    src="${traitValue.mainImage()?.fileName}"                     
                    alt="${traitValue.value}" /> 
                    </g:else>
            </div>

            <span class="ellipsis  ${showDetails ? 'multiline' : ''}" style="display: block;text-align:left;width:96px;float:left;margin-left:6px;margin-top:6px;" title="${traitValue.description}"> ${traitValue.value} </span> 
   </div>
</div>
