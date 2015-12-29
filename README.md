#Blog

##Deploy in Heroku
config the vars in your heroku console.
```sh
ACCESS_KEY={?your_file_system_key}
SECRET_KEY={?your_file_system_secret}
BUCKET_NAME={?your_file_system_bucket}
IMAGE_DOMAIN={?your_file_system_domain}
JDBC_DATABASE_USERNAME={?your_db_name}
JDBC_DATABASE_PASSWORD={?your_db_password}
JDBC_DATABASE_URL={?your_db_url}
SINA_APP_ID={?your_sina_app_id}
SINA_APP_SECRET={?your_sina_app_secrect}
SINA_REDIRECT_URL={?your_sina_app_redirect_url}
```

##Deploy in other CaaS using docker
Dockerfile has been written in this project
###First, you should have a db
I deployed my postgres in www.tenxcloud.com, it's easy
###Second, create the docker image with the Dockerfile in some Caas
I am using www.alauda.cn
###At last, remember to write a shell to start your application
```sh
cd /app
sbt run -server=y \
-DSINA_REDIRECT_URL={?your_sina_app_redirect_url} \
-DSINA_APP_ID={?your_sina_app_id} \
-DSINA_APP_SECRET={?your_sina_app_secrect} \
-DJDBC_DATABASE_USERNAME={?your_db_name} \
-DJDBC_DATABASE_PASSWORD={?your_db_password} \
-DJDBC_DATABASE_URL={?your_db_url} \
-DACCESS_KEY={?your_file_system_key} \
-DSECRET_KEY={?your_file_system_secret} \
-DBUCKET_NAME={?your_file_system_bucket} \
-DIMAGE_DOMAIN={?your_file_system_domain}
```

##DB
(currently support postgres, you can change the driver class in both db.conf && build.sbt, although this is not tested)
##OAuth
(currently support sina)
##FileSystem
(currently support qiniu)
##Add your own language support
config application.conf with `play.i18n.langs`, add messages.[yourLang] in `conf` dir
##Remember
```sh
insert your admin account with sql
```
#Currently not support https, will add this in the future
