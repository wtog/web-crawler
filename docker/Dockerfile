FROM alpine:3.8

MAINTAINER wangtong
USER root

RUN echo http://mirrors.ustc.edu.cn/alpine/v3.8/main > /etc/apk/repositories && \
    echo http://mirrors.ustc.edu.cn/alpine/v3.8/community >> /etc/apk/repositories && \
    echo @edge http://mirrors.ustc.edu.cn/alpine/edge/community >> /etc/apk/repositories && \
    echo @edge http://mirrors.ustc.edu.cn/alpine/edge/main >> /etc/apk/repositories 

RUN apk update && apk upgrade && \
    apk add --no-cache \
    bash \
    alsa-lib \
    at-spi2-atk \
    atk \
    cairo \
    cups-libs \
    dbus-libs \
    eudev-libs \
    expat \
    flac \
    gdk-pixbuf \
    glib \
    harfbuzz@edge \
    libgcc \
    libjpeg-turbo \
    libpng \
    libwebp \
    libx11 \
    libxcomposite \
    libstdc++@edge \
    libxdamage \
    libxext \
    libxfixes \
    libexif \
    chromium@edge \
    chromium-chromedriver@edge \    
    openjdk8

RUN ln -s /usr/lib/jvm/default-jvm/bin/jstat /usr/local/bin/jstat && \
    ln -s /usr/lib/jvm/default-jvm/bin/jcmd /usr/bin/jcmd && \
    ln -s /usr/lib/jvm/default-jvm/bin/jstack /usr/bin/jstack && \
    rm -rf /openjdk8*

ADD web-crawler-assembly.jar /apps/web-crawler.jar

CMD ["java", "-jar", "/apps/web-crawler.jar"]
