<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.ImageUtils"%>
<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>
<%@ page import="species.Species"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@page import="species.utils.Utils"%>
<html>
    <head>
        <g:set var="title" value="Species"/>
        <g:render template="/common/titleTemplate" model="['title':title]"/>
        <r:require modules="species_upload" />
        <uploader:head />
    </head>
    <body>
        <div>
            <s:showSubmenuTemplate model="['entityName':'Species Upload']" />
                <uGroup:rightSidebar/>
                <form id="uploadSpeciesForm" action="${uGroup.createLink(controller:'species', action:'upload')}" 
                    title="Upload spreadsheet" 
                    method="post">
                    <div class="super-section span12" style="margin-left: 0px;">
                        <div class="section">

                            <div
                                class="control-group">
                                <label class="control-label span2" for="speciesFile"> Species File <span
                                        class="req">*</span></label>
                                <div class="controls span9" style="">
                                    <% def allowedExtensions = '[ "xls", "xlsx", "csv"]'  %>
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
                                    <input type="text" class="input-block-level" name="imagesDir"
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
                    <div class="section" style="margin-top: 20px; margin-bottom: 40px;clear:both;">

                        <a href="${uGroup.createLink(controller:'species', action:'list')}" class="btn"
                            style="float: right; margin-right: 5px;"> Cancel </a>

                        <a id="uploadSpecies"
                            class="btn btn-primary" style="float: right; margin-right: 5px;">
                            Upload Species</a>
                        <span class="policy-text"> By submitting this form for
                            uploading species data you agree to our <a href="/terms">Terms
                                and Conditions</a> on the use of our site </span>
                    </div>


                </form>			
                    

                <div id="speciesGridSection" class="section checklist-slickgrid ${params.action=='upload'?'hide':''}">
                    <span id="addNewColumn" class="btn-link">+ Add New Column</span>
                    <span class="help-inline"> (Mark scientific and common name column using <img src='${createLinkTo(file:"dropdown_active.gif", base:grailsApplication.config.speciesPortal.resources.serverURL)}'/>)</span>

                    <div id="myGrid" class="" style="width:100%;height:350px;"></div>
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
                %{--
                <table id="tableHeader" border="1">
                    <tr>
                        <td class="columnName">S_No</td>
                        <td class="dataColCell" ><input class="dataColumns"></td>
                        <td class="mergeFlagCell"><input type="checkbox" class="mergeFlag" name = "merge" value = "mergeFlag"></td>
                        <td class="headerFlagCell"><input type="checkbox" class="headeFlag" name = "header" value = "headerFlag"></td>
                        <td><input type="radio" class="groupRadio" name="group" value="G1">G1<input type="radio" class="groupRadio" name="group" value="G2">G2<input type="radio" name="group" value="G3">G3</td>
                        <td><input type="text" class="delimiter"></td>
                    </tr>
                
                    <tr>
                        <td>Scien_Name</td>
                        <td><input class="tagsInput"></td>
                        <td> <input type="checkbox" name = "merge" value = "mergeFlag"></td>
                    </tr>
                    --}%

                </table>            
                <div id="tagHeaders" style="display:none;">
                    <table id="tableHeader" border="1">
                        <th>Column_Name</th>
                        <th>Data_Columns</th>
                        <th>Header</th>
                        <th>Merge</th>
                        <th>Group</th>
                        <th>Delimiter</th>
                    </table>


                    %{-- <button id="tagHeadersButton">Mark this Header</button> --}%
                    <button id="downloadModifiedSpecies">Download</button>
                </div>

                <div id="xlsxFileUrl" style="display:none;">
                    <input type="text" value="">
                </div>

                <div id="headerMetadata" style="display:none;">
                    <input type="text" value="">
                </div>

                <div id="saveModifiedUrl" style="display:none;">
                    <input type="text" value="">
                </div> 
                <form id="downloadSpeciesFile" action="${uGroup.createLink(action:'downloadSpeciesFile', controller:'UFile', 'userGroup':userGroupInstance)}" method="post" style="visibility:hidden;">
                    <input type="text" name="downloadFile" value="">
                    <input type="submit" value="Submit">
                </form>


                <div id="uploadConsole">

                </div>
            </div>
        </body>
        <r:script>
        $(document).ready(function() {
        /*
            var contributor_autofillUsersComp = $("#userAndEmailList_${contributor_autofillUsersId}").autofillUsers({
            usersUrl : '${createLink(controller:'SUser', action: 'terms')}'
            });
        */
 	    $("#uploadSpecies").click(function(){
                //$('#contributorIds').val(contributor_autofillUsersComp[0].getEmailAndIdsList().join(","));
                $('#uploadSpeciesForm').submit();
            });
                    
       });
        </r:script>
    </html>
