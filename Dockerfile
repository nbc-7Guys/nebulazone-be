FROM docker.elastic.co/elasticsearch/elasticsearch:8.18.1
RUN elasticsearch-plugin install analysis-nori
