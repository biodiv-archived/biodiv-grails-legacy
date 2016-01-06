<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@page import="species.auth.SUser"%>
<%@page import="species.participation.Featured"%>
<%@page import="species.dataset.Datasource"%>
<%@page import="species.dataset.Dataset"%>

<div class="thumbnail clearfix signature"
    style="margin-left: 0px;width:${showDetails?'auto;':'250px;'} max-width:${showDetails?'100%;':'250px;'}margin:10px 0px;">
    <div class="snippet tablet"
        style="display: table; width:100%;height: ${showDetails ? '50px;':'40px;'}">

            <span class="badge ${featured?'featured':''}" style="display:inherit;"  title="${(featureCount>0) ? 'Featured in this group':''}"> </span>
            <div class="figure pull-left" style="display: table; width:110px;height: ${showDetails ? '50px;':'40px;'};margin-right:10px;">
                <a target="_blank"
                    href="${uGroup.createLink('controller':'datasource', 'action':'show', id:instance.id)}">
                    <g:if test="${featured}">
                    </g:if>
                    <g:else>
                    <img
                    src="${instance.mainImage()?.fileName}" title="${instance.title}"
                    alt="${instance.title}" /> 
                    </g:else>
                </a>
            </div>

            <a target="_blank"
                href="${uGroup.createLink('controller':'datasource', 'action':'show', id:instance.id)}">
                <span class="ellipsis  ${showDetails ? 'multiline' : ''}" style="display: block;text-align:left;${showDetails ? 'width:auto' : 'width:200px'};"
                    title="${instance.title}"><b>Datasource : </b> ${instance.title} </span> 
            </a>
            <g:if test="${instance && instance.id}">
            <div class="footer-item" title="${g.message(code:'showdatasource.title.datasetCount')}">
                <i class="icon-book"></i>
                ${Dataset.countByDatasource(instance)}
            </div>
           </g:if>
    </div>
</div>
