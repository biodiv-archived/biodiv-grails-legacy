<script>
<sec:ifLoggedIn>
$(function(){
$('.login-box').mouseover(function(){
    $('.login-box-options').show();      
    });
    
$('.login-box').mouseout(function(){
    $('.login-box-options').hide();      
});
});
</sec:ifLoggedIn>
</script>
<style>
.login-box {
font-weight: normal;
height: 100px;
margin: 0;
position: absolute;
right: 0;
top: 0;
width: 180px;
}
<sec:ifLoggedIn>
.login-box:hover {
background-color: #ffffff;    
border-left: 1px solid #e5e5e5;
box-shadow: 0 6px 8px -6px #5e5e5e;
border-radius:0 0 5px 5px;        
}
</sec:ifLoggedIn>
.login-box-options {
float:right;
right: 0;
padding:10px;       
}

.login-box img {
width: 32px;
height: 32px;       
}

.loginLink {
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
.prop {
    margin-bottom: 5px;
    margin-top: 5px;
}
</style>
<div class="login-box">
<div class="register">
<sec:ifNotLoggedIn>
            <g:link controller='login'>Login</g:link> | <g:link
                controller='register'>Register</g:link>
</sec:ifNotLoggedIn>
</div>

    <span class='loginLink'>
            <sec:ifLoggedIn>
                    <sUser:renderProfileLink />
            </sec:ifLoggedIn> 
    </span>
    <div class="login-box-options" style="display:none;">
        <sec:ifLoggedIn>
            <a id="logout" href="${createLink(controller:'logout')}">Logout</a>
        </sec:ifLoggedIn> 
    </div>
</div>
<div id="fb-root"></div>
