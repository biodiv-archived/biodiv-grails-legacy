<script>
<sec:ifLoggedIn>
$(function(){
$('#login-box').mouseover(function(){
    $('#login-box-options').show();      
    });
    
$('#login-box').mouseout(function(){
    $('#login-box-options').hide();      
});
});
</sec:ifLoggedIn>
</script>
<style>
#login-box {
float: right;    
width: 180px;
margin: 0;       
height: 100%;      
}
<sec:ifLoggedIn>
#login-box:hover {
background-color: #ffffff;    
border-left: 1px solid #e5e5e5;
}
</sec:ifLoggedIn>
#login-box-options {
float:right;
right: 0;
}
#loginLink {
margin: 0;
position: relative;
float: right;       
}
.register {
float: right;
margin-right: 10px;       
}
.user-icon {
height: 32px;
line-height: 32px;
margin: 0 auto;
text-align: center;
width: 32px;
}
.figure {
font-size: 80%;
font-style: italic;
position: relative !important;
text-align: center;
}
.user_signature {
height: 50px;
padding-left: 5px;
padding-top: 2px;
text-align: left;
width: 175px;
}
.story-footer .footer-item {
float: left;
margin-right: 10px;
}
</style>
<div class="register">
<sec:ifNotLoggedIn>
            <g:link controller='login'>Login</g:link> | <g:link
                controller='register'>Register</g:link>
</sec:ifNotLoggedIn>
</div>
<div id="login-box" class="btn-group">
    <span id='loginLink' data-toggle="dropdown">
            <sec:ifLoggedIn>
                    <sUser:renderProfileLink />
            </sec:ifLoggedIn> 
    </span>
    <div id="fb-root"></div>
    <div id="login-box-options" class="dropdown-menu" style="display:none;">
        <sec:ifLoggedIn>
            <a id="logout" href="${createLink(controller:'logout')}">Logout</a>
        </sec:ifLoggedIn> 
    </div>
</div>
