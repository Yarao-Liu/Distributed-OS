# FastStorage

#### 介绍

FastStorage是一个基于fastDfs和Minio可定制化开发的文件服务器

#### 软件架构

基于springboot2.0版本开发


#### 安装教程

1.  拉取代码
2.  进入resource目录下netive目录
3.  执行npm install
4.  执行npm run dev
5.  application.yml配置数据库
6.  application-redis.yml配置高速缓存

#### 使用说明

1.  启动springboot项目
2.  浏览器访问 http://localhost:8080 体验minio直传
3.  浏览器访问 http://localhost:8838 体验springboot转发minio上传
4.  使用fastDfs作为文件服务器需要修改application.yml