# ActivityNet_Downloader

An Activity-Net Downloader,written in Java.

一个Activity-Net_v1.3的下载器,基于官方发布的JSON文件.

支持由Taxonomy建立完整类别文件夹目录,可调用OpenCV进行视频分割并且归类.

## 帮助信息
```
java -jar .\downloader.jar -h
usage: java downloader [options]
 -c,--enable_video_cut          开启视频剪切(使用OpenCV,剪切后文件会大很多)
 -h,--help                      帮助信息
 -r,--root <Path>               设置下载根目录,无须以/或\结尾.默认目录:
                                D:\Downloads\activity_net.v1-3
 -t,--test                      只下载少量视频,验证程序可行性
 -tn,--thread_number <number>   设置下载线程数,默认: 3
```

## 现有问题
- 使用OpenCV剪切视频后,视频文件会变得很大,至少在3.6倍以上.
- ```src/util/CutVideo.java``` 中注释掉的两个函数是利用命令行启动ffmepg进行视频分割,因为精度原因没用上.
- 不启用视频剪切则会将文件按照第一个segment中的lable分类,可能会有潜在的分类信息缺失或错误.
- 不支持下载失败的视频自动重下,但是会保留失败视频和成功视频的ID,重新启动程序可以跳过下载成功的视频.

    