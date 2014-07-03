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
            th {
                border:1px solid black;
            }
            td {
                border:1px solid black;
                text-align:center;
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
                    <div class="span12 super-section">
                        <div class="section">
                            <obv:addPhotoWrapper model="['observationInstance':observationInstance, 'userInstance':userInstance, 'resourceListType':'usersResource']"></obv:addPhotoWrapper>
                            <% 
                                /*
                                    propagate karne wale options
                                */
                            %>
                                    <table style="border:1px solid black;width:914px">
                                        <tr >
                                            <th>License</th>
                                            <th>Date</th>		
                                            <th>Tags</th>
                                            <th>Location</th>
                                            <th>Groups</th>
                                            <th>Propagate</th>
                                        </tr>
                                        <tr>
                                            <td><span class="propagateLicense">
                                                    <g:render template="/observation/selectLicense" model="['i':0, 'selectedLicense':null]"/>
                                                </span>
                                            </td>
                                            <td><span class="propagateDate">
                                                    <g:render template="dateInput" model="['observationInstance':observationInstance]"/>
                                                </span>
                                            </td>		
                                            <td><span class="propagateTags">
                                                    <div class="create_tags section-item" style="clear: both;">
                                                        <ul class="obvCreateTags">
                                                            <g:each in="${obvTags}" var="tag">
                                                            <li>${tag}</li>
                                                            </g:each>
                                                        </ul>
                                                    </div>
                                                </span>
                                            </td>
                                            <td><span class="propagateLocation">
                                                <%
                                                def obvInfoFeeder = lastCreatedObv ? lastCreatedObv : observationInstance
                                                %>
                                                <div>
                                                    <obv:showMapInput model="[observationInstance:obvInfoFeeder, userObservationInstanceList: totalObservationInstanceList, obvInfoFeeder:obvInfoFeeder, locationHeading:'Where did you find this observation?']"></obv:showMapInput>
                                                </div>
                                                </span>
                                            </td>
                                            <td>
                                                <span class="propagateGroups">
                                                    <button type="button" class="btn toggleGrpsDiv" > Groups </button> 
                                                    <div class="postToGrpsToggle" style="display:none;">
                                                        <g:render template="postToUserGroups" model="['observationInstance':obervationInstance]"/>
                                                    </div>
                                                </span>

                                            </td>
                                            <td>
                                                <a class="applyAll btn btn-primary"
                                                    style=" margin-right: 5px;"> Apply 
                                                </a>
                                            </td>
                                        </tr>
                                    </table>
                            <div class="miniObvWrapper">
                                <g:render template="/observation/miniObvCreateTemplate" model="['observationInstance': observationInstance]"/>
                            </div>
                            <div class="miniObvWrapper">
                            <g:render template="/observation/miniObvCreateTemplate" model="['observationInstance': observationInstance]"/>
                            </div>
                            <div class="miniObvWrapper">
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
                    <input class="resourceListType" type="hidden" name='resourceListType' value= />
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
                        $(ui.draggable).css("opacity","0.3");
                        $(form).find('.geotagged_image', this).load(function(){
                            $(form).find(".map_class").data('locationpicker').mapLocationPicker.update_geotagged_images_list($(this));		
                        });
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
