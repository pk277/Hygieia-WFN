#!/bin/bash

# mongo container provides the HOST/PORT
# api container provided DB Name, ID & PWD

if [ "$TEST_SCRIPT" != "" ]
then
        #for testing locally
        PROP_FILE=application.properties
else 
	PROP_FILE=hygieia-udeploy-deployment-collector.properties
fi
  
if [ "$MONGODB_PORT" != "" ]; then
	# Sample: MONGODB_PORT=tcp://172.17.0.20:27017
	MONGODB_HOST=`echo $MONGODB_HOST|sed 's;.*://\([^:]*\):\(.*\);\1;'`
	MONGODB_PORT=`echo $MONGODB_PORT|sed 's;.*://\([^:]*\):\(.*\);\2;'`
else
	env
	echo "ERROR: MONGO_PORT not defined"
	exit 1
fi

echo "MONGODB_HOST: $MONGODB_HOST"
echo "MONGODB_PORT: $MONGODB_PORT"


cat > $PROP_FILE <<EOF
#Database Name
dbname=${HYGIEIA_API_ENV_SPRING_DATA_MONGODB_DATABASE:-dashboard}

#Database HostName - default is localhost
dbhost=${MONGODB_HOST:-10.0.1.1}

#Database Port - default is 27017
dbport=${MONGODB_PORT:-27017}

#Database Username - default is blank
dbusername=${HYGIEIA_API_ENV_SPRING_DATA_MONGODB_USERNAME:-}

#Database Password - default is blank
dbpassword=${HYGIEIA_API_ENV_SPRING_DATA_MONGODB_PASSWORD:-}

#Collector schedule (required)
udeploy.cron=${UDEPLOY_CRON:-0 0/1 * * * *}

#UDeploy server (required) - Can provide multiple
udeploy.servers[0]=${UDEPLOY_URL:-http://cdlorchestration.nj.adp.com/oo/}
udeploy.niceNames[0]=${UDEPLOY_NAME:-UDeploy}

#UDeploy user name (required)
udeploy.username=${UDEPLOY_USERNAME:-kasibhov}

#UDeploy password (required)
udeploy.password=${UDEPLOY_PASSWORD:-Krishna@10}

EOF

echo "

===========================================
Properties file created `date`:  $PROP_FILE
Note: passwords hidden
===========================================
`cat $PROP_FILE |egrep -vi password`
 "

exit 0
