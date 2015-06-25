Change Log
=================
v 1.0
-----------------
1. JSON解析依赖包由Gson改为FastJson(rocketmq-client依赖已包含)
2. Condition.True类改为常量Condition.True


v 2.0
-----------------
1. DACFactory.createDAC()增加线程同步
2. DACFactory.createDAC()对于同一个BeanClass，返回同一个DAC实例
