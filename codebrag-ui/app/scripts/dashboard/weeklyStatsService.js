angular.module('codebrag.dashboard')

    .service('weeklyStatsService', function($rootScope, $http, $modal, $q) {

        var weeklyStatsApiUrl = 'rest/weeklyStats';

        this.initialize = function() {
            //$rootScope.$on('openUserMgmtPopup', openPopup);
        };

        this.loadStats = function() {
            return $http.get(weeklyStatsApiUrl).then(function(response) {                 
                return response.data;
            });
        };

    });
