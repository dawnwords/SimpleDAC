Change Log
=================
v 1.0
-----------------
1. JSON解析依赖包由Gson改为FastJson(rocketmq-client依赖已包含)
2. Condition.True类改为常量Condition.True


v 2.0
-----------------
1. DACFactory.createDAC()增加线程同步, 对于同一个BeanClass, 返回同一个DAC实例
2. FileBasedDAC.add(), FileBasedDAC.deleteByCondition(), FileBasedDAC.updateByCondition()加入写锁
3. FileBasedDAC.selectByCondition()加入读锁
4. FileBasedDAC.beginTransaction()加入互斥锁, 由FileBasedDAC.commit()或FileBasedDAC.rollback()释放
5. FileBasedDAC事务处理
    - 事务记录由File-based改为Memory-based, 且线程独立
    - 事务处理过程中的写操作(add,delete,update), 回滚操作(rollback)性能提升:时间复杂度O(1) 
    - 事务处理过程中的读操作(select), 提交操作(commit)性能下降:时间复杂度O(M*N), M为事务累计总量, N为数据总量
