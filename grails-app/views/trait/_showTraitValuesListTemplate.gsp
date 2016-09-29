<div class="groupsWithSharingNotAllowed btn-group userGroups" style="white-space:inherit;">
        <g:each in="${traitValues}" var="traitValue" status="i">
                <%
                boolean checked = false;//traitValue.value;
                %>  
                    <button type="button" data-tvid='${traitValue.id}' data-tid='${traitValue.trait.id}'
                        class="btn input-prepend single-post"
                        value="${traitValue.id}"
                        style="padding: 0px; height: 42px; border-radius: 6px; margin:5px;">

                        <g:render template="/trait/showTraitValueSignatureTemplate" model="['traitValue':traitValue]"/>
                    </button> 
        </g:each>
</div>


