#git aliases
alias ga='git add'
alias gp='git push'
alias gl='git log'
alias gs='git status'
alias gd='git diff'
alias gdc='git diff --cached'
alias glc='git diff --name-only --diff-filter=U'
alias gm='git commit -m'
alias gma='git commit -am'
alias gb='git branch'
alias gc='git checkout'
alias gra='git remote add'
alias grr='git remote rm'
alias gpu='git pull'
alias gcl='git clone'

ra() {
    echo "$@"
    cd ~/git/$1 
    grails run-app ~/git/$1 &> ~/git/$1/logs/$1.log &
    tail -f ~/git/$1/logs/$1.log
}
rs () {  
    cd ~/git/$1 
    grails run-script ~/git/$1/userscripts/$2.groovy &> ~/git/$1/logs/$1script.log &
    tail -f ~/git/$1/logs/$1script.log
}
dadb () {  
    dropdb -Upostgres $1;
}
cadb () {  
    createdb -Upostgres $1;
    createlang -Upostgres plpgsql $1;
    psql -Upostgres -d $1 -f /usr/share/postgresql/9.1/contrib/postgis-1.5/postgis.sql;
    psql -Upostgres -d $1 -f /usr/share/postgresql/9.1/contrib/postgis-1.5/spatial_ref_sys.sql;
}


export BIODIV_CONFIG_LOCATION=~/.grails/additional-config.groovy
export GRAILS_OPTS="-Xms1024M -Xmx2G -Dlog4jdbc.spylogdelegator.name=net.sf.log4jdbc.log.slf4j.Slf4jSpyLogDelegator"
export JAVA_OPTS="$JAVA_OPTS -Dlog4jdbc.spylogdelegator.name=net.sf.log4jdbc.log.slf4j.Slf4jSpyLogDelegator "


alias cdgb='cd /usr/local/src/biodiv'
alias tstart='/opt/tomcat/biodiv/bin/catalina.sh start'
alias tstop='/opt/tomcat/biodiv/bin/catalina.sh stop'
alias tlog='tail -f /var/log/tomcat/biodiv/catalina.out'
alias nstart='service nginx restart'
alias nstop='service nginx stop'
alias nedit='vim /etc/nginx/sites-enabled/biodiv-main.conf'
alias nlog='tail -f /var/log/nginx/access.log /var/log/nginx/error.log'

