<%@page import="species.Resource.ResourceType"%>
<%@page import="species.Resource"%>
<%@page import="species.License"%>
<%@page import="species.License.LicenseType"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.utils.Utils"%>
<%@page import="species.auth.SUser"%>

<html>
    <head>
        <g:set var="title" value="Observations"/>
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
            td {
                text-align:center;
            }
            .map_class {
                position:relative;
            }
            .map_canvas {
                border-color: rgba(82,168,236,0.8) !important;
                position: absolute !important;
                width: 99% !important;
                display: block;
                left: 0px;
                z-index:10 !important;
            }
            .selected_habitat, .selected_group{
                position:relative;
            }
            .propagateBlock .group_options, .propagateBlock .habitat_options {
                width : 218px;
            }
            .addObservation .group_options, .addObservation .habitat_options {
                width : 287px;
            }
            .userGroupsSuperDiv{
                position:absolute !important;
                z-index:10;
                background-color: rgb(106, 201, 162) !important;
            }
            .column.block_row {
                width:444px;
            }
            .miniObvWrapper.column {
                width:290px;
                margin:2px;
                height:auto;
                background-color: #a6dfc8;
            }
            .column {
                width:221px;
                float: left;
                padding: 10px 0px;
                margin:0px;
                border: 1px solid #c6c6c6;
                border-collapse: separate;
                border-left: 0;
                -webkit-border-radius: 4px;
                -moz-border-radius: 4px;
                border-radius: 4px;
                height:70px;
            }
            .selected_habitat, .selected_group {
                padding: 4px 3px;
                width:97%;
                text-align:center !important;
            }
            .block_row {
                margin-left:0px !important;
            }
            .section label {
                padding: 0px; 
            }
            .help-identify{
                left:21px !important;
            }
            .propagateBlock {
                background-color: #a6dfc8;
            }
            .placeName{
                width:90% !important;
            }
            .map_search .latlng {
                z-index:2;
                width:100%;
            }
            .map_search .latlng .control_group {
                margin-bottom:0px;
            }
            .latlng .input-prepend {
                width:100%;
            }
            .map_search {
                position:inherit;btn:hover,
            }
            .combobox-container {
	        right:9% !important;
	
            }
            .addObservation .wrapperParent {
                width:96% !important; 
            }
            input[type="text"]:focus {
                border-width:2px;
            }
            .propagateBlock .groups_super_div, .propagateBlock .habitat_super_div {
                width:200px;
            }
            .addObservation .groups_super_div, .addObservation .habitat_super_div {
                width : 273px;
            }
            .combobox-container .add-on {
                height: 22px !important;
                left: 68px !important;
            }
            .small_block_row {
                width:295px;
                margin-left:0px;
            }
            .controls.textbox {
                height:40px !important;
            }
            .imagesListWrapper {
                overflow-x:scroll;
                overflow-y:hidden;
                height: 250px;
                width:890px;
            }
            .imageHolder {
                border: 1px solid grey ;
                background-color: lightgrey ;
                margin-bottom:90px !important;
            }
            .imagesListWrapper ul {
                width:20000px;
                white-space:nowrap !important;
            }
            .imagesListWrapper ul li {
                display : inline !important;
                z-index:1;
            }
            li.group_option span, li.habitat_option span {
                padding: 0px;
                float: left;
            }
            li.group_option, li.habitat_option {
                height: 35px;
            }
            .display_value {
                display: inline-block !important;
                margin-top: 4px !important;
            }
            .selected_group .caret, .selected_habitat .caret , .propagateLicense .caret {
                margin-top: 12px !important;
            }
            .propagateDate .fromDate {
                height:42px;
            }
            .propagateLicense .license_div {
                margin: 0 auto !important;
            }
            .propagateLicense a{
                height:32px !important;
                width:89% !important;
            }
            .propagateLicense .btn img{
                margin-top: 8px !important; 
            }
            .propagateLicense .license_options {
                width:220px;
            }
        </style>
    </head>
    <body>
        <div class="bulk_observation_create">
            <div class="span12">
                <g:render template="/observation/addObservationMenu" model="['entityName':(params.action == 'edit' || params.action == 'update')?'Edit Observation':'Add Multiple Observations']"/>
                <%
                def form_id = "addBulkObservations"
                def form_action = uGroup.createLink(action:'saveBulkObservations', controller:'observation', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
                def form_button_name = "Submit & and more"
                def form_button_val = "Submit & Add More"
                def submitFinish = "Submit & Finish"
                %>
                <div class="super-section">
                    <div class="section">
                        <obv:addPhotoWrapper model="['observationInstance':observationInstance, 'userInstance':userInstance, 'resourceListType':'usersResource']"></obv:addPhotoWrapper>
                    </div>
                    <div class="section clearfix">
                        <a class="btn btn-primary togglePropagateDiv"> Show Bulk Action <b class="caret"></b></a>
                        <div class= "propagateBlock hide clearfix">
                            <div >
                                <div class="column propagateLicense">
                                    <label>License</label>
                                    <g:render template="/observation/selectLicense" model="['i':0, 'selectedLicense':License.findByName("CC_BY")]"/>
                                </div>
                                <div class="column propagateGrpHab">
                                    <g:render template="/common/speciesGroupDropdownTemplate" model="['observationInstance':observationInstance]"/> 
                                </div>
                                <div class="column propagateGrpHab">
                                    <g:render template="/common/speciesHabitatDropdownTemplate" model="['observationInstance':observationInstance]"/> 
                                </div>
                                <div class="column propagateDate">
                                    <g:render template="dateInput" model="['observationInstance':observationInstance]"/>
                                </div>
                            </div>
                            <div>
                                <div class="column span6 propagateLocation block_row">
                                    <%
                                    def obvInfoFeeder = lastCreatedObv ? lastCreatedObv : observationInstance
                                    %>
                                    <div>
                                        <obv:showMapInput model="[observationInstance:obvInfoFeeder, userObservationInstanceList: totalObservationInstanceList, obvInfoFeeder:obvInfoFeeder, locationHeading:'Where did you find this observation?']"></obv:showMapInput>
                                    </div>
                                </div>
                                <div class="column span6 propagateTags block_row">
                                    <label>Tags</label>
                                    <div class="create_tags" style="clear: both;">
                                        <ul class="obvCreateTags">
                                            <g:each in="${obvTags}" var="tag">
                                                <li>${tag}</li>
                                            </g:each>
                                        </ul>
                                    </div>
                                </div>
                                
                            </div>
                            <div>
                                <div class="column propagateGroups small_block_row">
                                    <label>User Groups</label>
                                    <div style="clear:both">
                                        <button type="button" class="btn toggleGrpsDiv"> User Groups</button> 
                                        <div class="postToGrpsToggle" style="display:none;">
                                            <g:render template="postToUserGroups" model="['observationInstance':obervationInstance]"/>
                                        </div>
                                    </div>
                                </div>
                                <div class="column propagateHelpID small_block_row">
                                    <label>Help Identify</label>
                                    <div style="margin-left:40px;clear:both">
                                        <input class="helpID" type="checkbox">
                                    </div>
                                </div>
                                <div class="column small_block_row" style="text-align:center">
                                    <a class="applyAll btn btn-primary"
                                        style=" margin-right: 5px; margin-top:17px;"> Apply Below
                                    </a>
                                    <a class="applyAll applyToAll btn btn-primary"
                                        style=" margin-right: 5px; margin-top:17px;"> Apply to All
                                    </a>
                                     <input type="hidden" name='applyToAll' value="" />
                                </div>
                            </div>
                        </div>

                    </div>

                        <div class="section clearfix">
                            <div class="miniObvWrapper column">
                                <g:render template="/observation/miniObvCreateTemplate" model="['observationInstance': observationInstance]"/>
                            </div>
                            <div class="miniObvWrapper column">
                            <g:render template="/observation/miniObvCreateTemplate" model="['observationInstance': observationInstance]"/>
                            </div>
                            <div class="miniObvWrapper column">
                                <g:render template="/observation/miniObvCreateTemplate" model="['observationInstance': observationInstance]"/>
                            </div>
                        </div>
                    </div>
                    <a id="addBulkObservationsAndListPage" class="btn btn-primary"
                        style="float: right; margin-right: 5px;"> ${submitFinish}
                    </a>
                    <a id="addBulkObservationsSubmit" class="btn btn-primary"
                        style="float: right; margin-right: 5px;"> ${form_button_val} 
                    </a>

                    <div class="control-group">
                        <label class="checkbox" style="text-align: left;"> 
                            <g:checkBox style="margin-left:0px;"
                            name="agreeTerms" value="${observationInstance?.agreeTerms}"/>
                            <span class="policy-text"> By submitting this form, you agree that the photos or videos you are submitting are taken by you, or you have permission of the copyright holder to upload them on creative commons licenses. </span></label>
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
                    <input class="resourceListType" type="hidden" name='resourceListType' value=>
                </form>

            </div>
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
            $(".togglePropagateDiv").click(function(){
                $(".propagateBlock").slideToggle("")
            });
            $(".propagateGrpHab .help-inline").css("display","none");
        
            if($(".userGroupsSuperDiv").hasClass("span12")){
                $(".userGroupsSuperDiv").removeClass("span12");
                $(".userGroupsSuperDiv").addClass("span4");
            } 
        
            if($( "input[name='resType']" ).val() == "species.auth.SUser") {
                $(".addedResource.thumbnail").draggable({helper:'clone'});  

                $(".imageHolder").droppable({
                    accept: ".addedResource.thumbnail",
                    drop: function(event, ui){
                        dropAction(event,ui,this); 
                    }
                });
            }
            initializeLanguage();
            
            $(document).click(function(){
                $(".group_options").hide();
                $(".habitat_options").hide();
            });

            /* Clicks within the dropdown won't make
            it past the dropdown itself */
            $(".group_options").click(function(e){
                e.stopPropagation();
            });
            $(".habitat_options").click(function(e){
                e.stopPropagation();
            });
        });

        </r:script>

    </body>
</html>
