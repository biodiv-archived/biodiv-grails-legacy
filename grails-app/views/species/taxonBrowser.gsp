<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>
<%@ page import="species.Species"%>
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
<meta name="layout" content="main" />

<g:set var="entityName"
	value="${message(code: 'species.label', default: 'Species')}" />
<title>Taxonomy Browser</title>

<r:require modules="species_show"/>

</head>
<body>
    <div class="container_12 big_wrapper outer_wrapper">
        <s:showSubmenuTemplate model="['entityName':'Taxonomy Browser']"/>

            <div class="grid_12">
                <t:showTaxonBrowser model="['expandAll':false]"/>
            </div>

            <a id="inviteCurators" class="btn btn-primary" href="#inviteCuratorsDialog" role="button" data-toggle="modal"><i
                    class="icon-envelope"></i> <g:message code="userGroup.members.label"
                default="Invite Curators" /> </a>
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
                        <div id="invite_curatorMsg"></div>
                        <form id="inviteCuratorsForm" method="post"
                            style="background-color: #F2F2F2;">
                            <sUser:selectUsers model="['id':3]" />
                            <input type="hidden" name="curatorUserIds" id="curatorUserIds" />
                            <textarea id="inviteCuratorMsg" class="comment-textbox" placeholder="Please write a note to invite curator."></textarea>
                        </form>
                    </div>
                </div>
                <div class="modal-footer">
                    <a href="#" class="btn" data-dismiss="modal" aria-hidden="true">Close</a>
                    <a href="#" id="inviteCuratorButton" class="btn btn-primary">Invite</a>
                </div>
            </div>
        </div>

    </body>
</html>
