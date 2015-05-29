package cn.edu.fudan.se.dac;

import com.alibaba.fastjson.JSON;

import java.io.BufferedWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dawnwords on 2015/5/21.
 */
final class FileBasedDataAccessComponent<Bean> implements DataAccessInterface<Bean> {

    private static final String TEMP_EXT = ".tmp";
    private static final String OLD_EXT = ".old";
    private static final String CHANGE_TEMP_EXT = ".ctmp";

    private File dataFile, oldFile, tempFile, changeTempFile;
    private boolean transaction;
    private Class<Bean> beanClass;

    FileBasedDataAccessComponent(Class<Bean> beanClass) {
        this.beanClass = beanClass;

        String dataFileName = beanClass.getSimpleName();
        dataFile = new File(dataFileName);
        oldFile = new File(dataFileName + OLD_EXT);
        tempFile = new File(dataFileName + TEMP_EXT);
        changeTempFile = new File(dataFileName + CHANGE_TEMP_EXT);

        boolean dataExists = dataFile.exists();
        boolean oldExists = oldFile.exists();
        boolean tempExists = tempFile.exists();

        if (tempExists) {
            if (dataExists) {
                tempFile.delete();
            } else if (oldExists) {
                tempFile.renameTo(dataFile);
            }
        } else if (dataExists) {
            if (oldExists) {
                oldFile.delete();
            }
        } else if (changeTempFile.exists()) {
            changeTempFile.renameTo(dataFile);
        } else {
            dataFile = FileUtil.createFileIfNotExist(dataFileName);
        }

        changeTempFile.deleteOnExit();
    }

    @Override
    public boolean beginTransaction() {
        try {
            transaction = FileUtil.copy(dataFile, tempFile);
            return transaction;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean commit() {
        transaction = false;
        return dataFile.renameTo(oldFile) && tempFile.renameTo(dataFile) && oldFile.delete();
    }

    @Override
    public boolean rollback() {
        transaction = false;
        return tempFile.delete();
    }

    @Override
    public boolean add(Bean bean) {
        String json = JSON.toJSONString(bean);
        return FileUtil.appendLine(getDataFile(), json);
    }

    @Override
    public boolean deleteByCondition(final Condition<Bean> condition) {
        return FileUtil.getBufferedWriter(changeTempFile, new FileUtil.BufferedWriterHandler() {
            @Override
            public boolean handle(BufferedWriter writer) {
                return FileUtil.eachLine(getDataFile(), new FileUtil.OutputLineHandler(writer), new ConditionFilter(condition, beanClass));
            }
        }) && FileUtil.overwrite(changeTempFile, getDataFile());
    }

    @Override
    public boolean updateByCondition(final Condition<Bean> condition, final BeanSetter<Bean> setter) {
        return setter == null || (FileUtil.getBufferedWriter(changeTempFile, new FileUtil.BufferedWriterHandler() {
            @Override
            public boolean handle(final BufferedWriter writer) {
                return FileUtil.eachLine(getDataFile(), new FileUtil.OutputLineHandler(writer) {
                    @Override
                    public void filter(String line) throws Exception {
                        Bean bean = JSON.parseObject(line, beanClass);
                        setter.set(bean);
                        keep(JSON.toJSONString(bean));
                    }
                }, new ConditionFilter(condition, beanClass));
            }
        }) && FileUtil.overwrite(changeTempFile, getDataFile()));
    }

    @Override
    public List<Bean> selectByCondition(final Condition<Bean> condition) {
        final List<Bean> result = new ArrayList<Bean>();
        FileUtil.eachLine(getDataFile(), new FileUtil.LineHandler() {
            @Override
            public void filter(String line) throws Exception {
                result.add(JSON.parseObject(line, beanClass));
            }

            @Override
            public void keep(String line) throws Exception {
            }
        }, new ConditionFilter(condition, beanClass));
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

    private File getDataFile() {
        return transaction ? tempFile : dataFile;
    }
}
