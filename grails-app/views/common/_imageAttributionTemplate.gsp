<%@page import="species.utils.Utils"%>
<%@page import="species.Resource.ResourceType"%>

<g:if test="${resource}">
<div class="notes" style="text-align: left;font: italic 12px/1.4 georgia,serif;margin: 0;color: black;">
    <div>

        <div class="license license_div">

        <g:if test="${resource.type != ResourceType.AUDIO}">
            <i class="slideUp icon-chevron-up pull-right"></i>

            <obv:rating model="['resource':resource, 'class':'gallery_rating']"/>
        </g:if>    
            <g:each in="${resource?.licenses}" var="l">
            <a href="${l?.url}" target="_blank"> <img class="icon" style="height:auto;margin-right:2px;"
                src="${createLinkTo(dir:'images/license', file: l?.name.value().toLowerCase().replaceAll('\\s+','')+'.png', absolute:true)}"
                alt="${l?.name.value()}" /> </a>
            </g:each>

<%--            <g:if test="${resource.type == ResourceType.IMAGE}">--%>
<%--                <a href="${createLinkTo(file: resource.fileName.trim(), base:base)}" target="_blank">View original image</a> --%>
<%--            </g:if>--%>

            <g:if test="${resource.description}">
                <div class="span5 ellipsis multiline" style="margin-left:0px">${raw(resource.description)}</div>

                <div style="clear:both;"></div>
            </g:if>
            

        </div>
        <g:if test="${resource.contributors?.size() > 0}">
        <b>Contributors:</b>
        <ol>
            <g:each in="${resource.contributors}" var="a">
            <li>
            ${(a?.user) ? a?.user.name : a?.name}
            </li>
            </g:each>
        </ol>
        </g:if>
        <g:if test="${resource.attributors?.size() > 0}">
        <b>Attributions:</b>
        <ol>
            <g:each in="${resource.attributors}" var="a">
            <li>
             ${(a?.user) ? a?.user.name : a?.name}
            </li>
            </g:each>
        </ol>
        </g:if>
        <g:if test="${resource.url}">
        <a href="${resource.url}" target="_blank"><b>View image
                source</b> </a>
        </g:if>

    </div>
</div>
</g:if>
