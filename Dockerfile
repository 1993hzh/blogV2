FROM accident/blog:ubuntu
# here you can pull both ubuntu(default) or centos(accident/blog:centos)
MAINTAINER      Leo Hu <mail@huzhonghua.cn>

# here package the project
RUN sbt dist
RUN unzip /blog/target/universal/blog-2.0.zip && chmod 755 /blog/blog-2.0/bin/blog

CMD ["/blog/blog-2.0/bin/blog"]
