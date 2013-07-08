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
alias ra='cd ~/git/biodiv; grails run-app ~/git/biodiv &> ~/git/biodiv/logs/app.log &'
_run-script () {  
    cd ~/git/biodiv; grails run-script ~/git/biodiv/userscripts/$1.groovy &> ~/git/biodiv/logs/script.log &
}
alias rs=_run-script
_dropdb () {  
    dropdb -Upostgres biodiv;
}
alias dropdb=_dropdb
_createdb () {  
    createdb -Upostgres biodiv;
    psql -Upostgres -d biodiv -f /usr/share/postgresql/9.1/contrib/postgis-1.5/postgis.sql;
    psql -Upostgres -d biodiv -f /usr/share/postgresql/9.1/contrib/postgis-1.5/spatial_ref_sys.sql;
}
alias createdb=_createdb
export GRAILS_OPTS="-XX:MaxPermSize=256m -Xmx1024M"
