package com.wsproxy.util;

import com.wsproxy.configuration.ApplicationConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public final class ScriptUtil {
    public static ArrayList<String> getScriptsByType (String scriptType ) {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        String scriptPath = String.format("%s/scripts/%s",applicationConfig.getConfigDirPath(), scriptType );
        ArrayList<String> scripts = new ArrayList<>();
        File scriptFolder = new File(scriptPath);
        File[] files = scriptFolder.listFiles();
        if ( files != null ) {
            for (File file : files) {
                if (file.isFile()) {
                    scripts.add(file.getName());
                }
            }
        }
        return scripts;
    }

    public static String runCommand( String[] arguments ) {
        String output = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(arguments);
            Process p = pb.start();
            p.waitFor();
            output = readStream(p.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return output;
    }

    public static String runScript( String path, Object... args ) {
        String output = null;
        ApplicationConfig applicationConfig = new ApplicationConfig();
        if ( args == null ) {
            args = new Object[]{};
        }
        String scriptArgs[] = new String[args.length+2];
        scriptArgs[0] = applicationConfig.getProperty("scripts.pythonpath");
        scriptArgs[1] = path;
        for ( int i = 0; i < args.length; i++ ) {
            scriptArgs[2+i] = args[i].toString();
        }
        try {
            ProcessBuilder pb = new ProcessBuilder(scriptArgs);
            Process p = pb.start();
            p.waitFor();
            output = readStream(p.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return output;
    }

    private static String readStream(InputStream is)  {
        byte[] readStreamBytes = new byte[0];
        StringBuilder sb = new StringBuilder();
        ArrayList<byte[]> blocks = new ArrayList<>();
        int bytesRead = 0;
        int totalBytes = 0;
        try {
            do {
                byte[] readBlock = new byte[1024];
                bytesRead = is.read(readBlock);
                if ( bytesRead > 0 ) {
                    totalBytes += bytesRead;
                    byte[] curBlock = new byte[bytesRead];
                    System.arraycopy(readBlock,0,curBlock,0,bytesRead);
                    blocks.add(curBlock);
                }
            } while ( bytesRead >= 0 );

            if ( totalBytes > 0 ) {
                int offset = 0;
                readStreamBytes = new byte[totalBytes];
                for ( byte[] block : blocks ) {
                    System.arraycopy(block,0,readStreamBytes,offset,block.length);
                    offset += block.length;
                }
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return new String(readStreamBytes);
    }
}
