<g:each in="${featuredNotes}" var="${featuredNotesItem}">
<div class="featured_notes linktext">
    ${featuredNotesItem.notes}
    <small>
        <div class="ellipsis" style="margin:0px;height:${instance.summary()?'20px':'0px'};">${instance.summary()}</div>
        <div class="ellipsis" style="height:20px;">Featured on 
            <b>${featuredNotesItem.createdOn.format('MMMMM dd, yyyy')}</b> 
            <g:if test="${featuredNotesItem.userGroup}">
                in the group 
                <b><a href="${uGroup.createLink(mapping:'userGroup', controller:'userGroup', action:'show', userGroup:featuredNotesItem.userGroup)}">${featuredNotesItem.userGroup.name}</a></b>
            </g:if>
        </div>
    </small>
</div>
</g:each>


