<%@page import="species.Resource"%>
<%@page import="species.Resource.ResourceType"%>
<%@ page import="species.utils.Utils"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.Species"%>

            <g:set var= "res" value="${resList}" />
            <g:if test="${resourceListType == 'fromRelatedObv'}">
                <g:set var="i" value="${1+offset}"/>
            </g:if>
            <g:else>
                <g:set var="i" value="${res?.size()?:1}"/>
            </g:else>
            <%
                def counter = 0 
            %>
            <g:if test="${resourceListType != 'fromRelatedObv' && resourceListType != 'fromSpeciesField'}">
            <li class="add_file addedResource">
            
            <div class="add_file_container">
                <div class="add_image"></div> 
                <div style="text-align:center;">
                    or
                </div> 
                <div class="add_video editable"></div>
            </div>
            <div class="progress">
                <div class="translucent_box"></div>
                <div class="progress_bar"></div>
                <div class="progress_msg"></div>
            </div>

            </li>
            </g:if>
            <g:each in="${res}" var="r">
            <li class="addedResource thumbnail">
            <%
            def imagePath = '';
            if(r) {
            if(r.context.value() == Resource.ResourceContext.OBSERVATION.toString() || r.context.value() == Resource.ResourceContext.CHECKLIST.toString()){
                imagePath = r.thumbnailUrl(Utils.getDomainServerUrlWithContext(request) + '/observations')?:null;
            }else{
                def spFolder = grailsApplication.config.speciesPortal.resources.rootDir
                def finalFolder = spFolder.substring(spFolder.lastIndexOf("/"), spFolder.size())
                imagePath = r.thumbnailUrl(Utils.getDomainServerUrlWithContext(request) + finalFolder)?:null;   
            }
            }
            def resSource = r.url
            if(obvLinkList.size()!= 0){
                if(!r.url){
                    resSource = uGroup.createLink(action:'show', controller:'observation', 'id' : obvLinkList.get(counter.toInteger()), 'absolute': true);
                    counter++
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
                <!--input name="resContext_${i}" type="hidden" value='${r.context.value()}'/-->
                <obv:rating model="['resource':r, class:'obvcreate', 'hideForm':true, index:i]"/>
                <g:if test="${r.type == ResourceType.IMAGE}">
                <g:render template="/observation/selectLicense" model="['i':i, 'selectedLicense':r?.licenses?.asList()?.first()]"/>
                </g:if>
                <g:if test="${observationInstance instanceof Species}">
                <div class="imageMetadataDiv" >
                <div class="imageMetadataForm" >
                    <input name="contributor_${i}" type="text" value="${r.contributors.name.join(',')}" placeholder="Contributor">
                    <input name="source_${i}" type="text" value="${resSource}" placeholder="Source">
                    <input name="title_${i}" type="text" value="${r.description}" placeholder="Caption">
                    <g:if test="${resourceListType == 'fromRelatedObv' || resourceListType == 'fromSpeciesField'}">
                        <%
                            def isChecked = ""
                            def resAlreadyPres = observationInstance.resources.id.asList()
                            if(resAlreadyPres.contains(r.id)){
                                isChecked = "checked"
                            }
                        %>
                        <input name="pullImage_${i}" type="checkbox" value="true" style="float:right; margin-right:-48px; margin-top:-23px;" ${isChecked} >
                        <input name="resId_${i}" type="hidden" value='${r.id}'/>
                    </g:if>
                </div>
            </div>
            </g:if>
            </div> 
            <div class="close_button"
                onclick="removeResource(event, ${i});$('#geotagged_images').trigger('update_map');"></div>

            </li>
            <g:set var="i" value="${i-1}" />
            </g:each>
            
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
                <div class="imageMetadataForm" >
                    <input name="contributor_{{>i}}" type="text" value="" placeholder="Contributor">
                    <input name="source_{{>i}}" type="text" value="" placeholder="Source">
                    <input name="title_{{>i}}" type="text" value="" placeholder="Caption">
                    <!--input name="resContext_{{>i}}" type="hidden" value = "SPECIES"-->
                </div>
            </div>
            </g:if>
            <g:else>
                <!--input name="resContext_{{>i}}" type="hidden" value = "OBSERVATION"-->
            </g:else>
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

