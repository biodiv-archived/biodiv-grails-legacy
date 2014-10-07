<%@page import="species.Resource.ResourceType"%>
<%@page import="species.Resource"%>
<%@page import="species.License"%>
<%@page import="species.License.LicenseType"%>
<%@page import="species.participation.Observation"%>
<%@page import="species.utils.Utils"%>
<%@page import="species.auth.SUser"%>

<html>
    <head>
        <g:set var="title" value="${g.message(code:'showusergroupsig.title.observations')}"/>
        <g:render template="/common/titleTemplate" model="['title':title]"/>
        <r:require modules="observations_create"/>
        <style>
            .imagesList{
                background-color: #fefad5;
                border-bottom: 1px solid #9E9E9E;
                border-top: 1px solid #9E9E9E;
                box-shadow: 0 2px 11px -3px inset;
                padding: 10px;
                min-height: 212px;
                height:240px;
                width:870px;
            }
            .imagesListWrapper ul {
                width:100000px;
                white-space:nowrap !important;
            }
            .imagesListWrapper ul li {
                display : inline !important;
                z-index:1;
            }
            .imagesListWrapper {
                overflow-x:scroll;
                overflow-y:hidden;
                height: 250px;
                width:890px;
            }
        </style>
    </head>
    <body>
        <div class="bulk_observation_create">
            <div class="span12 super-section">
                <div class="section">
                    <obv:addPhotoWrapper model="['observationInstance':observationInstance, 'userInstance':userInstance, 'resourceListType':'usersResource']"></obv:addPhotoWrapper>
                </div>
            </div>
            <%
            %>
            <form class="upload_resource ${hasErrors(bean: observationInstance, field: 'resource', 'errors')}" 
                title="Add a photo for this observation"
                method="post">
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

