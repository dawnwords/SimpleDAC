package cn.edu.fudan.se.dac;

import com.alibaba.fastjson.JSON;

import java.io.BufferedWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Dawnwords on 2015/5/21.
 */
final class FileBasedDataAccessComponent<Bean> implements DataAccessInterface<Bean> {

    private static final String TEMP_EXT = ".tmp";

    private File dataFile, changeTempFile;
    private List<Action> transaction;
    private Class<Bean> beanClass;
    private Lock readLock;
    private Lock writeLock;
    private ReentrantLock transactionLock;
    private boolean committing;

    private abstract class Action {
        private boolean createTemp;

        protected Action(boolean createTemp) {
            this.createTemp = createTemp;
        }

        boolean execute() {
            if (committing) {
                return modifyLogic();
            }

            if (transactionLock.isLocked()) {
                transaction.add(this);
                return true;
            }

            writeLock.lock();
            boolean result = modifyLogic();
            if (createTemp) {
                result = result && FileUtil.overwrite(changeTempFile, dataFile);
            }
            writeLock.unlock();
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
            return FileUtil.getBufferedWriter(changeTempFile, new FileUtil.BufferedWriterHandler() {
                @Override
                public boolean handle(BufferedWriter writer) {
                    return FileUtil.eachLine(dataFile, new FileUtil.OutputLineHandler(writer), new ConditionFilter(condition, beanClass));
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
            return FileUtil.getBufferedWriter(changeTempFile, new FileUtil.BufferedWriterHandler() {
                @Override
                public boolean handle(final BufferedWriter writer) {
                    return FileUtil.eachLine(dataFile, new FileUtil.OutputLineHandler(writer) {
                        @Override
                        public void filter(String line) throws Exception {
                            Bean bean = JSON.parseObject(line, beanClass);
                            setter.set(bean);
                            keep(JSON.toJSONString(bean));
                        }
                    }, new ConditionFilter(condition, beanClass));
                }
            });
        }
    }

    FileBasedDataAccessComponent(Class<Bean> beanClass) {
        this.beanClass = beanClass;
        this.transactionLock = new ReentrantLock();
        this.transaction = new ArrayList<Action>();

        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        this.readLock = readWriteLock.readLock();
        this.writeLock = readWriteLock.writeLock();

        String dataFileName = beanClass.getSimpleName();
        dataFile = new File(dataFileName);
        changeTempFile = new File(dataFileName + TEMP_EXT);


        if (dataFile.exists()) {
            changeTempFile.deleteOnExit();
        } else {
            if (changeTempFile.exists()) {
                FileUtil.overwrite(changeTempFile, dataFile);
            } else {
                dataFile = FileUtil.createFileIfNotExist(dataFileName);
            }
        }
    }

    @Override
    public boolean beginTransaction() {
        transactionLock.lock();
        transaction.clear();
        return true;
    }

    @Override
    public boolean commit() {
        if (!transactionLock.isLocked()) {
            throw new IllegalStateException("no transaction not begins");
        }
        committing = true;
        writeLock.lock();
        for (Action action : transaction) {
            action.performTransaction();
        }
        writeLock.unlock();
        transactionLock.unlock();
        committing = false;
        return true;
    }

    @Override
    public boolean rollback() {
        if (!transactionLock.isLocked()) {
            throw new IllegalStateException("no transaction not begins");
        }
        transaction.clear();
        return true;
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
        readLock.lock();
        final List<Bean> result = new ArrayList<Bean>();

        FileUtil.eachLine(dataFile, new FileUtil.LineHandler() {
            @Override
            public void filter(String line) throws Exception {
                Bean bean = JSON.parseObject(line, beanClass);
                if (transactionLock.isLocked()) {
                    for (Action action : transaction) {
                        bean = action.modifyBean(bean);
                        if (bean == null) return;
                    }
                }
                if (condition.assertBean(bean)) {
                    result.add(bean);
                }
            }

            @Override
            public void keep(String line) throws Exception {
            }
        }, new ConditionFilter(Condition.True, beanClass));

        if (transactionLock.isLocked()) {
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

        readLock.unlock();
        return result;
    }

    private class ConditionFilter implements FileUtil.LineFilter {

        private Condition<Bean> condition;
        private Class<Bean> beanClass;

        public ConditionFilter(Condition<Bean> condition, Class<Bean> beanClass) {
            this.condition = condition;
            this.beanClass = beanClass;
        }

        @Override
        public boolean shouldBeFiltered(String line) {
            return condition.assertBean(JSON.parseObject(line, beanClass));
        }
    }
}
