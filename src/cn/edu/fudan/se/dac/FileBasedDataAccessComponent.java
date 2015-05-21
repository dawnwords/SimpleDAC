package cn.edu.fudan.se.dac;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.File;
import java.lang.reflect.Field;
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
    private Gson gson;
    private boolean transaction;

    public FileBasedDataAccessComponent(String dataFileName) {
        gson = new Gson();

        dataFile = new File(dataFileName);
        oldFile = new File(dataFileName + OLD_EXT);
        tempFile = new File(dataFileName + TEMP_EXT);
        changeTempFile = new File(dataFileName + CHANGE_TEMP_EXT);

        boolean dataExists = dataFile.exists();
        boolean oldExists = oldFile.exists();
        boolean tempExists = tempFile.exists();

        changeTempFile.deleteOnExit();

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
        } else {
            dataFile = FileUtil.createFileIfNotExist(dataFileName);
        }

    }


    @Override
    public boolean beginTransaction() {
        try {
            transaction = FileUtil.copy(dataFile, tempFile, null);
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
    public boolean add(Bean bean) {
        String json = gson.toJson(bean);
        return FileUtil.appendLine(getDataFile(), json);
    }

    @Override
    public boolean deleteByField(final String fieldName, final Object value, final Class<Bean> beanClass) {
        return FileUtil.getBufferedWriter(changeTempFile, new FileUtil.BufferedWriterHandler() {
            @Override
            public boolean handle(BufferedWriter writer) {
                return FileUtil.eachLine(getDataFile(), new FileUtil.OutputLineHandler(writer), new EqualFilter(beanClass, value, fieldName));
            }
        });
    }

    @Override
    public boolean updateByField(final String selectFieldName, final Object selectValue, final Class<Bean> beanClass,
                                 final String updateFieldName, final Object updateValue) {
        return FileUtil.getBufferedWriter(changeTempFile, new FileUtil.BufferedWriterHandler() {
            @Override
            public boolean handle(final BufferedWriter writer) {
                return FileUtil.eachLine(getDataFile(), new FileUtil.LineHandler() {
                    @Override
                    public void handle(String line) throws Exception {
                        Bean bean = gson.fromJson(line, beanClass);
                        Field field = beanClass.getField(updateFieldName);
                        field.setAccessible(true);
                        field.set(bean, updateValue);
                        writer.write(gson.toJson(bean));
                        writer.newLine();
                    }
                }, new EqualFilter(beanClass, selectValue, selectFieldName));
            }
        });
    }

    @Override
    public List<Bean> selectByField(String fieldName, Object value, final Class<Bean> beanClass) {
        final List<Bean> result = new ArrayList<Bean>();
        FileUtil.eachLine(getDataFile(), new FileUtil.LineHandler() {
            @Override
            public void handle(String line) throws Exception {
                result.add(gson.fromJson(line, beanClass));
            }
        }, new EqualFilter(beanClass, value, fieldName));
        return result;
    }

    private File getDataFile() {
        return transaction ? tempFile : dataFile;
    }

    private class EqualFilter implements FileUtil.LineFilter {
        private Class<Bean> beanClass;
        private Object value;
        private String fieldName;

        public EqualFilter(Class<Bean> beanClass, Object value, String fieldName) {
            this.beanClass = beanClass;
            this.value = value;
            this.fieldName = fieldName;
        }

        @Override
        public boolean filter(String line) {
            Bean bean = gson.fromJson(line, beanClass);
            try {
                Field field = beanClass.getField(fieldName);
                field.setAccessible(true);
                return field.get(bean).equals(value);
            } catch (Exception e) {
                return false;
            }
        }
    }
}
