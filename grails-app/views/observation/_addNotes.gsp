<%@ page import="species.utils.Utils"%>
<%@page import="java.util.Arrays"%>


<div class="section" style="position: relative; overflow: visible;">
    <h3><g:message code="observation.addnotes.additional.information" /> </h3>
    <div class="span6 block">
        <!--label for="notes"><g:message code="observation.notes.label" default="Notes" /></label-->
        <h5><label><i
                    class="icon-pencil"></i><g:message code="default.notes.label" /> <small><g:message code="observation.notes.message" default="Description" /></small></label><br />
            </h5><div class="section-item" style="margin-right: 10px;">
            <!-- g:textArea name="notes" rows="10" value=""
            class="text ui-corner-all" /-->

            <ckeditor:config var="toolbar_editorToolbar">
            [
            [ 'Bold', 'Italic' ]
            ]
            </ckeditor:config>
            <ckeditor:editor name="notes" height="53px" toolbar="editorToolbar">
            ${observationInstance?.notes}
            </ckeditor:editor>
        </div>
    </div>
    <%
    def obvTags = observationInstance?.tags
    if(params.action == 'save' && params?.tags){
    obvTags = Arrays.asList(params.tags)
    }				
    %>

    <div class="span6 block sidebar-section" style="margin:0px 0px 20px -10px;">
        <h5><label><i
                    class="icon-tags"></i><g:message code="default.tags.label" /> <small><g:message code="observation.tags.message" default="" /></small></label>
        </h5>
        <div class="create_tags section-item" style="clear: both;">
            <ul id="tags" class="obvCreateTags">
                <g:each in="${obvTags}" var="tag">
                <li>${tag}</li>
                </g:each>
            </ul>
        </div>
    </div>



    <sUser:isFBUser>
    <div class="span6 sidebar-section block" style="margin-left:-10px;">
        <div class="create_tags" >
            <label class="checkbox" > <g:checkBox style="margin-left:0px;"
                name="postToFB" />
                <g:message code="default.post.facebook.label" /></label>
        </div>
    </div>
    </sUser:isFBUser>



</div>
