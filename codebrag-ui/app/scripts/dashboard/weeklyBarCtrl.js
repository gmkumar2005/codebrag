angular.module('codebrag.dashboard')

    .controller('WeeklyBarCtrl', function ($scope, $http, weeklyStatsService,events) {

        $scope.$on(events.allfollowupsTabOpened, initCtrl);
        function initCtrl() {
            weeklyStatsService.loadStats().then(function(weeklyStats) {
            $scope.series = weeklyStats.series;            
            $scope.labels = weeklyStats.labels;
            $scope.weeklystatsdata = weeklyStats.data;     
            });          
        }

        initCtrl();

    });