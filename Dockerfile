FROM            centos:latest

MAINTAINER      Leo Hu <mail@huzhonghua.cn>

RUN yum update -y

RUN yum install -y wget
RUN yum install -y git

RUN wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u45-b14/jdk-8u45-linux-x64.rpm
RUN rpm -ivh jdk-8*-linux-x64.rpm && rm jdk-8*-linux-x64.rpm
ENV JAVA_HOME /usr/java/latest

RUN wget --no-check-certificate https://dl.bintray.com/sbt/native-packages/sbt/0.13.8/sbt-0.13.8.tgz
RUN tar -zxvf sbt-0.13.8.tgz && rm sbt-0.13.8.tgz && chmod a+x /sbt/bin/sbt
ENV PATH $PATH:/sbt/bin

EXPOSE 9000 8888
RUN mkdir /app
WORKDIR /app

RUN git clone https://github.com/1993hzh/blogV2.git /app

CMD ["sbt", "run"]

