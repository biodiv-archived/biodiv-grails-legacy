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
                <div class="add_image"><span class="add_resource_text"><g:message code="default.resource.add.image.label" /></span></div> 
               
                <div class="add_video editable"><span class="add_resource_text"><g:message code="default.resource.add.video.label" /></span></div>
                
                <div class="add_audio"><span class="add_resource_text"><g:message code="default.resource.add.audio.label" /></span></div>

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
                def validUrl = true;
                if(resourceListType == "usersResource"){
                    def d = new Date()
                    DateGroovyMethods.clearTime(d)
                    def d1 = d - 20
                    def d2 = d - 19
                    if(d2 >= r.uploadTime && r.uploadTime >= d1){
                        flag19 = true
                    }
                }
                def imagePath = '';
                String domainServerUrlwithContext = Utils.getDomainServerUrlWithContext(request) ;
                if(r) {
                if(r.context.value() == Resource.ResourceContext.OBSERVATION.toString() || r.context.value() == Resource.ResourceContext.CHECKLIST.toString()){
                    imagePath = r.thumbnailUrl(domainServerUrlwithContext + '/observations', null, ImageType.LARGE )?:null;
                } else if(r.context.value() == Resource.ResourceContext.USER.toString()){
                    imagePath = r.thumbnailUrl(domainServerUrlwithContext + '/usersRes', null, ImageType.LARGE)?:null;    
                } else{
                    def spFolder = grailsApplication.config.speciesPortal.resources.rootDir
                    def finalFolder = spFolder.substring(spFolder.lastIndexOf("/"), spFolder.size())
                    imagePath = r.thumbnailUrl(domainServerUrlwithContext + finalFolder, null, ImageType.LARGE)?:null;   
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
            <li class="addedResource thumbnail" style="${flag19?'border:1px red solid' :''}">
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
                def firstLicense = r?.license
                def listType="${resourceListType}"
                def resAlreadyPres = observationInstance?.hasProperty('resources')?observationInstance?.resources?.id?.asList():observationInstance?.resource?.id?.asList()
                def isEditable = (r?.context.value() == 'SPECIES' || (resourceListType == 'ofObv' || resourceListType == 'usersResource'))? true : false
                %>
                <g:render template="/observation/selectLicense" model="['i':i, 'selectedLicense':firstLicense, 'isEditable':isEditable]"/>
               
                <g:if test="${observationInstance instanceof Species}">
                <div class="imageMetadataDiv" >
                <div class="imageMetadataForm" >
           
                    <input name="contributor_${i}" ${(r.context.value()== 'OBSERVATION')?'disabled' :''} type="text" value="${r.contributors.name.join(',')}" placeholder="${g.message(code:'placeholder.contributor')}">
                    <input name="source_${i}" type="text" value="${resSource}" placeholder="${g.message(code:'placeholder.source')}" ${(r.context.value()== 'OBSERVATION')?'disabled' :''}>
                    <input name="title_${i}" type="text" value="${r.description}" placeholder="${g.message(code:'placeholder.caption')}">


                    <g:if test="${resourceListType == 'fromRelatedObv' || resourceListType == 'fromSpeciesField'}">
                             <%
                            def isChecked = ""
                            if(resAlreadyPres.contains(r.id)){
                                isChecked = "checked"
                            }
                            if(checkFlag){
                               isChecked = "" 
                            }
                        %>
                        <input class="pullImage" name="pullImage_${i}" type="checkbox" value="true" ${isChecked} >
                    </g:if>
                 
                </div>
            </div>
            </g:if>
            <g:else>
                <input name="title_${i}" type="text" value="${r.description}" placeholder="${g.message(code:'placeholder.caption')}">
            </g:else>
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
                <img src="${assetPath(src:'/all/license/'+'cc_by.png', absolute:true)}" title="${g.message(code:'title.set.license')}"/>
                <b class="caret"></b>
            </a>            
                <ul id="license_options_{{>i}}" class="dropdown-menu license_options">
                <span><g:message code="default.choose.license.label" /></span>
                <g:each in="${species.License.list()}" var="l">
                    <li class="license_option" onclick="selectLicense($(this), {{>i}})">
                    <img src="${assetPath(src:'/all/license/'+l?.name.getIconFilename()+'.png', absolute:true)}"/><span style="display:none;">${l?.name?.value}</span>
                </li>
                </g:each>
            </ul>
            <input id="license_{{>i}}" type="hidden" name="license_{{>i}}" value="CC BY"></input>
        </div>

        <g:if test="${observationInstance instanceof Species}">
            <div class="imageMetadataDiv" >
                <div class="imageMetadataForm" >
                    <input name="contributor_{{>i}}" type="text" value="${currentUser?.name}"  placeholder="${g.message(code:'placeholder.contributor')}">
                    <input name="source_{{>i}}" type="text" value="" placeholder="${g.message(code:'placeholder.source')}">
                    <input name="title_{{>i}}" type="text" value="" placeholder="${g.message(code:'placeholder.caption')}">
                    <!--input name="resContext_{{>i}}" type="hidden" value = "SPECIES"-->
                </div>
            </div>
        </g:if>
        <g:else>
            <!--input name="resContext_{{>i}}" type="hidden" value = "OBSERVATION"-->
            <input name="title_{{>i}}" type="text" value="" placeholder="${g.message(code:'placeholder.caption')}">
        </g:else>   
        
   
    </div>
    <div class="close_button" onclick="removeResource(event, {{>i}});$('#geotagged_images').trigger('update_map');"></div>
    </li>
</script>
