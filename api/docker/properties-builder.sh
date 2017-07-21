#!/bin/bash

# if we are linked, use that info
if [ "$MONGODB_PORT" != "" ]; then
  # Sample: MONGO_PORT=tcp://172.17.0.20:27017
  export SPRING_DATA_MONGODB_HOST=`echo $MONGODB_HOST|sed 's;.*://\([^:]*\):\(.*\);\1;'`
  export SPRING_DATA_MONGODB_PORT=`echo $MONGODB_PORT|sed 's;.*://\([^:]*\):\(.*\);\2;'`
fi

echo "SPRING_DATA_MONGODB_HOST: $SPRING_DATA_MONGODB_HOST"
echo "SPRING_DATA_MONGODB_PORT: $SPRING_DATA_MONGODB_PORT"


cat > dashboard.properties <<EOF
#Database Name - default is test
dbname=${SPRING_DATA_MONGODB_DATABASE:-dashboarddb}

#Database HostName - default is localhost
dbhost=${SPRING_DATA_MONGODB_HOST:-11.16.42.153}

#Database Port - default is 27017
dbport=${SPRING_DATA_MONGODB_PORT:-27017}

#Database Username - default is blank
dbusername=${SPRING_DATA_MONGODB_USERNAME:-dashboarduser}

#Database Password - default is blank
dbpassword=${SPRING_DATA_MONGODB_PASSWORD:-dbpassword}
server.contextPath=/api
feature.dynamicPipeline=${FEATURE_DYNAMIC_PIPELINE:-disabled}
systemConfig.multipleDeploymentServers=${CONFIG_GLOBAL_MULTIPLE_DEPLOYMENT_SERVERS:-false}
EOF
