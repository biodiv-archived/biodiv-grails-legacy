<div class="request">
    <a id="requestPermission" class="btn btn-primary ${hide?'hide':''}" href="#requestPermissionDialog" role="button" data-toggle="modal" data-invitetype='requestPermission'><i
            class="icon-envelope"></i> <g:message code="userGroup.members.label"
        default="${g.message(code:'link.request')}" /> </a>

    <div class="modal hide fade" id="requestPermissionDialog" tabindex='-1'
        role="dialog" aria-labelledby="requestPermissionModalLabel"
        aria-hidden="true">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3 id="requestPermissionModalLabel"><g:message code="link.request" /></h3>
        </div>
        <div class="modal-body">
            <p><g:message code="inviteforcontribut.send.request" /></p>
            <div>
                <div class="inviteMsg_status"></div>
                <form method="post"
                    style="background-color: #F2F2F2;">
                    <label class="radio">
                        <input type="radio" name="invitetype" value="contributor" checked>
                        Species page Contributor
                    </label>
                    <label class="radio">
                        <input type="radio" name="invitetype" value="taxon_curator">
                        Taxon Curator
                    </label>
                    <label class="radio">
                        <input type="radio" name="invitetype" value="taxon_editor">
                        Taxon Editor
                    </label>
                    <textarea class="inviteMsg comment-textbox" placeholder="${g.message(code:'placeholder.species.about.you')}"></textarea>
                </form>
            </div>
        </div>
        <div class="modal-footer">
            <a href="#" class="btn" data-dismiss="modal" aria-hidden="true"><g:message code="button.close" /></a>
            <a href="#" class="requestButton btn btn-primary"><g:message code="inviteforcontribut.req" /></a>
        </div>
    </div>
</div>

