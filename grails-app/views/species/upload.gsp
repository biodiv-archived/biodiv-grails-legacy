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
        <uploader:head />
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
                                <label class="control-label span2" for="speciesFile"><g:message code="upload.species.file" />  <span
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
                                <label class="control-label span2" for="imagesDir"><g:message code="upload.images.url" /> </label>

                                <div class="controls span9">
                                    <input id="imagesDir" type="text" class="input-block-level" name="imagesDir"
                                    placeholder="${g.message(code:'placeholder.species.enter.url')}"
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
                    %{--<span id="addNewColumn" class="btn-link"><g:message code="checklist.create.add.new.column" /></span>--}%
                    <!--span class="help-inline"> (Mark scientific and common name column using <img src='${createLinkTo(file:"dropdown_active.gif", base:grailsApplication.config.speciesPortal.resources.serverURL)}'/>)</span-->

                    <div id="myGrid" class="" style="width:100%;height:350px;clear:both;"></div>
                    <div id="nameSuggestions" style="display: block;"></div>
                    <div id="legend" class="hide">
                        <span class="incorrectName badge"><g:message code="error.incorrect.names" /> </span>
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
                        style="float: right; margin: 5px;display:none;"><g:message code="button.validate.names" /> </a>

                </div>
                
                </table>            
                <div id="tagHeaders" class="section checklist-slickgrid" style="clear:both;display:none;">
                    <table id="tableHeader" border="1">
                        <th class="columnName"><g:message code="upload.column.name" /></th>
                        <th><g:message code="upload.data.columns" /></th>
                        <th><g:message code="upload.header" /></th>
                        <th><g:message code="upload.append" /></th>
                        <!--th>Group</th-->
                        <th><g:message code="upload.delimiter" /></th>
                        <th><g:message code="button.images" /></th>
                        <th class="contributorCell"><g:message code="default.contributors.label" />  &nbsp; &nbsp; <i class="icon-edit initPropagation"></i><div style="display:none;"><ul class="propagateDown"></ul><button class="propagateButton btn btn-primary"><g:message code="button.propogate" /></button></div> </th>
                        <th class ="attributionsCell"><g:message code="default.attributions.label" /> &nbsp; &nbsp; <i class="icon-edit initPropagation"></i><div style="display:none;"><ul class="propagateDown"></ul><button class="propagateButton btn btn-primary"><g:message code="button.propogate" /></button></div> </th>
                        <th><g:message code="default.references.label" /></th>
                        <th class="licenseCell"><g:message code="default.licenses.label" /> &nbsp; &nbsp; <i class="icon-edit initPropagation"></i><div style="display:none;"><ul class="propagateDown"></ul><button class="propagateButton btn btn-primary"><g:message code="button.propogate" /></button></div> </th>
                        <th class="audienceCell"><g:message code="default.audiences.label" /> &nbsp; &nbsp; <i class="icon-edit initPropagation"></i><div style="display:none;"><ul class="propagateDown"></ul><button class="propagateButton btn btn-primary"><g:message code="button.propogate" /></button></div></th>

                    </table>


                    %{-- <button id="tagHeadersButton"><g:message code="button.mark.header" /></button> --}%
                    <button class="btn btn-primary" id="downloadModifiedSpecies" style="margin-top: 8px"><g:message code="button.download" /></button>
                </div>
                
                <div class="section" style="margin-top: 0px; margin-bottom: 40px;clear:both;">

                    <a id="speciesUploadCancel" href="${uGroup.createLink(controller:'species', action:'list')}" class="btn"
                        style="float: right; margin-right: 5px;"> <g:message code="button.cancel" /> </a>

                    <a id="uploadSpecies"
                        class="btn btn-primary" style="float: right; margin-right: 5px;">
                        <g:message code="button.upload.species" /></a>
                    <div id="speciesLoader" style="display:none; float: right; margin-right: 5px;"><img class="uploadingSpecies" src="../images/rotate.gif"></div>
                    <span class="policy-text"> <g:message code="upload.submit.upload" /> <a href="/terms"><g:message code="link.terms.conditions" />          </a> <g:message code="button.propogate" /><g:message code="register.index.use.of.site" /></span>
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
                    <g:message code="upload.download.upload" /> <input class="btn btn-primary" type="submit" value="Download">
                </form>
                
                <form id="downloadErrorFile" action="${uGroup.createLink(action:'downloadSpeciesFile', controller:'UFile', 'userGroup':userGroupInstance)}" method="post" style="visibility:hidden;">
                    <input type="text" name="downloadFile" value="" style="visibility:hidden;">
                   <g:message code="upload.download.error.file" />  <input class="btn btn-primary" type="submit" value="Download">
                </form>
                <span id="filterLinkSpan" style="display:none;"><g:message code="text.view.contribution" />  <a href="" id="filterLink"> <g:message code="msg.here" /> </a>.</span>
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
