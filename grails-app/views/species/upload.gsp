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

        <r:require modules="add_file" />
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
                                    <% def allowedExtensions = '[ "xlx", "xlsx"]'  %>
                                    <g:render template='/UFile/docUpload'
                                    model="['name': 'speciesfile', 'path': speciesFile?.path, 'size':speciesFile?.size,'allowedExtensions':allowedExtensions, 'fileParams':['uploadDir':'species']]" />
                                    <div class="help-inline">
                                    </div>
                                </div>
                            </div>
                            <div
                                class="control-group">
                                <label class="control-label span2" for="mappingFile"> Mapping File</label>
                                <div class="controls span9" style="">
                                    <g:render template='/UFile/docUpload'
                                    model="['name': 'speciesmappingfile', 'path': mappingFile?.path, 'size':mappingFile?.size,'allowedExtensions':allowedExtensions, 'fileParams':['uploadDir':'species']]" />
                                    <div class="help-inline">
                                    </div>
                                </div>
                            </div>

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
