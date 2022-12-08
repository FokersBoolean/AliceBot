AliceBot
===========================

###### 环境依赖
* ubuntu 18.04
* jdk8
* go-cqhttp_linux_arm64

###### 部署步骤
1. 安装jdk1.8
     sudo apt-get install openjdk-8-jdk
     
2. 下载运行go-cq
      下载地址：https://docs.go-cqhttp.org/guide/quick_start.html#%E5%9F%BA%E7%A1%80%E6%95%99%E7%A8%8B<br>
      下载到服务器后使用tar -zxvf进行解压<br>
      运行其中的的在解压目录下运行screen -S gocq<br>
      再运行./go-cqhttp，第一次运行会先让你选连接方式，我们选择2：正向Websocket,gocq在解压目录下生成config.yml，这时我们先Ctrl+C<br>
      运行nano config.yml，拉到最下面找到address: 0.0.0.0:8080，把8080替换成9099,然后Ctrl+X,然后按Y，再回车一下保存<br>
      然后再次运行./go-cqhttp，完成登录后按Ctrl+A+D放到后台运行<br>
2. 执行jar包
     先输入screen -S Alice 创建个会话<br>
     然后使用java -jar -Xmx512m --clientBaseConfig.admin=管理员QQ --clientBaseConfig.robot=机器人QQ --clientBaseConfig.wakeUpWord=唤醒词 --clientBaseConfig.standbyWord=待机词 --clientBaseConfig.promptUpWord=提示词 --clientBaseConfig.robotName=机器人名称 --chatGPT.email=gpt邮箱 --chatGPT.password=gpt密码 --chatGPT.sessionToken=token --server.port=8080<br>
     运行后再使用Ctrl+A+D放到后台运行即可

###########V1.0.0 版本内容更新
1. 新功能     增删用户
2. 新功能     基础对接
