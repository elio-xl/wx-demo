pipeline {
    agent any

    tools {
        jdk 'jdk8'
        maven 'apache-maven-3.6.2'
    }

    stages {
        stage('Pull code') {
            steps {
                echo '拉取代码'
                checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'github-account-passwd', url: 'https://github.com/xiaolei582/wx-demo.git']]])
                echo '拉取代码完成'
            }
        }
        stage('Build') {
            steps {
                echo 'mvn 打包开始'
                sh 'cd wx-demo'
                sh 'mvn clean install -DskipTests -Prelease'
                echo 'mvn 打包完成'
            }
        }
        stage('Archive') {
            steps {
                archiveArtifacts artifacts: '**/target/*.jar', followSymlinks: false, onlyIfSuccessful: true
            }
        }
        stage('Deploy') {
            steps {
                sh 'tar -zcvf  /data/software/wx-demo-$(date "+%Y%m%d%H%M%S").tar /data/software/wx-demo'
                sh 'cd /data/software/wx-demo'
                sh 'unalias cp'
                sh 'cp -rf  /data/source/wx-demo/Dockerfile /data/software/wx-demo/'
                sh 'cp -rf  /data/source/wx-demo/entrypoint.sh /data/software/wx-demo/'
                sh 'cp -rf  /data/source/wx-demo/docker-compose.yml /data/software/wx-demo/'
                sh 'cp -rf  /data/source/wx-demo/target/wx-demo.jar /data/software/wx-demo/'
                sh 'alias cp="cp -i"'
                sh 'cd /data/software/wx-demo'
                sh 'chmod +x entrypoint.sh'
                echo '部署开始'
                sh 'docker-compose up -d'
                echo '部署完成'
            }
        }
    }
}