<sec:ifAnyGranted roles='ROLE_SPECIES_ADMIN,ROLE_ADMIN'>
<div class="invite">
        <a id="inviteCurators" class="btn btn-primary ${hide?'hide':''}" href="#inviteCuratorsDialog" role="button" data-toggle="modal" data-invitetype='curator'><i
            class="icon-envelope"></i> <g:message code="inviteforcontribut.invite.curators" /> </a>
        <a id="inviteContributors" class="btn btn-primary  ${hide?'hide':''}" href="#inviteContributorsDialog" role="button" data-toggle="modal" data-invitetype='contributor'><i
                class="icon-envelope"></i> <g:message code="inviteforcontribut.invite.contributors"
            /> </a>

        <a id="inviteTaxonCurators" class="btn btn-primary ${hide?'hide':''}" href="#inviteTaxonCuratorsDialog" role="button" data-toggle="modal" data-invitetype='taxon_curator'><i
            class="icon-envelope"></i> <g:message code="inviteforcontribut.invite.taxon_curators" /> </a>
        <a id="inviteTaxonEditors" class="btn btn-primary ${hide?'hide':''}" href="#inviteTaxonEditorsDialog" role="button" data-toggle="modal" data-invitetype='taxon_editor'><i
            class="icon-envelope"></i> <g:message code="inviteforcontribut.invite.taxon_editors" /> </a>


        <div class="modal hide fade" id="inviteCuratorsDialog" tabindex='-1'
            role="dialog" aria-labelledby="inviteCuratorsModalLabel"
            aria-hidden="true">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h3 id="inviteCuratorsModalLabel"><g:message code="inviteforcontribut.invite.curators" /></h3>
            </div>
            <div class="modal-body">
                <p><g:message code="inviteforcontribut.send.invitation.curator" /></p>
                <div>
                    <div class="inviteMsg_status"></div>
                    <form method="post"
                        style="background-color: #F2F2F2;">
                        <sUser:selectUsers model="['id':'curator']" />
                        <input type="hidden" name="userIds" />
                        <input type="hidden" name="invitetype" value="curator" />
                        <textarea class="inviteMsg comment-textbox" placeholder="${g.message(code:'placeholder.invite.curator')}"></textarea>
                    </form>
                </div>
            </div>
            <div class="modal-footer">
                <a href="#" class="btn" data-dismiss="modal" aria-hidden="true"><g:message code="button.close" /></a>
                <a href="#" class="inviteButton btn btn-primary"><g:message code="button.invite" /></a>
            </div>
        </div>


        <div class="modal hide fade" id="inviteContributorsDialog" tabindex='-1'
            role="dialog" aria-labelledby="inviteContributorsModalLabel"
            aria-hidden="true">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h3 id="inviteContributorsModalLabel"><g:message code="inviteforcontribut.invite.contributors" /></h3>
            </div>
            <div class="modal-body">
                <p><g:message code="inviteforcontribut.send.invitation.contributors" /></p>
                <div>
                    <div class="inviteMsg_status"></div>
                    <form method="post"
                        style="background-color: #F2F2F2;">
                        <sUser:selectUsers model="['id':'contributor']" />
                        <input type="hidden" name="userIds" />
                        <input type="hidden" name="invitetype" value="contributor" />
                        <textarea class="inviteMsg comment-textbox" placeholder="${g.message(code:'placeholder.invite.contributor')}"></textarea>
                    </form>
                </div>
            </div>
            <div class="modal-footer">
                <a href="#" class="btn" data-dismiss="modal" aria-hidden="true"><g:message code="button.close" /></a>
                <a href="#" class="inviteButton btn btn-primary"><g:message code="button.invite" /></a>
            </div>
        </div>

        <div class="modal hide fade" id="inviteTaxonCuratorsDialog" tabindex='-1'
            role="dialog" aria-labelledby="inviteTaxonCuratorsModalLabel"
            aria-hidden="true">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h3 id="inviteTaxonCuratorsModalLabel"><g:message code="inviteforcontribut.invite.taxon_curators" /></h3>
            </div>
            <div class="modal-body">
                <p><g:message code="inviteforcontribut.send.invitation.taxon_curators" /></p>
                <div>
                    <div class="inviteMsg_status"></div>
                    <form method="post"
                        style="background-color: #F2F2F2;">
                        <sUser:selectUsers model="['id':'taxon_curator']" />
                        <input type="hidden" name="userIds" />
                        <input type="hidden" name="invitetype" value="taxon_curator" />
                        <textarea class="inviteMsg comment-textbox" placeholder="${g.message(code:'placeholder.invite.taxon_curator')}"></textarea>
                    </form>
                </div>
            </div>
            <div class="modal-footer">
                <a href="#" class="btn" data-dismiss="modal" aria-hidden="true"><g:message code="button.close" /></a>
                <a href="#" class="inviteButton btn btn-primary"><g:message code="button.invite" /></a>
            </div>
        </div>


        <div class="modal hide fade" id="inviteTaxonEditorsDialog" tabindex='-1'
            role="dialog" aria-labelledby="inviteTaxonEditorsModalLabel"
            aria-hidden="true">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h3 id="inviteTaxonEditorsModalLabel"><g:message code="inviteforcontribut.invite.taxon_editors" /></h3>
            </div>
            <div class="modal-body">
                <p><g:message code="inviteforcontribut.send.invitation.taxon_editors" /></p>
                <div>
                    <div class="inviteMsg_status"></div>
                    <form method="post"
                        style="background-color: #F2F2F2;">
                        <sUser:selectUsers model="['id':'taxon_editor']" />
                        <input type="hidden" name="userIds" />
                        <input type="hidden" name="invitetype" value="taxon_editor" />
                        <textarea class="inviteMsg comment-textbox" placeholder="${g.message(code:'placeholder.invite.taxon_editor')}"></textarea>
                    </form>
                </div>
            </div>
            <div class="modal-footer">
                <a href="#" class="btn" data-dismiss="modal" aria-hidden="true"><g:message code="button.close" /></a>
                <a href="#" class="inviteButton btn btn-primary"><g:message code="button.invite" /></a>
            </div>
        </div>


    </div>
</sec:ifAnyGranted>
