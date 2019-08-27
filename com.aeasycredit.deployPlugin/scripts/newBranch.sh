#!/bin/bash
export PATH="/usr/local/bin:/usr/bin:$JAVA_HOME/bin:$MVN_HOME/bin:$PATH"

sedi() {
  case $(uname) in
    Darwin*) sedi=('-i' '') ;;
    *) sedi='-i' ;;
  esac

  sed "${sedi[@]}" "$@"
}

# script replace, don't delete.
#cd #{project}

#检查是否已经保存过git的账户与密码
git ls-remote > /dev/null
if [[ $? != 0 ]]; then
	echo "=================Authentication error================="
	echo "Authentication error. Please execute the following command through git bash, and enter the account and password:"
	echo "git config --global credential.helper store"
	echo "git ls-remote"
	echo "=================Authentication error================="
	exit -1
fi

git status|grep "git add" &> /dev/null
if [[ $? == 0 ]]; then
  echo "Your local repo seems changed, but not commit yet, please stage or stash changes first!"
  exit -1
fi

currentBranchVersion=`git branch|grep "*"|sed 's;^* ;;'`
git remote show origin|grep $currentBranchVersion | egrep "本地已过时|local out of date" &> /dev/null
if [[ $? == 0 ]]; then
  echo "Your local repo seems out of date, please \"git pull\" first!"
  exit -1
fi

#git remote show origin|grep $currentBranchVersion | egrep "可快进|up to date" &> /dev/null
#if [[ $? == 0 ]]; then
#  echo "Your local repo seems to be up to date, please git push first!"
#  exit -1
#fi

NEW_BRANCH=$1
desc=$2
desc=${desc//\"/}

if [[ "$NEW_BRANCH" == "" || ($NEW_BRANCH != *.x) ]]; then
  # echo "branchVersion must be not empty!"
  echo "Usage: $0 branch"
  echo "$0 1.0.x"
  exit -1
fi

function SwitchBranch() {
    branchVersions=$1
    #git add .
    #git commit -m "Commit by new branch:${NEW_BRANCH}."
    git checkout -b ${branchVersions} > /dev/null
    if [[ $? != 0 ]]; then
        git checkout ${branchVersions} > /dev/null
        if [[ $? != 0 ]]; then
            echo "Switch branch to ${branchVersions} error."
            exit -1
        fi
    fi
    echo "Switch branch to ${branchVersions} successfully."
    # git branch
}

function Push() {
    branchVersions=$1
    git add .
    if [[ "$desc" == "" ]]; then
      desc="Mod New branch version to ${branchVersions}"
    fi
    git commit -m "${desc}"
    git push origin ${branchVersions}
    if [[ $? != 0 ]]; then
        echo "Push ${branchVersions} error."
        exit -1
    fi
    echo "Push ${branchVersions} successfully."
}

function changeNextVersion() {
  #change version
  nextVersion=$1
  mvn versions:set -DnewVersion=${nextVersion}
  mvn versions:commit
}

SwitchBranch $NEW_BRANCH

#change version
arr=(${NEW_BRANCH//./ })
mvnVersion=${arr[0]}.${arr[1]}.0-SNAPSHOT
changeNextVersion $mvnVersion &> /dev/null
if [[ -f "deploy.sh" ]]; then
  bash deploy.sh changeVersion $mvnVersion
fi
Push $NEW_BRANCH