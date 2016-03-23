<%@page import="species.License"%>
<%@page import="species.License.LicenseType"%>

<div id="license_div_${i}" class="license_div dropdown">
    <g:if test="${resource=='SPECIES'}">
        <a id="selected_license_${i}"
        class="btn dropdown-toggle"
        data-toggle="dropdown">
        <asset:image src="/all/license/${selectedLicense?.name?.getIconFilename()+'.png'}" absolute="true" title="${g.message(code:'title.set.license')}" />

        <b class="caret"></b>
        </a>
    </g:if>
    <g:else>
    <asset:image src="/all/license/${selectedLicense?.name?.getIconFilename()+'.png'}" absolute="true" title="${g.message(code:'title.set.license')}" />
    </g:else>
    <ul id="license_options_${i}" class="dropdown-menu license_options">
        <span><g:message code="default.choose.license.label" /></span>
        <g:each in="${species.License.list()}" var="l">
        <li class="license_option"
        onclick="selectLicense($(this), ${i});" title="${l.name.getTooltip()}">

        <asset:image src="/all/license/${l?.name?.getIconFilename()+'.png'}" absolute="true"/><span style="display:none;">${l?.name?.value}</span> 
        </li>
        </g:each>
    </ul>
    <input id="license_${i}" type="hidden" name="license_${i}" value="${selectedLicense?.name}"></input>
</div>

