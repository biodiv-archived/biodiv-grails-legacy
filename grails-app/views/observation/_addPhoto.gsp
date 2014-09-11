<%@page import="species.Resource"%>
<%@page import="species.Resource.ResourceType"%>
<%@ page import="species.utils.Utils"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.Species"%>
<%@ page import="species.utils.ImageType"%>
<%@page import= "org.codehaus.groovy.runtime.DateGroovyMethods"%>

            <g:set var= "res" value="${resList}" />
            <g:if test="${resourceListType == 'fromRelatedObv'}">
                <g:set var="i" value="${offset-1}"/>
            </g:if>
            <g:else>
                <g:set var="i" value="${res?(res.size()-1):0}"/>
            </g:else>
            <%
                def counter = 0 
            %>
            <g:if test="${resourceListType != 'fromRelatedObv' && resourceListType != 'fromSpeciesField'}">
            <li class="add_file addedResource" >
            
            <div class="add_file_container">
                <div class="add_image"></div> 
               
                <div class="add_video editable"></div>
                
                <div class="add_audio"></div>

            </div>
            <div class="progress">
                <div class="translucent_box"></div>
                <div class="progress_bar"></div>
                <div class="progress_msg"></div>
                <div class="mediaProgressBar" style ="margin-top:117px"></div>
            </div>

            </li>
            </g:if>
            <g:each in="${res}" var="r">
            <%
                def flag19 = false
                if(resourceListType == "usersResource"){
                    def d = new Date()
                    DateGroovyMethods.clearTime(d)
                    def d1 = d - 20
                    def d2 = d - 19
                    if(d2 >= r.uploadTime && r.uploadTime >= d1){
                        flag19 = true
                    }
                }
            %>
            <li class="addedResource thumbnail" style="${flag19?'border:1px red solid' :''}">
            <%
            def imagePath = '';
                if(r) {
                if(r.context.value() == Resource.ResourceContext.OBSERVATION.toString() || r.context.value() == Resource.ResourceContext.CHECKLIST.toString()){
                    imagePath = r.thumbnailUrl(Utils.getDomainServerUrlWithContext(request) + '/observations', null, ImageType.LARGE )?:null;
                } else if(r.context.value() == Resource.ResourceContext.USER.toString()){
                    imagePath = r.thumbnailUrl(Utils.getDomainServerUrlWithContext(request) + '/usersRes', null, ImageType.LARGE)?:null;    
                } else{
                    def spFolder = grailsApplication.config.speciesPortal.resources.rootDir
                    def finalFolder = spFolder.substring(spFolder.lastIndexOf("/"), spFolder.size())
                    imagePath = r.thumbnailUrl(Utils.getDomainServerUrlWithContext(request) + finalFolder, null, ImageType.LARGE)?:null;   
                }
                }
                def resSource = r.url
                if(obvLinkList?.size()!= 0){
                    if(!r.url){
                        resSource = uGroup.createLink(action:'show', controller:'observation', 'id' : obvLinkList?.get(counter.toInteger()), 'absolute': true);
                        counter++
                    }
                }
            %>
            <div class='figure' style="height: 200px; overflow: hidden;">
                <span> <img class="image_${i} geotagged_image" style="width: auto; height: auto;"
                    src='${imagePath}' exif='true' /> </span>
            </div>


            <div class='metadata prop'
                style="position: relative; top: -30px;">
                <input class="fileName" name="file_${i}" type="hidden" value='${r.fileName}' />
                <input name="url_${i}" type="hidden" value='${r.url}' />
                <input name="type_${i}" type="hidden" value='${r.type}'/>
                <input name="date_${i}" type="hidden" value='${flag19}'/>
                <!--input name="resContext_${i}" type="hidden" value='${r.context.value()}'/-->
                
                <g:if test="${r.type != ResourceType.AUDIO}">  
                    <obv:rating model="['resource':r, class:'obvcreate', 'hideForm':true, index:i]"/>
                </g:if>
                <%
                    def licenseList = r?.licenses?.asList()
                    def firstLicense
                    if(licenseList.size() != 0){
                        firstLicense = licenseList.first()
                    }
                %>
                <g:render template="/observation/selectLicense" model="['i':i, 'selectedLicense':firstLicense]"/>
               
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
                            if(checkFlag){
                               isChecked = "" 
                            }
                        %>
                        <input class="pullImage" name="pullImage_${i}" type="checkbox" value="true" style="position: absolute;z-index: 1;top: -140px;float: right;margin-left: -81px;" ${isChecked} >
                    </g:if>
                </div>
            </div>
            </g:if>
            <input class="resId" name="resId_${i}" type="hidden" value='${r.id}'/>
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
            <img class='image_{{>i}} geotagged_image' style="width:auto; height: auto;" src='{{>thumbnail}}' exif='true'/> 
        </span>
    </div>

    <div class='metadata prop' style="position:relative; top:-30px;">
        <input class="fileName" name="file_{{>i}}" type="hidden" value='{{>file}}'/>
        <input name="url_{{>i}}" type="hidden" value='{{>url}}'/>
        <input name="type_{{>i}}" type="hidden" value='{{>type}}'/>
        
        
    {{if type != "AUDIO"}}  
        <%def r = new Resource();%>        
        <obv:rating model="['resource':r, class:'obvcreate', 'hideForm':true, index:1]"/>
    {{/if}}
        
        
        <div id="license_div_{{>i}}" class="license_div pull-left dropdown">
            <a id="selected_license_{{>i}}" class="btn dropdown-toggle" data-toggle="dropdown">
                <img src="${resource(dir:'images/license',file:'cc_by.png', absolute:true)}" title="Set a license for this image"/>
                <b class="caret"></b>
            </a>            
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

        <g:if test="${observationInstance instanceof Species}">
            <div class="imageMetadataDiv" >
                <div class="imageMetadataForm" >
                    <input name="contributor_{{>i}}" type="text" value="${currentUser?.name}" placeholder="Contributor">
                    <input name="source_{{>i}}" type="text" value="" placeholder="Source">
                    <input name="title_{{>i}}" type="text" value="" placeholder="Caption">
                    <!--input name="resContext_{{>i}}" type="hidden" value = "SPECIES"-->
                </div>
            </div>
        </g:if>
        <g:else>
            <!--input name="resContext_{{>i}}" type="hidden" value = "OBSERVATION"-->
        </g:else>	
        
   
    </div>
    <div class="close_button" onclick="removeResource(event, {{>i}});$('#geotagged_images').trigger('update_map');"></div>
    </li>

</script>

