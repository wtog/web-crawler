#!/bin/bash

set -e

function deploy_git() {
    git add .
    params=$@
    if [ "X$params" = "X" ]; then
        msg=$(git log --pretty=oneline --abbrev-commit | awk '{if(NR<2) for(i=2;i<=NF;i++) printf $i" " }')
    else
        msg=$params
    fi
    echo $msg
    git commit -m "$msg"
    git push
}

deploy_git $@
