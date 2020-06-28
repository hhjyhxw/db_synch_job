使用方式，使用ant打包方式运行build.xml，生成recommend-monitor.jar文件到target目录下。
1、将 recommend-monitor.jar 复制到 运行目录下
2、将lib文件夹也复制到运行目录下
3、在运行目录下创建config文件夹，将src/main/resources/jdbc.properties文件复制到config文件夹下
4、修改config下的jdbc.properties文件为运行环境下的真实参数
5、修改t_cfg_dbdata_info, t_cfg_dbdata_sync表参数，详见init.sql表结构说明
6、使用jdk1.7，运行方式为 java -jar recommend-monitor.jar
