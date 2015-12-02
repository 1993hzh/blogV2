#Blog

##Set up env
config the vars in your heroku console.
###db.conf
(current support postgres)
###oauth.conf
(current support sina)
###fileSystem.conf
(current support qiniu)
###and then
```sh
insert your admin account with sql
```
##add your own language support
config application.conf with `play.i18n.langs`, add messages.[yourLang] in `conf` dir

##TODO(will try to deploy the blog in daoCloud)
