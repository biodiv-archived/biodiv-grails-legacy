<%@page import="species.Resource.ResourceType"%>
<%@page import="species.Resource"%>
<%@page import="species.License"%>
<%@page import="species.License.LicenseType"%>
<%@page import="species.participation.Observation"%>
<%@page import="species.utils.Utils"%>
<%@page import="species.auth.SUser"%>

<html>
    <head>
        <g:set var="title" value="Observations"/>
        <g:render template="/common/titleTemplate" model="['title':title]"/>
        <r:require modules="observations_create"/>
    </head>
    <body>
        <div class="bulk_observation_create">
            <div class="span12 super-section">
                <div class="section">
                    <obv:addPhotoWrapper model="['observationInstance':observationInstance, 'userInstance':userInstance, 'resourceListType':'usersResource']"></obv:addPhotoWrapper>
                    <% 
                    /*
                    propagate karne wale options
                    */
                    %>

                </div>
            </div>
            <%

            //def obvTmpFileName = (observationInstance?.resource?.iterator()?.hasNext() ) ? (observationInstance.resource.iterator().next()?.fileName) : false 
            //def obvDir = resDir     
            //obvTmpFileName ?  obvTmpFileName.substring(0, obvTmpFileName.lastIndexOf("/")) : ""
            %>
            <form id="upload_resource" 
                title="Add a photo for this observation"
                method="post"
                class="${hasErrors(bean: observationInstance, field: 'resource', 'errors')}">

                <span class="msg" style="float: right"></span>
                <input class="videoUrl" type="hidden" name='videoUrl' value="" />
                <input type="hidden" name='obvDir' value="${obvDir}" />
                <input type="hidden" name='resType' value='${userInstance.class.name}'>
            </form>

            <%
            def form_create_resource = uGroup.createLink(action:'createResource', controller:'resource', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
            %>
            <form action="${form_create_resource}" method="post" class="createResource ${hasErrors(bean: observationInstance, field: 'resource', 'errors')}" >
                <input type="hidden" name='resType' value='${userInstance.class.name}'>
                <input class="resourceListType" type="hidden" name='resourceListType' value= />
            </form>

        </div>
        <r:script>	
        var add_file_button = '<li class="add_file addedResource" style="display:none;z-index:10;"><div class="add_file_container"><div class="add_image"></div><div class="add_video editable"></div></div><div class="progress"><div class="translucent_box"></div><div class="progress_bar"></div ><div class="progress_msg"></div ></div></li>';


        $(document).ready(function(){
            var uploadResource = new $.fn.components.UploadResource($('.bulk_observation_create'));
            <%
                if(observationInstance?.group) {
                    out << "jQuery('#group_${observationInstance.group.id}').addClass('active');";
                }
                if(observationInstance?.habitat) {
                    out << "jQuery('#habitat_${observationInstance.habitat.id}').addClass('active');";
                }
            %>
            
            initializeLanguage();
        });

        </r:script>

    </body>
</html>

