<%@page import="species.Resource.ResourceType"%>
<%@page import="species.Resource"%>
<%@page import="species.utils.ImageType"%>
<%@page	import="org.springframework.web.context.request.RequestContextHolder"%>
<%@page import="species.License"%>
<%@page import="species.License.LicenseType"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<%@ page import="org.grails.taggable.Tag"%>
<%@ page import="species.utils.Utils"%>
<%@page import="species.Resource.ResourceType"%>
<%@page import="java.util.Arrays"%>

<html>
    <head>
        <g:set var="title" value="${g.message(code:'default.checklist.label')}"/>
        <g:render template="/common/titleTemplate" model="['title':title]"/>
        <style>
            .upload_file div {
                display:inline-block;
            }
        </style>
        <asset:javascript src="slickgrid.js"/>
    </head>
    <body>
        <div class="observation_create">
            <div class="span12">
                <g:render template="/observation/addObservationMenu" model="['entityName':(params.action == 'edit' || params.action == 'update')?'Edit List':'Add List']"/>
                <g:render template="/checklist/addChecklist"/>
            </div>
        </div>

        <asset:script>
        $(document).ready(function(){
            
            intializesSpeciesHabitatInterest(false);
                        <%
            if(observationInstance?.group) {
            out << "jQuery('#group_${observationInstance.group.id}').addClass('active');";
            }
            if(observationInstance?.habitat) {
            out << "jQuery('#habitat_${observationInstance.habitat.id}').addClass('active');";
            }
            %>
            function initBlankSpreadsheet() {
            
            var rowDataForBlankSheet = new Array();
            for (var i =0 ; i<10 ; i++ ){
                rowDataForBlankSheet.push({S_No:"",Scientific_Name:"",Common_Name:""});
            }
            var columnDataForBlankSheet = [{id: "S_No", name:window.i8ln.species.parseUtil.sno, field:"S_No",editor: Slick.Editors.Text, width:50},
                {id: "Scientific_Name", name: window.i8ln.species.parseUtil.snu, field: "Scientific_Name",editor: Slick.Editors.Text,  width:150, header:getHeaderMenuOptions()},
                {id: "Common_Name", name: window.i8ln.species.parseUtil.cnu, field: "Common_Name",editor: Slick.Editors.Text,  width:150, header:getHeaderMenuOptions()}
                ]

            columnDataForBlankSheet.push(getMediaColumnOptions());
            loadDataToGrid(rowDataForBlankSheet, columnDataForBlankSheet, "checklist", "Scientific_Name", "Common_Name"); 
            }

            if(${params.action=="create"}){
                initBlankSpreadsheet();
                }

            $("#textAreaSection").show();
            $("#parseNames").click(function(){
                $("#textAreaSection").hide();
            });
            $('#addResourcesModal').modal({show:false});
            $("#tab_grid").click(function(){
                $("#gridSection").show();
                $("#addNames").hide();
            });
            $("#tab_up_file").click(function(){
                $("#gridSection").hide();
                $("#addNames").hide();
            });
            $("#tab_type_list").click(function(){
                $("#gridSection").hide();
                $('#checklistColumns').val('');
                $("#checklistData").val('');
                $("#addNames").show();
            });
            var uploadResource = new $.fn.components.UploadResource($('.observation_create'), {
                POLICY : "${policy}",
                SIGNATURE :"${signature}" 
            });
            $('.filePicker').data('uploadResource', uploadResource);

        });
        </asset:script>

    </body>
</html>
