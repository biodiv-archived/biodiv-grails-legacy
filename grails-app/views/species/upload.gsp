<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.ImageUtils"%>
<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>
<%@page import="species.Species"%>
<%@page import="species.groups.SpeciesGroup"%>
<%@page import="species.utils.Utils"%>
<html>
    <head>
        <g:set var="title" value="Species"/>
        <g:render template="/common/titleTemplate" model="['title':title]"/>
        <r:require modules="species_upload" />
    </head>
    <body>
        <div id="uploadSpeciesDiv">
            <s:showSubmenuTemplate model="['entityName':'Species Upload']" />
                <uGroup:rightSidebar/>
                    <div class="super-section span12" style="margin-left: 0px;">
                        <div class="section">
                            <!--div class="control-group">
                                <input type="radio" name="isSimpleSheet" value="true" checked="checked">Simple Sheet &nbsp; &nbsp;&nbsp;
                                <input type="radio" name="isSimpleSheet" value="false">Mapped Sheet
                            </div-->
                            <div
                                class="control-group">
                                <label class="control-label span2" for="speciesFile"> Species File <span
                                        class="req">*</span></label>
                                <div class="controls span9" style="">
                                    <% def allowedExtensions = '[ "xls", "xlsx"]'  %>
                                    <g:render template='/UFile/docUpload'
                                    model="['name': 'speciesfile', 'path': speciesFile?.path, 'size':speciesFile?.size,'allowedExtensions':allowedExtensions, 'fileParams':['uploadDir':'species','fileConvert' : true,'fromChecklist' : false], uploadCallBack:'viewGrid()']" />
                                    <div class="help-inline">
                                    </div>
                                </div>
                            </div>
                    
                            <!--div
                                class="control-group">
                                <label class="control-label span2" for="mappingFile"> Mapping File</label>
                                <div class="controls span9" style="">
                                    <g:render template='/UFile/docUpload'
                                    model="['name': 'speciesmappingfile', 'path': mappingFile?.path, 'size':mappingFile?.size,'allowedExtensions':allowedExtensions, 'fileParams':['uploadDir':'species', 'fileConvert': true]]" />
                                    <div class="help-inline">
                                    </div>
                                </div>
                            </div-->

                            <div
                                class="control-group">
                                <label class="control-label span2" for="imagesDir"> Images Url</label>

                                <div class="controls span9">
                                    <input id="imagesDir" type="text" class="input-block-level" name="imagesDir"
                                    placeholder="Enter URL for the images"
                                    value="${imagesDir}" />
                                </div>
                            </div>

                            <!--div
                                class="control-group">
                                <label for="contributors" class="control-label span2"><g:message
                                    code="contributors.label" default="Contributors" /> <span
                                        class="req">*</span></label>
                                <div class="controls span9 textbox">
                                    <sUser:selectUsers model="['id':contributor_autofillUsersId]" />
                                    <input type="hidden" name="contributorIds" id="contributorIds" />
                                </div>
                            </div-->
                        </div>
                    </div>
                   

                    

                <div id="speciesGridSection" style="clear:both;" class="section checklist-slickgrid ${params.action=='upload'?'hide':''}">
                    %{--<span id="addNewColumn" class="btn-link">+ Add New Column</span>--}%
                    <!--span class="help-inline"> (Mark scientific and common name column using <img src='${createLinkTo(file:"dropdown_active.gif", base:grailsApplication.config.speciesPortal.resources.serverURL)}'/>)</span-->

                    <div id="myGrid" class="" style="width:100%;height:350px;clear:both;"></div>
                    <div id="nameSuggestions" style="display: block;"></div>
                    <div id="legend" class="hide">
                        <span class="incorrectName badge">Incorrect Names</span>
                    </div>

                    <div class="section" style="clear:both;margin:0;">
                        <div class="row control-group ${hasErrors(bean: observationInstance, field: 'sciNameColumn', 'error')}">
                            <!--span class="pull-left span3"><g:message
                            code="observation.mark.sciNameColumn.label" default="Marked Scientific & Common Name Columns:" /></span-->
                            <div class="controls">
                                <%-- <input type="hidden" id="sciNameColumn" class="markColumn" name="sciNameColumn" value="${observationInstance.sciNameColumn}"/>
                                <input type="hidden" id="commonNameColumn" class="markColumn" name="commonNameColumn" value="${observationInstance.commonNameColumn}"/>
                                --%>
                                <div class="help-inline">
                                    <g:hasErrors bean="${observationInstance}" field="sciNameColumn">
                                    <g:message code="checklist.scientific_name.validator.invalid" />
                                    </g:hasErrors>
                                </div>

                            </div>

                        </div>	
                    </div> 
                    <a id="parseNames" class="btn btn-primary"
                        style="float: right; margin: 5px;display:none;">Validate Names</a>

                </div>
                
                </table>            
                <div id="tagHeaders" class="section checklist-slickgrid" style="clear:both;display:none;">
                    <table id="tableHeader" border="1">
                        <th class="columnName">Column_Name</th>
                        <th>Data_Columns</th>
                        <th>Header</th>
                        <th>Append</th>
                        <!--th>Group</th-->
                        <th>Delimiter</th>
                        <th>Images</th>
                        <th class="contributorCell">Contributor  &nbsp; &nbsp; <i class="icon-edit initPropagation"></i><div style="display:none;"><ul class="propagateDown"></ul><button class="propagateButton btn btn-primary">Propagate</button></div> </th>
                        <th class ="attributionsCell">Attributions &nbsp; &nbsp; <i class="icon-edit initPropagation"></i><div style="display:none;"><ul class="propagateDown"></ul><button class="propagateButton btn btn-primary">Propagate</button></div> </th>
                        <th>Refrences</th>
                        <th class="licenseCell">License &nbsp; &nbsp; <i class="icon-edit initPropagation"></i><div style="display:none;"><ul class="propagateDown"></ul><button class="propagateButton btn btn-primary">Propagate</button></div> </th>
                        <th class="audienceCell">Audience &nbsp; &nbsp; <i class="icon-edit initPropagation"></i><div style="display:none;"><ul class="propagateDown"></ul><button class="propagateButton btn btn-primary">Propagate</button></div></th>

                    </table>


                    %{-- <button id="tagHeadersButton">Mark this Header</button> --}%
                    <button class="btn btn-primary" id="downloadModifiedSpecies" style="margin-top: 8px">Download</button>
                </div>
                
                <div class="section" style="margin-top: 0px; margin-bottom: 40px;clear:both;">

                    <a id="speciesUploadCancel" href="${uGroup.createLink(controller:'species', action:'list')}" class="btn"
                        style="float: right; margin-right: 5px;"> Cancel </a>

                    <a id="uploadSpecies"
                        class="btn btn-primary" style="float: right; margin-right: 5px;">
                        Upload Species</a>
                    <div id="speciesLoader" style="display:none; float: right; margin-right: 5px;"><img class="uploadingSpecies" src="../images/rotate.gif"></div>
                    <span class="policy-text"> By submitting this form for
                        uploading species data you agree to our <a href="/terms">Terms
                            and Conditions</a> on the use of our site </span>
                </div>
                </div>
                <div id="xlsxFileUrl" style="display:none;">
                    <input type="text" value="">
                </div>

                <div id="isSimpleSheet" style="display:none;">
                    <input type="text" value="">
                </div>
                <div id="headerMetadata" style="display:none;">
                    <input type="text" value="">
                </div>
                <div id="columnOrder" style="display:none;">
                    <input type="text" value="">
                </div>

                <form id="downloadSpeciesFile" action="${uGroup.createLink(action:'downloadSpeciesFile', controller:'UFile', 'userGroup':userGroupInstance)}" method="post" style="visibility:hidden;">
                    <input type="text" name="downloadFile" value="" style="visibility:hidden;">
                    Download the uploaded sheet here <input class="btn btn-primary" type="submit" value="Download">
                </form>
                
                <form id="downloadErrorFile" action="${uGroup.createLink(action:'downloadSpeciesFile', controller:'UFile', 'userGroup':userGroupInstance)}" method="post" style="visibility:hidden;">
                    <input type="text" name="downloadFile" value="" style="visibility:hidden;">
                    Download the error file here <input class="btn btn-primary" type="submit" value="Download">
                </form>
                <span id="filterLinkSpan" style="display:none;">You may view your contribution <a href="" id="filterLink"> here </a>.</span>
                <div id="uploadConsole">

                </div>
                 

        </body>
        <r:script>
        $(document).ready(function() {
        /*
            var contributor_autofillUsersComp = $("#userAndEmailList_${contributor_autofillUsersId}").autofillUsers({
            usersUrl : '${createLink(controller:'SUser', action: 'terms')}'
            });
        
 	    $("#uploadSpecies").click(function(){
                //$('#contributorIds').val(contributor_autofillUsersComp[0].getEmailAndIdsList().join(","));
                $('#uploadSpeciesForm').submit();
            });
        */            
       });
        </r:script>
    </html>
