<div class="groupsWithSharingNotAllowed btn-group userGroups" style="white-space:inherit;">
            <div data-tvid='any' data-tid='${traitValues[0].trait.id}'
                class="btn any ${queryParams.trait && traitValues && queryParams.trait[traitValues[0].trait.id+'']=='any'?'active btn-success':''}"
                value="any"
                style="padding: 0px; height: 42px; border-radius: 6px; margin:5px;width:${showDetails?'auto':'165px;'} max-width:${showDetails?'auto':'165px;'}; line-height:40px;">
                Any
            </div> 

        <g:each in="${traitValues}" var="traitValue" status="i">
                    <button type="button" data-tvid='${traitValue.id}' data-tid='${traitValue.trait.id}'
                    class="btn input-prepend single-post ${queryParams?.trait?(queryParams.trait[traitValue.trait.id+'']==traitValue.id+''?'active btn-success':''):''}"
                        value="${traitValue.id}"
                        style="padding: 0px; height: 42px; border-radius: 6px; margin:5px;">

                        <g:render template="/trait/showTraitValueSignatureTemplate" model="['traitValue':traitValue]"/>
                    </button> 
        </g:each>

            <div data-tvid='none' data-tid='${traitValues[0].trait.id}'
                class="btn none ${queryParams.trait && traitValues && queryParams.trait[traitValues[0].trait.id+'']=='none'?'active btn-success':''}"
                value="none"
                style="padding: 0px; height: 42px; border-radius: 6px; margin:5px;width:${showDetails?'auto':'165px;'} max-width:${showDetails?'auto':'165px;'}; line-height:40px;">
                None
            </div> 
</div>


