# 批量上传Jar到Maven私服

## 前言：

前段时间因为Nexus服务器的磁盘不足，实在没有办法， 忍痛删除了所有的快照。

于是，埋下了祸根，大量的依赖找不到，本地无法运行。



今天，决心恢复快照！

## 前置条件

本地或者某台服务器拥有完整或者比较全的快照Jar包。

由于我们打包服务器都是一台，所以这个条件是满足的。



## 准备

1. 在Nexus新建一个Repository

![](https://notes.zijiancode.cn/maven/create-local-repositroy.png)

2. 创建完毕，地址复制出来

```
http://ip:port/repository/local/
```

3. 将新建的repository放到maven-public组中

![](https://notes.zijiancode.cn/maven/add-to-group.png)

## 开始

登陆打包服务器，进入到maven 仓库文件夹，在与com同级的目录中编写脚本

```shell
vim batchimports.sh
```

```shell
#!/bin/bash
while getopts ":r:u:p:" opt; do
	case $opt in
		r) REPO_URL="$OPTARG"
		;;
		u) USERNAME="$OPTARG"
		;;
		p) PASSWORD="$OPTARG"
		;;
	esac
done
 
find . -type f -not -path './batchimports\.sh*' -not -path '*/\.*' -not -path '*/\^archetype\-catalog\.xml*' -not -path '*/\^maven\-metadata\-local*\.xml' -not -path '*/\^maven\-metadata\-deployment*\.xml' | sed "s|^\./||" | xargs -I '{}' curl -u "$USERNAME:$PASSWORD" -X PUT -v -T {} ${REPO_URL}/{} ;
```

```shell
chmod +x batchimports.sh
```

执行脚本

```
./batchimports.sh -u username -p password -r http://ip:port/repository/local/
```

> 自行替换账号密码与地址