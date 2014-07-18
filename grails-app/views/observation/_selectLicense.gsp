<%@page import="species.License"%>
<%@page import="species.License.LicenseType"%>

<div id="license_div_${i}" class="license_div dropdown">

    <a id="selected_license_${i}"
        class="btn dropdown-toggle"
        data-toggle="dropdown">

        <img
        src="${resource(dir:'images/license',file:selectedLicense?.name?.getIconFilename()+'.png', absolute:true)}"
        title="Set a license for this image" />

        <b class="caret"></b>
    </a>

    <ul id="license_options_${i}" class="dropdown-menu license_options">
        <span>Choose a license</span>
        <g:each in="${species.License.list()}" var="l">
        <li class="license_option"
        onclick="selectLicense($(this), ${i});" title="${l.name.getTooltip()}">
        <img
        src="${resource(dir:'images/license',file:l?.name?.getIconFilename()+'.png', absolute:true)}" /><span style="display:none;">${l?.name?.value}</span> 
        </li>
        </g:each>
    </ul>
    <input id="license_${i}" type="hidden" name="license_${i}" value="${selectedLicense?.name}"></input>
</div>

