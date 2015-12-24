FROM            centos:latest

MAINTAINER      Leo Hu <mail@huzhonghua.cn>

RUN yum update -y

RUN yum install -y wget
RUN yum install -y git

RUN wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u45-b14/jdk-8u45-linux-x64.rpm
RUN rpm -ivh jdk-8*-linux-x64.rpm && rm jdk-8*-linux-x64.rpm
ENV JAVA_HOME /usr/java/latest

RUN yum install -y unzip
RUN curl -O https://downloads.typesafe.com/typesafe-activator/1.3.6/typesafe-activator-1.3.6.zip
RUN unzip typesafe-activator-1.3.6.zip -d / && rm typesafe-activator-1.3.6.zip && chmod a+x /activator-dist-1.3.6/activator
ENV PATH $PATH:/activator-dist-1.3.6

EXPOSE 9000 8888
RUN mkdir /app
WORKDIR /app

RUN git clone https://github.com/1993hzh/blogV2.git /app

CMD ["activator", "run"]

