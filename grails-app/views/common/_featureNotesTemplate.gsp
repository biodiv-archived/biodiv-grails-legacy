<g:each in="${featuredNotes}" var="${featuredNotesItem}">
<div class="featured_notes linktext">
    ${featuredNotesItem.notes}
    <p style="margin:0px"><small>${instance.summary()}</small> <small>Featured on <b>${featuredNotesItem.createdOn.format('MMMMM dd, yyyy')}</b> <g:if test="${featuredNotesItem.userGroup}">in the group <b><a href="${uGroup.createLink(controller:'userGroup', action:'show', userGroup:featuredNotesItem.userGroup)}">${featuredNotesItem.userGroup.name}</a></b></g:if></small></p>
</div>
</g:each>


