FROM 127.0.0.1:10000/helena-job-base:1.0.0

ENV PROJECT_ENV test
ENV DB_HOST 127.0.0.1
ENV DB_PORT 3306
ENV DB_NAME demo
ENV DB_USER root
ENV DB_PASS root

RUN mkdir -p /data/www/storage/test/job-logs \
	&& mkdir -p /etc/supervisor

COPY ./ci/job/runtime/pack-dict /data/www
COPY ./ci/job/runtime/supervisor-default.conf /etc/supervisor/conf.d/supervisor-default.conf
COPY ./ci/job/runtime/pack-dict/.keep /data/www/storage/test/job-logs/.keep
COPY ./ci/job/tools/composer.phar /usr/bin/composer
COPY ./ci/job/tools/project-init.sh /opt/project.sh


RUN echo "user=www" >> /etc/supervisor/supervisord.conf \
	&& chmod +x /opt/project.sh \
    && /bin/bash /opt/project.sh \
	&& chmod -R 777 /data/www

CMD ["supervisord", "-n"]
