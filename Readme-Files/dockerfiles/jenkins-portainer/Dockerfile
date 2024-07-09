FROM jenkins/jenkins:latest

USER root
RUN apt update && curl -fsSL https://get.docker.com | sh
RUN usermod -aG docker jenkins
RUN apt install -y maven
USER jenkins