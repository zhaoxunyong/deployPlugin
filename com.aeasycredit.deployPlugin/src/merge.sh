#!/bin/sh
echo "Please exec from Git Bash"
branchFromVersion=$1
branchToVersion=$2
isPushBranch=$3

cd #{project}

currentbranchVersion=$(git branch|grep "^\*"|sed "s;^\*\s*;;")

if [[ "$branchFromVersion" == "" || "$branchToVersion" == "" ]]; then
  # echo "branchFromVersion and branchToVersion must be not empty!"
  echo "Usage: $0 branchFromVersion branchToVersion isPushBranch"
  echo "Example: $0 1.0.0.release master true"
  exit -1
fi

if [[ "$isPushBranch" == "" ]]; then
  isPushBranch="true"
fi

echo "currentbranchVersion: ${currentbranchVersion}"
echo "branchFromVersion: ${branchFromVersion}"
echo "branchToVersion: ${branchToVersion}"
echo "isPushBranch: ${isPushBranch}"

#Merge
git add .
git commit -m "Mod merge code."
git pull
git checkout ${branchToVersion}
git merge --no-ff ${branchFromVersion} -m "Merge branch '${branchToVersion}' of ${branchFromVersion}"
if [[ "$isPushBranch" == "true" ]]; then
  git push -u origin ${branchToVersion}
fi
git checkout ${currentbranchVersion}

cd $PWD
echo "----------Successfully----------"