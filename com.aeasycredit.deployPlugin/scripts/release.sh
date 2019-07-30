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
	echo "Authentication error. Please execute the following command through git bash, and enter the account and password:"
	echo "1. git config --global credential.helper store"
	echo "2. git ls-remote"
	exit -1
fi

branchVersion=$1
newDate=$2
#Whether or not to tag this branch version: "false": don't tag "yes": tag
needTag=$3

if [[ "$branchVersion" == "" || "$newDate" == "" ]]; then
  # echo "branchVersion must be not empty!"
  echo "Usage: $0 BranchVersion newTagDate"
  echo "$0 1.0.0.release 201802271230"
  echo "$0 1.0.0.htofix 201802271230"
  exit -1
fi

newTag=${branchVersion}-${newDate}

function SwitchBranch() {
    branchVersions=$1
    # git add .
    # git commit -m "Commit by new branch:${NEW_BRANCH}."
    git checkout -b ${branchVersions} > /dev/null
    if [[ $? != 0 ]]; then
        git checkout ${branchVersions} > /dev/null
        if [[ $? != 0 ]]; then
            echo "Switching branch to ${branchVersions} error."
            exit -1
        fi
    fi
    echo "Switching branch to ${branchVersions} successful."
    # git branch
}

function Push() {
    branchVersions=$1
    git add .
    git commit -m "Add New branch version to ${branchVersions}"
    git push origin ${branchVersions}
    if [[ $? != 0 ]]; then
        echo "Pushing ${branchVersions} error."
        exit -1
    fi
    echo "Pushing ${branchVersions} successful."
}

function Tag() {
    newTag=$1
    git tag -a $newTag -m "For prod version ${newTag}"
    if [[ $? != 0 ]]; then
      echo "Tagging error!"
      exit -1
    else
      echo "Tagging to ${newTag} successful!"
      git push origin ${newTag}
    fi
}

function deleteUnusedReleaseBranch() {
    type=$1
    reserveVersionNumber=$2
    if [[ "${type}" == "" ]]; then
        type="release"
    fi
    if [[ "${reserveVersionNumber}" == "" ]]; then
        reserveVersionNumber=20
    fi
    deleteBranchs=`git branch -a --sort=-committerdate|grep ${type}|grep remotes|sed 's;remotes/origin/;;'|sort -t '.' -r -k 2 -V|sed "1,${reserveVersionNumber}d"`
    for deleteBranch in $deleteBranchs   
    do
        # Keep only the last releases
        git branch -d $deleteBranch &> /dev/null
        git push origin --delete $deleteBranch &> /dev/null
    done
    echo "Keep only the last ${reserveVersionNumber} ${type} versions!"
}

function changeReleaseVersion() {
  #change version
  mvnVersion=$1
  mvn versions:set -DnewVersion=${mvnVersion}
  mvn versions:commit
}

function changeNextVersion() {
  #change version
  nextVersion=$1
  mvn versions:set -DnewVersion=${nextVersion}
  mvn versions:commit
}

function updateVersionRecord() {
  version=$1
  verFile=.version
  if [[ ! -f "$verFile" ]]; then
    touch "$verFile"
  fi
  echo "version=$version" > $verFile
}

#Get next develop version
releaseVersion=$(echo $branchVersion|sed 's;\.test;;'|sed 's;\.release;;'|sed 's;\.hotfix;;')
arr=(${releaseVersion//./ })
nextDevelopVersion=${arr[0]}.${arr[1]}.$((${arr[2]}+1))-SNAPSHOT


currentBranchVersion=`git branch|grep "*"|sed 's;^* ;;'`
echo "branchVersion--------${branchVersion}"
echo "newTag--------${newTag}"
echo "currentBranchVersion--------${currentBranchVersion}"
SwitchBranch $branchVersion


changeReleaseVersion $releaseVersion &> /dev/null
updateVersionRecord $releaseVersion

# deploy
cat pom.xml 2>/dev/null | grep "<skip_maven_deploy>false</skip_maven_deploy>" &> /dev/null
if [[ $? == 0 ]]; then
mvn clean deploy > /dev/null
fi
Push $branchVersion
if [[ "$needTag" == "true" ]]; then
  Tag $newTag
fi
git checkout $currentBranchVersion
changeNextVersion $nextDevelopVersion &> /dev/null
updateVersionRecord $nextDevelopVersion
Push $currentBranchVersion


# Keep only the last releases version
echo "Deleting unused release or hotfix branches..."
deleteUnusedReleaseBranch release
deleteUnusedReleaseBranch hotfix
echo "Release or hotfix branches have been deleted..."