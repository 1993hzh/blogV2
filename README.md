# Blog

## Deploy in Heroku
config the vars in your heroku console.
```sh
FS_ACCESS_KEY={?your_file_system_key}
FS_SECRET_KEY={?your_file_system_secret}
FS_BUCKET_NAME={?your_file_system_bucket}
FS_IMAGE_DOMAIN={?your_file_system_domain}
JDBC_DATABASE_USERNAME={?your_db_name}
JDBC_DATABASE_PASSWORD={?your_db_password}
JDBC_DATABASE_URL={?your_db_url}
SINA_APP_ID={?your_sina_app_id}
SINA_APP_SECRET={?your_sina_app_secrect}
SINA_REDIRECT_URL={?your_sina_app_redirect_url}
RESUME_URL={?your_resume_redirect_url}
RESUME_PASSWORD={?your_resume_password}
```

## Deploy in other CaaS using docker
Dockerfile has been written in this project
### First, you should have a db
I deployed my postgres in www.tenxcloud.com, it's easy, but remember to set the service as stateful to persist your data.
### Second, create the docker image with the Dockerfile in some Caas
Thanks for www.alauda.cn. 
The base image of the blog has been uploaded to docker hub, you can view its src in my another repo: blog-image.
### At last, Config vars in ENV
```sh
FS_ACCESS_KEY={?your_file_system_key}
FS_SECRET_KEY={?your_file_system_secret}
FS_BUCKET_NAME={?your_file_system_bucket}
FS_IMAGE_DOMAIN={?your_file_system_domain}
JDBC_DATABASE_USERNAME={?your_db_name}
JDBC_DATABASE_PASSWORD={?your_db_password}
JDBC_DATABASE_URL={?your_db_url}
SINA_APP_ID={?your_sina_app_id}
SINA_APP_SECRET={?your_sina_app_secrect}
SINA_REDIRECT_URL={?your_sina_app_redirect_url}
APPLICATION_HOME={?application_home_for_logs}
APPLICATION_SECRET={?your_app_secret}
RESUME_URL={?your_resume_redirect_url}
RESUME_PASSWORD={?your_resume_password}
```
Notice, there is an issue for play-slick 1.1.0, here is the question raised by me: https://groups.google.com/forum/#!topic/play-framework/fjRwu2rymVI
You need to set the JDBC_DATABASE_URL to something like 'jdbc:postgresql'.

## PS
1. DB: currently support postgres, you can change the driver class in both db.conf && build.sbt, although this is not tested
2. OAuth: currently support sina
3. File system: currently support qiniu
4. Add your own language support: config application.conf with `play.i18n.langs`, add messages.[yourLang] in `conf` dir
5. Remember
```sh
insert your admin account with sql
```
