AliceBot
===========================

###### 环境依赖
* ubuntu 18.04
* jdk8
* go-cqhttp_linux_arm64

###### 部署步骤
1. 安装jdk1.8<br>
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

###### 机器人的所有操作指令（以下指令皆需唤醒机器人后再进行发送才能生效）<br>
#private 将机器人设为私有的，需要权限(默认就是私有)<br>
#public 将机器人设为所有人可用<br>
add @某人 添加一个拥有权限的使用者<br>
del @某人 将某人从使用者列表移除<br>
#reset 在多线程模式下清除所有会话，单线程模式下清除会话<br>
#reset me 用于多线程时清除管理员自己的会话<br>
#more 开启多线程模式<br>
#less 关闭多线程模式<br>
#alone 开启一问到底(既无需唤醒机器人)<br>
#add this 将当前的群聊加入权限列表<br>
#del this 将当前的群聊从权限列表中移除<br>

###### V3.0.0 版本内容更新
1. 新功能     多线程<br>
2. 新功能     一问到底<br>
3. 新功能     群聊权限<br>
