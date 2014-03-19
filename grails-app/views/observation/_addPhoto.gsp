<%@page import="species.Resource"%>
<%@page import="species.Resource.ResourceType"%>
<%@ page import="species.utils.Utils"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.Species"%>

<div>
    <i class="icon-picture"></i><span>Upload photos of a
        single observation and species and rate images inorder to order them.</span>


    <div
        class="resources control-group ${hasErrors(bean: observationInstance, field: 'resource', 'error')}">
        <ul id="imagesList" class="thumbwrap thumbnails"
            style='list-style: none; margin-left: 0px;'>
            <g:set var="i" value="${1}" />
            <g:if test="${observationInstance instanceof Observation}">
            <g:set var= "res" value="${observationInstance?.resource}" />
            </g:if>
            <g:else>
            <g:set var= "res" value="${observationInstance?.resources}" />
            </g:else>
            <g:each in="${res}" var="r">
            <li class="addedResource thumbnail">
            <%
            def imagePath = '';
            if(r) {
            if(observationInstance instanceof Observation){
                imagePath = r.thumbnailUrl(Utils.getDomainServerUrlWithContext(request) + '/observations')?:null;
            }else{
                def spFolder = grailsApplication.config.speciesPortal.resources.rootDir
                def finalFolder = spFolder.substring(spFolder.lastIndexOf("/"), spFolder.size())
                imagePath = r.thumbnailUrl(Utils.getDomainServerUrlWithContext(request) + finalFolder)?:null;   
            }
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
                <g:render template="/observation/selectLicense" model="['i':i, 'selectedLicense':r?.licenses?.asList().first()]"/>
                </g:if>
                <g:if test="${observationInstance instanceof Species}">
                <div class="imageMetadataDiv" >
                <i class="imageMetadataInfo icon-edit"></i>
                <div class="imageMetadataForm" style="display:none;">
                    <input name="contributor_${i}" type="text" value="${r.contributors.name.join(',')}" placeholder="Contributor">
                    <input name="source_${i}" type="text" value="${r.url}" placeholder="Source">
                    <input name="title_${i}" type="text" value="${r.description}" placeholder="Caption">
                </div>
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
<!--====== Template ======-->
<script id="metadataTmpl" type="text/x-jquery-tmpl">
    <li class="addedResource thumbnail addedResource_{{>i}}">
    <div class='figure' style='height: 200px; overflow:hidden;'>
        <span> 
            <img id='image_{{>i}}' style="width:auto; height: auto;" src='{{>thumbnail}}' class='geotagged_image' exif='true'/> 
        </span>
    </div>

    <div class='metadata prop' style="position:relative; top:-30px;">
        <input name="file_{{>i}}" type="hidden" value='{{>file}}'/>
        <input name="url_{{>i}}" type="hidden" value='{{>url}}'/>
        <input name="type_{{>i}}" type="hidden" value='{{>type}}'/>
        
        <%def r = new Resource();%>
        <obv:rating model="['resource':r, class:'obvcreate', 'hideForm':true, index:1]"/>

        {{if type == '${ResourceType.IMAGE}'}}
        <div id="license_div_{{>i}}" class="licence_div pull-left dropdown">
            <a id="selected_license_{{>i}}" class="btn dropdown-toggle btn-mini" data-toggle="dropdown">
                <img src="${resource(dir:'images/license',file:'cc_by.png', absolute:true)}" title="Set a license for this image"/>
                <b class="caret"></b>
            </a>
            <g:if test="${observationInstance instanceof Species}">
            <div >
            <i class="imageMetadataInfo icon-edit"></i>
                <div class="imageMetadataForm" style="display:none;">
                    <input name="contributor_${i}" type="text" value="" placeholder="Contributor">
                    <input name="source_${i}" type="text" value="" placeholder="Source">
                    <input name="title_${i}" type="text" value="" placeholder="Caption">
                </div>
            </div>
            </g:if>
                <ul id="license_options_{{>i}}" class="dropdown-menu license_options">
                <span>Choose a license</span>
                <g:each in="${species.License.list()}" var="l">
                <li class="license_option" onclick="selectLicense($(this), {{>i}})">
                <img src="${resource(dir:'images/license',file:l?.name.getIconFilename()+'.png', absolute:true)}"/><span style="display:none;">${l?.name?.value}</span>
                </li>
                </g:each>
            </ul>
            <input id="license_{{>i}}" type="hidden" name="license_{{>i}}" value="CC BY"></input>
        </div>	
        {{/if}}
   
    </div>
    <div class="close_button" onclick="removeResource(event, {{>i}});$('#geotagged_images').trigger('update_map');"></div>
    </li>

</script>
<r:script>
    $(document).ready(function(){
    filepicker.setKey("${grailsApplication.config.speciesPortal.observations.filePicker.key}");
        $(".imageMetadataInfo").click(function(){
            console.log("clicked");
            $(this).closest("div").find(".imageMetadataForm").toggle();
        });
    });
</r:script>
