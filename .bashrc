#git aliases
alias ga='git add'
alias gp='git push'
alias gl='git log'
alias gs='git status'
alias gd='git diff'
alias gdc='git diff --cached'
alias gm='git commit -m'
alias gma='git commit -am'
alias gb='git branch'
alias gc='git checkout'
alias gra='git remote add'
alias grr='git remote rm'
alias gpu='git pull'
alias gcl='git clone'
alias ra='cd ~/git/bhutanbiodiv; grails run-app ~/git/bhutanbiodiv &> ~/git/bhutanbiodiv/logs/app.log &'
_run-script () {  
    cd ~/git/bhutanbiodiv; grails run-script ~/git/bhutanbiodiv/userscripts/$1.groovy &> ~/git/bhutanbiodiv/logs/script.log &
}
alias rs=_run-script
_dropdb () {  
    dropdb -Upostgres bhutanbiodiv;
}
alias dropdb=_dropdb
_createdb () {  
    createdb -Upostgres bhutanbiodiv;
    psql -Upostgres -d bhutanbiodiv -f /usr/share/postgresql/9.1/contrib/postgis-1.5/postgis.sql;
    psql -Upostgres -d bhutanbiodiv -f /usr/share/postgresql/9.1/contrib/postgis-1.5/spatial_ref_sys.sql;
}
alias createdb=_createdb
export GRAILS_OPTS="-XX:MaxPermSize=256m -Xmx1024M"
