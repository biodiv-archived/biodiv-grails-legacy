execute pathogen#infect()

syntax on

set sta
set sw=4

set foldmethod=marker
set hlsearch
set showmatch

" use smart c-like indentation.
set cindent
" dont insert tabs on indentation. use spaces instead.
set expandtab

set autoindent
syntax on
filetype plugin indent on
set wildignore+=*.class,.git,.hg,.svn,target/**
set runtimepath^=~/.vim/bundle/ctrlp.vim

" keys for easy saving and restoring vim session
map <F2> :mksession! ~/vim_session <cr> " Quick write session with F2
map <F3> :source ~/vim_session <cr>     " And load session with F3

"source ~/.vim/bundle/matchit/plugin/matchit.vim 
"the cursor will briefly jump to the matching brace when you insert one.
set showmatch
set matchtime=3
"The 'showmatch' option will not scroll the screen. To scroll the screen you
"can use a mapping like:
inoremap } }<Left><c-o>%<c-o>:sleep 500m<CR><c-o>%<c-o>a
inoremap ] ]<Left><c-o>%<c-o>:sleep 500m<CR><c-o>%<c-o>a
inoremap ) )<Left><c-o>%<c-o>:sleep 500m<CR><c-o>%<c-o>a

"Bash doesn’t load your .bashrc unless it’s interactive. Use
"to make Vim’s :! shell behave like your command prompt
set shellcmdflag=-ic

"""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""
" => Files, backups and undo
" """""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""
" Turn backup off, since most stuff is in SVN, git et.c anyway...
set nobackup
set nowb
set noswapfile

" Smart way to move between windows
map <C-j> <C-W>j
map <C-k> <C-W>k
map <C-h> <C-W>h
map <C-l> <C-W>l

" Useful mappings for managing tabs
map <leader>tn :tabnew<cr>
map <leader>to :tabonly<cr>
map <leader>tc :tabclose<cr>
map <leader>tm :tabmove

" Return to last edit position when opening files (You want this!)
autocmd BufReadPost *
            \ if line("'\"") > 0 && line("'\"") <= line("$") |
            \   exe "normal! g`\"" |
            \ endif
" Remember info about open buffers on close
set viminfo^=%

"File Browser
"autocmd vimenter * NERDTree
map <C-n> :NERDTreeToggle<CR>

"Fuzzy Search
map <C-p> :CtrlP<CR>
map <C-m> :CtrlPMRU<CR>
map <C-a> :CtrlPMixed<CR>

" markdown helpers
nnoremap <leader>1 yypVr=
nnoremap <leader>2 yypVr-
nnoremap <leader>3 I# <Esc>A #<Esc>
nnoremap <leader>4 I## <Esc>A ##<Esc>
nnoremap <leader>5 I### <Esc>A ###<Esc>
nnoremap <leader>6 I#### <Esc>A ####<Esc>

" working with split windows
nnoremap <C-h> <C-w>h
nnoremap <C-j> <C-w>j
nnoremap <C-k> <C-w>k
nnoremap <C-l> <C-w>l

autocmd bufenter * if (winnr("$") == 1 && exists("b:NERDTreeType") && b:NERDTreeType == "primary") | q | endif

" EasyGrep
let g:EasyGrepMode=2
let g:EasyGrepCommand=0
let g:EasyGrepRecursive=1
let g:EasyGrepIgnoreCase=1

"Open file under cursor
"map <C-i> :call OpenVariableUnderCursor(expand("<cword>"))<CR>
"map <Leader>h :call FindSubClasses(expand("<cword>"))<CR>

"function! OpenVariableUnderCursor(varName)
"    let filename = substitute(a:varName,'(<w+>)', 'u1', 'g')
"    :call OpenFileUnderCursor(filename)
"endfunction
"
"function! OpenFileUnderCursor(filename)
"    let ext = fnamemodify(expand("%:p"), ":t:e")
"    execute ":find " . a:filename . "." . ext
"endfunction
"
"function! FindSubClasses(filename)
"    execute ":Grep \(implements\|extends\) " . a:filename
"endfunction


"call ExtractSnips("/home/sravanthi/.vim/bundle/snipmate.vim/snippets/groovyVim/html", "gsp")
"call ExtractSnips("/home/sravanthi/.vim/bundle/snipmate.vim/snippets/groovyVim/grails", "groovy")
"

"On Mac, ‘D’ is the command key in .vimrc. As you can probably guess, Cmd-1
"will copy local changes to the merged file, Cmd-2 will copy from the remote
"file, and Cmd-3 will copy from the base.
map <D-1> :diffget LOCAL<CR>
map <D-2> :diffget REMOTE<CR>
map <D-3> :diffget BASE<CR>


"Grails testing
map <S-F9> <Esc>:w<CR>:call RunSingleGrailsTest()<CR>
map <F9> <Esc>:w<CR>:call RunGrailsTestFile()<CR>
map <D-F9> :call RunLastCommandInTerminal()<CR>
command! TestResults :call TestResults()

function! RunSingleGrailsTest()
    let testName = expand("%:t:r.") . "." . expand("<cword>")
    :call RunGrailsTest(testName)
endfunction

function! RunGrailsTestFile()
    let testName = expand("%:t:r")
    :call RunGrailsTest(testName)
endfunction

function! RunGrailsTest(testName)
    let path = expand("%:r")
    if path =~ "integration"
        let flag = "--integration"
    else
        let flag = "--unit"
    endif
    execute ":!grails test-app " . flag . " " . a:testName
endfunction

function! TestResults()
    silent execute ":!open target/test-reports/html/index.html"
endfunction

" set these up as environment variables on your system, or override
" " per session by using ':let g:grails_user = foo'
let g:grails_user = $DEFAULT_GRAILS_USER
let g:grails_password = $DEFAULT_GRAILS_PASSWORD
let g:grails_base_url = $DEFAULT_GRAILS_BASE_URL

function! Grails_eval_vsplit() range
    let temp_source = s:copy_groovy_buffer_to_temp(a:firstline, a:lastline)
    let temp_file = s:select_new_temp_buffer()

    if strlen(g:grails_user) > 0
        " replace current buffer with grails' output
        silent execute ":%! postCode.groovy -u " . g:grails_user . " -p " . g:grails_password . " -b " . g:grails_base_url . " " . temp_source . " 2>&1 "
    else 
        silent execute ":%! postCode.groovy -b " . g:grails_base_url . " " . temp_source . " 2>&1 "
    endif

    wincmd p " change back to the source buffer
endfunction

au BufNewFile,BufRead *.groovy vmap <silent> <F8> :call Grails_eval_vsplit()<CR>
au BufNewFile,BufRead *.groovy nmap <silent> <F8> mzggVG<F8>`z
au BufNewFile,BufRead *.groovy imap <silent> <F8> <Esc><F8>a
au BufNewFile,BufRead *.groovy map <silent> <S-F8> :wincmd P<CR>:q<CR>
au BufNewFile,BufRead *.groovy imap <silent> <S-F8> <Esc><S-F8>a

"to kill running grails application and start app again
nmap <F5> :wa<CR> :silent !pkill -f grails<CR> :call system("screen -X stuff 'ra\n'")<CR>

inoremap <F6> <C-O>za
nnoremap <F6> za
onoremap <F6> <C-C>za
vnoremap <F6> zf
nnoremap <silent> <Space> @=(foldlevel('.')?'za':"\<Space>")<CR>
vnoremap <Space> zf

set smartindent
set tabstop=4
set shiftwidth=4
set expandtab
