 <%@page import="species.utils.Utils"%>
<%@page import="java.text.SimpleDateFormat" %>
<%@page import="species.License.LicenseType"%>

   
        <div class="control-group">
            <label class="control-label" for="license">${g.message(code:"default.licenses.label")}</label> 
            <div class="controls">
                <select name="aq.license" multiple="multiple" class="multiselect licenseFilter input-block-level">
                    <g:each in="${LicenseType.toList()}" var="license">
                    <option value="${license.value()}"> ${g.message(error:license)} </option>
                    </g:each>
                </select>

            </div>
        </div>

        <div class="control-group">
            <label class="control-label" for="members">${g.message(code:'default.members.label')}</label> 
            <div class="controls">
                <input
                data-provide="typeahead" type="text" class="input-block-level"
                name="aq.members" value="${queryParams?queryParams['aq.members']?.encodeAsHTML():''}"
                placeholder="${g.message(code:'placeholder.search.members')}" /> 
            </div>
        </div>


        <div class="control-group">
            <label class="control-label" for="text">${g.message(code:'default.tags.label')}</label> 
            <div class="controls">
                <input
                data-provide="typeahead" type="text" class="input-block-level"
                name="aq.tag" value="${queryParams?queryParams['aq.tag']?.encodeAsHTML():''}"
                placeholder="${g.message(code:'placeholder.search.all.tags')}" /> 
            </div>
        </div>



        <div class="control-group">
            <label
                class="control-label" for="observedOn">${g.message(code:'label.createdon')}</label>

            <div class="controls">
                <div id="uploadedOnDatePicker" style="position: relative;overflow:visible">
                    <div id="uploadedOn" class="btn pull-left" style="text-align:left;padding:5px;" >
                        <i class="icon-calendar icon-large"></i> <span class="date"></span>
                    </div>
                </div>
            </div>
        </div>

        <div class="control-group">
            <div style="${params.webaddress?:'display:none;'}">
                <label class="radio inline"> 
                    <input type="radio" id="uGroup_ALL" name="uGroup" 
                    value="ALL"> ${g.message(code:'default.search.in.all.groups')} </label> <label
                    class="radio inline"> <input type="radio" id="uGroup_THIS_GROUP" name="uGroup" 
                    value="THIS_GROUP"> ${g.message(code:'default.search.within.this.group')} </label>
            </div>
        </div>


