#!/bin/bash

export PATH="/usr/local/bin:/usr/bin:$JAVA_HOME/bin:$MVN_HOME/bin:$PATH"
sedi() {
  case $(uname) in
    Darwin*) sedi=('-i' '') ;;
    *) sedi='-i' ;;
  esac
  sed "${sedi[@]}" "$@"
}

type=$1
branchVersion=$2

#Working for deploy: deploying jar to maven before pushing codes to git
if [[ "$type" == "deploy" ]]; then
    echo "Working for deploy: $branchVersion..."
#Working for changeVersion: changing related project's version after changing owned version
elif [[ "$type" == "changeVersion" ]]; then
    echo "Working for changeVersion: $branchVersion..."
fi