<div class="request">
    <a id="requestPermission" class="btn btn-primary ${hide?'hide':''}" href="#requestPermissionDialog" role="button" data-toggle="modal" data-invitetype='requestPermission'><i
            class="icon-envelope"></i> <g:message code="userGroup.members.label"
        default="Request Permission" /> </a>

    <div class="modal hide fade" id="requestPermissionDialog" tabindex='-1'
        role="dialog" aria-labelledby="requestPermissionModalLabel"
        aria-hidden="true">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3 id="requestPermissionModalLabel">Request Permission</h3>
        </div>
        <div class="modal-body">
            <p>Send a request for permission</p>
            <div>
                <div class="inviteMsg_status"></div>
                <form method="post"
                    style="background-color: #F2F2F2;">
                    <input type="hidden" name="invitetype" value="contributor"></input>
                    <textarea class="requestMsg comment-textbox" placeholder="Please write a note why do you need to have permission for this taxon."></textarea>
                </form>
            </div>
        </div>
        <div class="modal-footer">
            <a href="#" class="btn" data-dismiss="modal" aria-hidden="true">Close</a>
            <a href="#" class="requestButton btn btn-primary">Request</a>
        </div>
    </div>
</div>

<sec:ifAnyGranted roles='ROLE_SPECIES_ADMIN,ROLE_ADMIN'>
<div class="invite">
    <a id="inviteCurators" class="btn btn-primary ${hide?'hide':''}" href="#inviteCuratorsDialog" role="button" data-toggle="modal" data-invitetype='curator'><i
            class="icon-envelope"></i> <g:message code="userGroup.members.label" default="Invite Curators" /> </a>
        <a id="inviteContributors" class="btn btn-primary  ${hide?'hide':''}" href="#inviteContributorsDialog" role="button" data-toggle="modal" data-invitetype='contributor'><i
                class="icon-envelope"></i> <g:message code="userGroup.members.label"
            default="Invite Contributors" /> </a>

        <div class="modal hide fade" id="inviteCuratorsDialog" tabindex='-1'
            role="dialog" aria-labelledby="inviteCuratorsModalLabel"
            aria-hidden="true">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h3 id="inviteCuratorsModalLabel">Invite curators</h3>
            </div>
            <div class="modal-body">
                <p>Send an invitation to add curator</p>
                <div>
                    <div class="inviteMsg_status"></div>
                    <form method="post"
                        style="background-color: #F2F2F2;">
                        <sUser:selectUsers model="['id':'curator']" />
                        <input type="hidden" name="userIds" />
                        <input type="hidden" name="invitetype" value="curator" />
                        <textarea class="inviteMsg comment-textbox" placeholder="Please write a note to invite curator."></textarea>
                    </form>
                </div>
            </div>
            <div class="modal-footer">
                <a href="#" class="btn" data-dismiss="modal" aria-hidden="true">Close</a>
                <a href="#" class="inviteButton btn btn-primary">Invite</a>
            </div>
        </div>


        <div class="modal hide fade" id="inviteContributorsDialog" tabindex='-1'
            role="dialog" aria-labelledby="inviteContributorsModalLabel"
            aria-hidden="true">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h3 id="inviteContributorsModalLabel">Invite contributors</h3>
            </div>
            <div class="modal-body">
                <p>Send an invitation to add contributors</p>
                <div>
                    <div class="inviteMsg_status"></div>
                    <form method="post"
                        style="background-color: #F2F2F2;">
                        <sUser:selectUsers model="['id':'contributor']" />
                        <input type="hidden" name="userIds" />
                        <input type="hidden" name="invitetype" value="contributor" />
                        <textarea class="inviteMsg comment-textbox" placeholder="Please write a note to invite contributor."></textarea>
                    </form>
                </div>
            </div>
            <div class="modal-footer">
                <a href="#" class="btn" data-dismiss="modal" aria-hidden="true">Close</a>
                <a href="#" class="inviteButton btn btn-primary">Invite</a>
            </div>
        </div>

    </div>
</sec:ifAnyGranted>
