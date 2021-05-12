#!/usr/bin/env bash

rootDic="/data/www"
runEnv="test"


# 安装composer 并 初始化env

projectes=$(ls $rootDic)

for i in $projectes ; do
    if [ "$i" == 'storage' ]; then
        continue
    fi
    cd "$rootDic/$i/src" || echo "$rootDic/$i/src is not found" cd "$rootDic" continue ;
    cp ".env.$runEnv" .env
    if [ ! -d  "storage/$runEnv-logs" ]; then
        mkdir -p "storage/$runEnv-logs"
    fi
    ln -s "/data/www/storage/$runEnv/job-logs/" "storage/$runEnv-logs/"
    composer install
done
