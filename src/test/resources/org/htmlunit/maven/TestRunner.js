TestRunner = {
  run: function () {
    var callbacks = Lib.getCallbacks();

    callbacks.forEach(function (callback) {
      callback();
    });
    document.getElementById("result").innerHTML = "OK";
  }
};

if (window.addEventListener) {
  addEventListener('DOMContentLoaded', function () {
    TestRunner.run();
  }, false);
} else {
  attachEvent('onload', function () {
    TestRunner.run();
  });
}
