FROM postgres:latest

RUN apt-get update && apt-get install -y locales \
    && echo "tr_TR.UTF-8 UTF-8" > /etc/locale.gen \
    && locale-gen tr_TR.UTF-8

ENV LANG tr_TR.UTF-8
ENV LANGUAGE tr_TR:tr
ENV LC_ALL tr_TR.UTF-8
