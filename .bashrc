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
alias ra='cd ~/git/biodiv; grails run-app ~/git/biodiv &> ~/Desktop/op.txt &'
_run-script () {  
    cd ~/git/biodiv; grails run-script ~/git/biodiv/userscripts/$1.groovy &> ~/Desktop/op1.txt &
}
alias rs=_run-script

export GRAILS_OPTS="-XX:MaxPermSize=256m -Xmx1024M"
