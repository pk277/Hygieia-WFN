(function () {
    'use strict';

    angular
        .module(HygieiaConfig.module)
        .controller('monitorViewController', monitorViewController)
        .controller('monitorStatusController', monitorStatusController);

    monitorViewController.$inject = ['$scope', 'monitorData', 'DashStatus', '$modal', '$q'];
    function monitorViewController($scope, monitorData, DashStatus, $modal, $q) {
        /*jshint validthis:true */
        var ctrl = this;
       
        // public variables
		 ctrl.lineOptions = {
            plugins: [
                Chartist.plugins.gridBoundaries(),
                Chartist.plugins.lineAboveArea(),
                Chartist.plugins.tooltip(),
                Chartist.plugins.pointHalo()
            ],
			
            
          donutWidth: 70,
          donutSolid: true,
          startAngle: 270,
           showLabel: true,
		   height:200,
		   width:300
        };
        ctrl.statuses = DashStatus;
        ctrl.services = [];
        ctrl.dependencies = [];
		var errorCountArray=[];
		var label=[];
        ctrl.lineData= {
		labels:label,
        series: errorCountArray
		};
        // public methods
        ctrl.openStatusWindow = openStatusWindow;
        ctrl.hasMessage = hasMessage;

        ctrl.load = function() {
            // grab data from the api
            var deferred = $q.defer();
            monitorData.details($scope.dashboard.id).then(function(data) {
                processResponse(data.result);
                deferred.resolve(data.lastUpdated);
            });
            return deferred.promise;
        };


        // method implementations
        function hasMessage(service) {
            return service.message && service.message.length;
        }

        function openStatusWindow(service) {
            // open up a new modal window for the user to set the status
            $modal.open({
                templateUrl: 'monitorStatus.html',
                controller: 'monitorStatusController',
                controllerAs: 'ctrl',
                scope: $scope,
                size: 'lg',
                resolve: {
                    // make sure modal has access to the status and selected
                    statuses: function () {
                        return DashStatus;
                    },
                    service: function () {
                        return {
                            id: service.id,
                           pod:service.pod,
  						  errorCount:service.errorCount,
						  ucids:service.ucidCount,
						  percentage:service.percentage,
						  sysCodes:service.syscodes,
						  app:service.app,
						  custids:service.custids,
						  errorMessages:service.errorMessages
                        };
                    }
                }
            }).result
                .then(function (updatedService) {
                    // if the window is closed without saving updatedService will be null
                    if(!updatedService) {
                        return;
                    }

                    // update locally
                    _(ctrl.services).forEach(function(service, idx) {
                        if(service.id == updatedService.id) {
                            ctrl.services[idx] = angular.extend(service, updatedService);
                        }
                    });

                    // update the api
                    monitorData.updateService($scope.dashboard.id, updatedService);
                });
        }

        function processResponse(response) {
            var worker = {
                    doWork: workerDoWork
                };

            worker.doWork(response, DashStatus, workerCallback);
        }

        function workerDoWork(data, statuses, cb) {
            cb({
                services: get(data.services, false),
                dependencies: get(data.dependencies, true)
            });

            function get(services, dependency) {
                return _.map(services, function (item) {
                    var name = item.name;
                    errorCountArray.push(item.errorCount);
					label.push(item.pod);
                    if (dependency && item.applicationName) {
                        name = item.applicationName + ': ' + name;
                    }

                    if(item.status && (typeof item.status == 'string' || item.status instanceof String)) {
                        item.status = item.status.toLowerCase();
                    }

                    switch (item.status) {
                        case 'ok':
                            item.status = statuses.PASS;
                            break;
                        case 'warning':
                            item.status = statuses.WARN;
                            break;
                        case 'alert':
                            item.status = statuses.FAIL;
                            break;
                    }

                    return {
                        id: item.id,
                        name: name,
                        status: item.status,
                        app: item.app,
						pod:item.pod,
						errorCount:item.errorCount,
						ucids:item.custIDs.length,
						custids:item.custIDs,
						syscodes:item.syscodes,
						percentage:item.percentage,
						errorMessages:item.errorMessages
                    };
                });
            }
        }

        function workerCallback(data) {
            //$scope.$apply(function () {
                ctrl.services = data.services;
                ctrl.dependencies = data.dependencies;
            //});
        }
    }

    monitorStatusController.$inject = ['service', 'statuses', '$modalInstance'];
    function monitorStatusController(service, statuses, $modalInstance) {
        /*jshint validthis:true */
        var ctrl = this;

        // public variables
        ctrl.service = service;
        ctrl.statuses = statuses;
        ctrl.setStatus = setStatus;
        
        // public methods
        ctrl.submit = submit;

        function setStatus(status) {
            ctrl.service.status = status;
			
        }

        function submit() {
            // pass the service back so the widget can update
            $modalInstance.close(ctrl.service);
        }
    }
})();