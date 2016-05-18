<div class="download-box"  style="z-index: 10; display:inline-block; margin-left: 5px;${instanceTotal == 0 ? 'display:none;' :'' }">
    <a class="download-action ${(onlyIcon == 'true')?'':'btn'} ${(params.action=='show')?'btn-link':''}" role="button" data-toggle="modal" href="#downloadModal" title="Download"> <i class=" icon-download-alt"></i>
        <g:if test="${onlyIcon == 'false'}">
            <g:message code="button.download" />
        </g:if>
    </a>

    <form class="download-form form-horizontal">
        <div class="downloadModal modal download-options hide fade" role="dialog" aria-hidden="true">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h3 class="downloadModalLabel">Download</h3>
            </div>
            <div class="modal-body">
                <div><div class="alert alert-info"><g:message code="msg.link.available" /></div></div>
                <g:if test="${exportFields}">
                <div>
                    <h4>${g.message(code:'download.export.fields')}</h4>
                    <g:each in="${exportFields}" var="exportField">
                    <span class="checkbox inline no_indent">
                        <input type="checkbox" name="exportFields" value="${exportField.field}" ${exportField.default?'checked="checked"':''} >
                        ${exportField.name}</input>
                    </span>
                    </g:each>
                </div>
                </g:if>
                <div style="clear:both">
                <h4>${g.message(code:'download.export')}</h4>
                <g:each in="${downloadTypes}" var="downloadType" status="i">
                <span class="radio inline">
                    <g:if test="${i > 0}">
                    <input type="radio" style="margin-top: 0px;" name="downloadType" value="${downloadType}" >
                    ${downloadType.value()}</input>
                    </g:if>
                    <g:else>
                    <input type="radio" style="margin-top: 0px;" name="downloadType" value="${downloadType}" CHECKED >
                    ${downloadType.value()}</input>
                    </g:else>
                </span>
                </g:each>
                </div>
                <br />
                <input type="hidden" name="downloadFrom" value="${downloadFrom}"/>
                <input type="hidden" name="source" value="${source}"/>
                <input type="hidden" name="downloadObjectId" value="${downloadObjectId}"/>
                <input id="instanceTotal" type="hidden" name="instanceTotal" value="${instanceTotal}"/>
                <textarea class="comment-textbox noComment" placeholder="${g.message(code:'placeholder.how.intend')}" name="notes"></textarea>
                <!--div class = "download-close popup-form-close" value="${g.message(code:'button.close')}">
                <i class="icon-remove"></i>
                </div-->

                <div class="downloadMessage">
                </div>
            </div>
            <div class="modal-footer">
                <button class="btn" data-dismiss="modal" aria-hidden="true">Cancel</button>
                <input class="btn download-close" type="submit" value="${g.message(code:'button.ok')}" ></input>
            </div>
        </div>
    </form>
</div>
<asset:script>
</asset:script>
