package cn.edu.fudan.se.dac;

import java.io.*;

/**
 * Created by Dawnwords on 2015/5/21.
 */
class FileUtil {

    static File createFileIfNotExist(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Can not create file " + fileName);
            }
        }
        return file;
    }

    static boolean copy(final File src, File dst) {
        return getBufferedWriter(dst, new BufferedWriterHandler() {
            @Override
            public boolean handle(BufferedWriter writer) {
                return eachLine(src, new OutputLineHandler(writer));
            }
        });
    }

    static boolean eachLine(File file, LineHandler handler) {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
            String line;
            while ((line = in.readLine()) != null) {
                handler.doLine(line);
            }
        } catch (Exception e) {
            return false;
        } finally {
            close(in);
        }
        return true;
    }

    static boolean appendLine(File file, final String line) {
        return getBufferedWriter(file, new BufferedWriterHandler() {
            @Override
            public boolean handle(BufferedWriter writer) {
                try {
                    new OutputLineHandler(writer).doLine(line);
                } catch (Exception e) {
                    return false;
                }
                return true;
            }
        });
    }

    static boolean getBufferedWriter(File file, BufferedWriterHandler handler) {
        BufferedWriter writer = null;
        boolean result = false;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "utf-8"));
            result = handler.handle(writer);
        } catch (Exception ignore) {
            return false;
        } finally {
            FileUtil.close(writer);
        }
        return result;
    }

    static boolean close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    static boolean overwrite(File src, File dst) {
        boolean result = dst.delete();
        result =  src.renameTo(dst) && result;
        return result;
    }

    interface BufferedWriterHandler {
        boolean handle(BufferedWriter writer);
    }

    interface LineHandler {
        void doLine(String line) throws Exception;
    }

    static class OutputLineHandler implements LineHandler {
        BufferedWriter writer;

        public OutputLineHandler(BufferedWriter writer) {
            this.writer = writer;
        }

        @Override
        public void doLine(String line) throws Exception {
            writer.write(line);
            writer.newLine();
        }
    }

}
