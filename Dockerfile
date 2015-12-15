FROM            centos:latest

MAINTAINER      Leo Hu <mail@huzhonghua.cn>

RUN yum update -y

RUN yum install -y wget
RUN wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u45-b14/jdk-8u45-linux-x64.rpm
RUN rpm -ivh jdk-8*-linux-x64.rpm && rm jdk-8*-linux-x64.rpm
ENV JAVA_HOME /usr/java/latest

RUN yum install -y unzip
RUN curl -O https://dl.bintray.com/sbt/native-packages/sbt/0.13.8/sbt-0.13.8.zip
RUN unzip sbt-0.13.8.zip -d / && rm sbt-0.13.8.zip && chmod a+x /sbt/bin/sbt
ENV PATH $PATH:/sbt/bin

RUN yum install -y git

EXPOSE 9000 8888
RUN mkdir /app
WORKDIR /app

RUN git clone https://github.com/1993hzh/blogV2.git /app && cd /app && sbt run

CMD ["sbt", "run"]

