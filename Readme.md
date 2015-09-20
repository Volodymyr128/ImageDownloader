To launch an application you need:
 - Have installed mongoDB with default port 27017 && hostname "DESKTOP-C9DUDQV" (sorry about that, have no time). Also you should create "Demo" db
 - If something goes wrong on next run pls try to clear DEMO db (sorry, have no time to make it well)


Solution described:
 - To store images I used file system (each job/webPage mapped to base64(pageUrl) directory).
   Images for each page are stored inside related directory (have no time to develop deeper directory tree)
 - To store info: status, width, height etc I used Mongo DB
 - Each verticle runs on separate Hazelcast instance (means this is clustered solution). You can run multiple
     instanced to overcome single point of failure. Also it allow very easely scale this application
 - Download jobs are parralleled by JobManager - each dowload - separate worker thread which is executed in thread pool

 Even no time to explain my solution :(((

To launch java -jar target/image-downloader-1.0-SNAPSHOT-fat.jar