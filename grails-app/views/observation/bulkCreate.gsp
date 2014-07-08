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
            }
            td {
                text-align:center;
            }
            .map_canvas {
                position: absolute;
                width: 100%;
                display: block;
                left: 0px;
            }
            .map_search {
                position:inherit;
            }
            .selected_habitat, .selected_group{
                position:relative;
            }
            .habitat_options, .group_options{
                position:absolute;
                z-index:10;
            }
            .userGroupsSuperDiv{
                position:absolute !important;
                z-index:10;
            }
            .section {
                overflow:auto;
            }
            .column.block_row {
                width:442px;
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
                text-align:left;
            }
            .block_row {
                margin-left:0px;
            }
            .section label {
                padding: 0px; 
            }
            .help-identify{
                left:10px;
            }
            .propagateBlock {
                clear:both;
                background-color: #a6dfc8;
                overflow:auto;
            }
        </style>
    </head>
    <body>
        <div class="bulk_observation_create">
            <div class="span12">
                <%
                def form_id = "addBulkObservations"
                def form_action = uGroup.createLink(action:'saveBulkObservations', controller:'observation', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
                def form_button_name = "Submit All"
                def form_button_val = "Submit All"
                %>
                <div class="super-section">
                    <div class="section">
                        <obv:addPhotoWrapper model="['observationInstance':observationInstance, 'userInstance':userInstance, 'resourceListType':'usersResource']"></obv:addPhotoWrapper>
                    </div>
                    <div class="section">
                        <div>
                            <a class="btn togglePropagateDiv"
                                style="float: left; margin-right: 5px;"> Show Bulk Action <b class="caret"></b>
                            </a>
                        </div>
                        <div class= "propagateBlock hide">
                            <div >
                                <div class="column propagateLicense">
                                    <label>License</label>
                                    <g:render template="/observation/selectLicense" model="['i':0, 'selectedLicense':null]"/>
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
                                <div class="column span6 propagateLocation block_row" style="width:440px;">
                                    <%
                                    def obvInfoFeeder = lastCreatedObv ? lastCreatedObv : observationInstance
                                    %>
                                    <div>
                                        <obv:showMapInput model="[observationInstance:obvInfoFeeder, userObservationInstanceList: totalObservationInstanceList, obvInfoFeeder:obvInfoFeeder, locationHeading:'Where did you find this observation?']"></obv:showMapInput>
                                    </div>
                                </div>
                            </div>
                            <div>
                                <div class="column span6 propagateGroups block_row">
                                    <label>User Groups</label>
                                    <div style="clear:both">
                                        <button type="button" class="btn toggleGrpsDiv"> Groups </button> 
                                        <div class="postToGrpsToggle" style="display:none;">
                                            <g:render template="postToUserGroups" model="['observationInstance':obervationInstance]"/>
                                        </div>
                                    </div>
                                </div>
                                <div class="column span6 block_row">
                                    <a class="applyAll btn btn-primary"
                                        style=" margin-right: 5px;"> Apply 
                                    </a>
                                </div>
                            </div>
                        </div>

                    </div>

                        <div class="section">
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
                    <a class="btn btn-primary" href="${uGroup.createLink(controller:'observation', action:'list','userGroup':userGroup, absolute:true)}"
                        style="float: right; margin-right: 5px;"> Show List Page 
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
        /*
            if($(".userGroupsSuperDiv").hasClass("span12")){
                $(".userGroupsSuperDiv").removeClass("span12");
                $(".userGroupsSuperDiv").addClass("span4");
            } 
        */
            if($( "input[name='resType']" ).val() == "species.auth.SUser") {
                $(".addedResource.thumbnail").draggable({helper:'clone'});  

                $(".imageHolder").droppable({
                    accept: ".addedResource.thumbnail",
                    drop: function(event,ui){
                        console.log("Item was Dropped");
                        $(this).append($(ui.draggable).clone());
                        var draggedImages = $(this).find(".addedResource");
                        var countOfImages = draggedImages.length;
                        if(countOfImages == 1){
                            console.log("FIRST FIRST");
                            draggedImages.css({
                                "position":"relative",
                                "top":"0"
                            });

                        } else{
                            console.log("SECOND SECOND");
                            var lastTop = parseInt($(draggedImages[(countOfImages - 2)]).css("top"));
                            draggedImages.last().css({
                                "position":"absolute",
                                "top":lastTop + 20
                            });

                        }
                        console.log($(ui.draggable));
                        $(this).find(".star_obvcreate").children().remove();
                        var form = $(this).closest(".addObservation");
                        var $ratingCont = $(this).find(".star_obvcreate");
                        console.log($ratingCont);
                        rate($ratingCont);
                        var imageID = $(ui.draggable).find("img").first().attr("class").split(" ")[0];
                        $("."+imageID).first().mousedown(function(){console.log("mouse down");return false;});
                        $(ui.draggable).appendTo(".imagesList");
                        $(ui.draggable).css("opacity","0.3");
                        $(form).find(".address").trigger('click');
                        $(".imageHolder .addedResource").click(function(){
                            console.log("changing z-index");
                            form.find(".addedResource").css('z-index','0')
                            $(this).css('z-index','1');
                        });

                    }
                });
                            }
            initializeLanguage();
        });

        </r:script>

    </body>
</html>
