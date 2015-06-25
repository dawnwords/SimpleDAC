package cn.edu.fudan.se.dac;

import com.alibaba.fastjson.JSON;

import java.io.BufferedWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Dawnwords on 2015/5/21.
 */
final class FileBasedDataAccessComponent<Bean> implements DataAccessInterface<Bean> {

    private static final String TEMP_EXT = ".tmp";

    private File dataFile, tempFile;
    private List<Action> transaction;
    private Class<Bean> beanClass;
    private ReentrantReadWriteLock readWriteLock;
    private ReentrantLock transactionLock;
    private boolean committing;
    private Thread transactionThread;

    private abstract class Action {
        private boolean createTemp;

        protected Action(boolean createTemp) {
            this.createTemp = createTemp;
        }

        boolean execute() {
            if (committing) return getResult();

            if (currentThreadTransaction()) {
                transaction.add(this);
                return true;
            }

            readWriteLock.writeLock().lock();
            boolean result = getResult();
            readWriteLock.writeLock().unlock();

            return result;
        }

        private boolean getResult() {
            boolean result = modifyLogic();
            if (createTemp) result = FileUtil.overwrite(tempFile, dataFile) && result;
            return result;
        }


        abstract Bean modifyBean(Bean bean);

        abstract void performTransaction();

        abstract boolean modifyLogic();
    }

    private class AddAction extends Action {
        Bean element;

        public AddAction(Bean element) {
            super(false);
            this.element = element;
        }

        @Override
        Bean modifyBean(Bean bean) {
            return bean;
        }

        @Override
        public void performTransaction() {
            add(element);
        }

        @Override
        boolean modifyLogic() {
            return FileUtil.appendLine(dataFile, JSON.toJSONString(element));
        }

    }

    private class DeleteAction extends Action {
        Condition<Bean> condition;

        public DeleteAction(Condition<Bean> condition) {
            super(true);
            this.condition = condition;
        }

        @Override
        Bean modifyBean(Bean bean) {
            return condition.assertBean(bean) ? null : bean;
        }

        @Override
        public void performTransaction() {
            deleteByCondition(condition);
        }

        @Override
        boolean modifyLogic() {
            return FileUtil.getBufferedWriter(tempFile, new FileUtil.BufferedWriterHandler() {
                @Override
                public boolean handle(BufferedWriter writer) {
                    return FileUtil.eachLine(dataFile, new FileUtil.OutputLineHandler(writer) {
                        @Override
                        public void doLine(String line) throws Exception {
                            if (!condition.assertBean(JSON.parseObject(line, beanClass))) super.doLine(line);
                        }
                    });
                }
            });
        }
    }

    private class UpdateAction extends Action {
        Condition<Bean> condition;
        BeanSetter<Bean> setter;

        public UpdateAction(Condition<Bean> condition, BeanSetter<Bean> setter) {
            super(true);
            this.condition = condition;
            this.setter = setter;
        }

        @Override
        Bean modifyBean(Bean bean) {
            if (condition.assertBean(bean)) setter.set(bean);
            return bean;
        }

        @Override
        public void performTransaction() {
            updateByCondition(condition, setter);
        }

        @Override
        boolean modifyLogic() {
            return FileUtil.getBufferedWriter(tempFile, new FileUtil.BufferedWriterHandler() {
                @Override
                public boolean handle(final BufferedWriter writer) {
                    return FileUtil.eachLine(dataFile, new FileUtil.OutputLineHandler(writer) {
                        @Override
                        public void doLine(String line) throws Exception {
                            Bean bean = JSON.parseObject(line, beanClass);
                            if (condition.assertBean(bean)) setter.set(bean);
                            super.doLine(JSON.toJSONString(bean));
                        }
                    });
                }
            });
        }
    }

    FileBasedDataAccessComponent(Class<Bean> beanClass) {
        this.beanClass = beanClass;
        this.readWriteLock = new ReentrantReadWriteLock();
        this.transactionLock = new ReentrantLock();
        this.transaction = new ArrayList<Action>();


        String dataFileName = beanClass.getSimpleName();
        dataFile = new File(dataFileName);
        tempFile = new File(dataFileName + TEMP_EXT);


        if (dataFile.exists()) {
            tempFile.deleteOnExit();
        } else {
            if (tempFile.exists()) {
                FileUtil.overwrite(tempFile, dataFile);
            } else {
                dataFile = FileUtil.createFileIfNotExist(dataFileName);
            }
        }
    }

    @Override
    public boolean beginTransaction() {
        transactionLock.lock();
        transactionThread = Thread.currentThread();
        transaction.clear();
        return true;
    }

    @Override
    public boolean commit() {
        if (!transactionLock.isLocked()) {
            throw new IllegalStateException("no transaction not begins");
        }
        committing = true;
        readWriteLock.writeLock().lock();
        for (Action action : transaction) {
            action.performTransaction();
        }
        readWriteLock.writeLock().unlock();
        transactionUnlock();
        committing = false;
        return true;
    }

    @Override
    public boolean rollback() {
        if (!transactionLock.isLocked()) {
            throw new IllegalStateException("no transaction not begins");
        }
        transactionUnlock();
        return true;
    }

    private void transactionUnlock() {
        transaction.clear();
        transactionThread = null;
        transactionLock.unlock();
    }

    private boolean currentThreadTransaction() {
        return transactionLock.isLocked() && transactionThread == Thread.currentThread();
    }

    @Override
    public boolean add(Bean bean) {
        if (bean == null) {
            throw new NullPointerException("bean is null");
        }
        return new AddAction(bean).execute();
    }

    @Override
    public boolean deleteByCondition(final Condition<Bean> condition) {
        if (condition == null) {
            throw new NullPointerException("condition is null");
        }
        return new DeleteAction(condition).execute();
    }

    @Override
    public boolean updateByCondition(final Condition<Bean> condition, final BeanSetter<Bean> setter) {
        if (condition == null) {
            throw new NullPointerException("condition is null");
        }
        if (setter == null) {
            throw new NullPointerException("setter is null");
        }
        return new UpdateAction(condition, setter).execute();
    }

    @Override
    public List<Bean> selectByCondition(final Condition<Bean> condition) {
        if (condition == null) {
            throw new NullPointerException("condition is null");
        }
        readWriteLock.readLock().lock();
        final List<Bean> result = new ArrayList<Bean>();

        FileUtil.eachLine(dataFile, new FileUtil.LineHandler() {
            @Override
            public void doLine(String line) throws Exception {
                Bean bean = JSON.parseObject(line, beanClass);
                if (currentThreadTransaction()) {
                    for (Action action : transaction) {
                        bean = action.modifyBean(bean);
                        if (bean == null) return;
                    }
                }
                if (condition.assertBean(bean)) {
                    result.add(bean);
                }
            }
        });

        if (currentThreadTransaction()) {
            traverseAddAction:
            for (int i = 0; i < transaction.size(); i++) {
                Action action = transaction.get(i);
                if (AddAction.class.equals(action.getClass())) {
                    Bean bean = ((AddAction) action).element;
                    for (int j = i + 1; j < transaction.size(); j++) {
                        bean = transaction.get(j).modifyBean(bean);
                        if (bean == null) {
                            continue traverseAddAction;
                        }
                    }
                    if (condition.assertBean(bean)) {
                        result.add(bean);
                    }
                }
            }
        }

        readWriteLock.readLock().unlock();
        return result;
    }
}
