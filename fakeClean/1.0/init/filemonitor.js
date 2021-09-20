


function sendData(event, path){
    var packet = {
        'event': event,
        'path': path,
    };
    send("file:::" + JSON.stringify(packet));

}

Java.perform(function(){
    
    Java.use("com.filemonitor.RecursiveFileObserver").getResultToFile.implementation=function(x, y){
        sendData(x, y);
    } 
})

