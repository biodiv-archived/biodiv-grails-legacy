<div>
    <span id='loginLink'
            style='position: relative; margin-right: 30px; float: right'>
            <sec:ifLoggedIn>
                    <sUser:renderProfileLink /> (<a id="logout"
                            href="${createLink(controller:'logout')}">Logout</a>)
</sec:ifLoggedIn> <sec:ifNotLoggedIn>
                    <g:link controller='login'>Login</g:link> | <g:link
                            controller='register'>Register</g:link>
            </sec:ifNotLoggedIn> </span>
    <!-- g:render template='/common/ajaxLogin' /-->
    <div id="fb-root"></div>
    <br />
</div>

