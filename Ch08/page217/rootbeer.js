setTimeout(function() {
    Java.perform(function() {
        const RootBeer = Java.use('com.scottyab.rootbeer.RootBeer');
        RootBeer.isRooted.implementation = function() {
            return false;
        }
    });
});