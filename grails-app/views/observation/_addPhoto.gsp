<%@page import="species.Resource"%>
<%@page import="species.Resource.ResourceType"%>
<%@page import="species.License"%>
<%@page import="species.License.LicenseType"%>
<%@ page import="species.utils.Utils"%>
<div>
    <i class="icon-picture"></i><span>Upload photos of a
        single observation and species and rate images inorder to order them.</span>


    <div
        class="resources control-group ${hasErrors(bean: observationInstance, field: 'resource', 'error')}">
        <ul id="imagesList" class="thumbwrap thumbnails"
            style='list-style: none; margin-left: 0px;'>
            <g:set var="i" value="${1}" />
            <g:each in="${observationInstance?.resource}" var="r">
            <li class="addedResource thumbnail">
            <%
            def imagePath = '';
            if(r) {
                imagePath = r.thumbnailUrl(Utils.getDomainServerUrlWithContext(request) + '/observations')?:null;
            }
            %>

            <div class='figure' style="height: 200px; overflow: hidden;">
                <span> <img id="image_${i}" style="width: auto; height: auto;"
                    src='${imagePath}'
                    class='geotagged_image' exif='true' /> </span>
            </div>


            <div class='metadata prop'
                style="position: relative; top: -30px;">
                <input name="file_${i}" type="hidden" value='${r.fileName}' />
                <input name="url_${i}" type="hidden" value='${r.url}' />
                <input name="type_${i}" type="hidden" value='${r.type}'/>
                <obv:rating model="['resource':r, class:'obvcreate', 'hideForm':true, index:i]"/>
                <g:if test="${r.type == ResourceType.IMAGE}">
                <div id="license_div_${i}" class="licence_div pull-left dropdown">

                    <a id="selected_license_${i}"
                        class="btn dropdown-toggle btn-mini"
                        data-toggle="dropdown">

                        <img
                        src="${resource(dir:'images/license',file:r?.licenses?.asList().first()?.name?.getIconFilename()+'.png', absolute:true)}"
                        title="Set a license for this image" />

                        <b class="caret"></b>
                    </a>

                    <ul id="license_options_${i}" class="dropdown-menu license_options">
                        <span>Choose a license</span>
                        <g:each in="${species.License.list()}" var="l">
                        <li class="license_option"
                        onclick="$('#license_${i}').val($.trim($(this).text()));$('#selected_license_${i}').find('img:first').replaceWith($(this).html());" title="${l.name.getTooltip()}">
                        <img
                        src="${resource(dir:'images/license',file:l?.name?.getIconFilename()+'.png', absolute:true)}" /><span style="display:none;">${l?.name?.value}</span> 
                        </li>
                        </g:each>
                    </ul>
                    <input id="license_${i}" type="hidden" name="license_${i}" value="${r?.licenses?.asList().first()?.name}"></input>
                </div>
                </g:if>

            </div> 
            <div class="close_button"
                onclick="removeResource(event, ${i});$('#geotagged_images').trigger('update_map');"></div>

            </li>
            <g:set var="i" value="${i+1}" />
            </g:each>
            <li id="add_file" class="addedResource" style="z-index:40">
            <div id="add_file_container">
                <div id="add_image"></div> 
                <div style="text-align:center;">
                    or
                </div> 
                <div id="add_video" class="editable"></div>
            </div>
            <div class="progress">
                <div id="translucent_box"></div>
                <div id="progress_bar"></div>
                <div id="progress_msg"></div>
            </div>

            </li>
        </ul>
        <div id="image-resources-msg" class="help-inline">
            <g:renderErrors bean="${observationInstance}" as="list"
            field="resource" />
        </div>
    </div>
</div>

