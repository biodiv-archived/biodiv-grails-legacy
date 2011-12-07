<div id='ajaxLogin'>
   <div class='inner'>
   <div class='fheader'>Please Login..</div>
   <form action='${request.contextPath}/j_spring_security_check' method='POST'
       id='ajaxLoginForm' name='ajaxLoginForm' class='cssform'>
      <p>
         <label for='username'>Login ID</label>
         <input type='text' class='text_' name='j_username' id='username' />
      </p>
      <p>
         <label for='password'>Password</label>
         <input type='password' class='text_' name='j_password' id='password' />
      </p>
      <p>
         <label for='remember_me'>Remember me</label>
         <input type='checkbox' class='chk' id='remember_me'
                name='_spring_security_remember_me'/>
      </p>
      <p>
         <a href='javascript:void(0)' onclick='authAjax(); return false;'>Login</a>
         <a href='javascript:void(0)' onclick='cancelLogin(); return false;'>Cancel</a>
      </p>
   </form>
    <div style='display: none; text-align: left;' id='loginMessage'></div>
   </div>
</div>
