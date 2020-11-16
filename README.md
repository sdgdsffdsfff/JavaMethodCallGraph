# JavaMethodCallGraph

## 程序的入口类在process包里面  
## FileProcess是提取方法调用信息到MySQL数据库  
## FilterMethodInvocation是过滤数据库中已经提取的方法调用信息，同时进行匹配，便于进行可视化。

## StructDetect类实现违反mvc设计架构的检测
### 检测模式：serviceImpl调controller、controller调dao、dao调service、dao调controller
### 运行说明：
#### 1.在com/se/config/DataConfig.java中修改配置
    projectName     项目名
    projectPath     项目路径
    layer_dao       dao层包名，eg：dao       
    layer_serviceImpl   service的实现类包名  eg：impl
    layer_controller    controller包名      eg：controller
    layer_service       service包名
#### 注意：包名要与检测的项目包名一致
#### 2.进入StructDetect类中运行主方法
#### 3.结果将在com/se/config/DataConfig.java中的structDetectResult对应的路径（需要的话可修改），目前配置为src/main/resources/DetectResult/structureDetectResult.txt
####  注意：每次运行时都会清理上一次写入的结果

## SimilarityEntityDetect类实现实体类的重复检测
### 检测过程：
#### 1.某一实体包下的所有类两两进行比对
#### 2.先检测相同类型的字段，设置一个阈值，然后计算对比的两个类中相同类型的字段个数与各自类字段的总个数相比，如果两个类中的比值都大于设置的阈值，则初步判定两个类可能重复，然后用相同的方法判断类型、参数相等的方法，再与阈值比较
### 运行说明：目前阈值设为0.6（如需修改，请修改下面列出的threshold对应的值）
#### 1.在com/se/config/DataConfig.java中修改配置
    projectName     项目名
    projectPath     项目路径
    layer_dao       dao层包名，eg：dao       
    layer_vo        vo包名，eg：pojo、vo等
    threshold       比对的阈值
####  注意：目前只设置了一次检测两个包，layer_dao和layer_vo包
#### 2.进入SimilarityEntityDetectResult类中运行主方法
#### 3.结果将在com/se/config/DataConfig.java中的SimilarityEntityDetectResult对应的路径（需要的话可修改），目前配置为src/main/resources/DetectResult/similarityEntityDetectResult.txt
####  注意：每次运行时都会清理上一次写入的结果