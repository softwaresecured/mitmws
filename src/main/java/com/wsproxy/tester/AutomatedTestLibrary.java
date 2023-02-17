package com.wsproxy.tester;

import com.wsproxy.configuration.ApplicationConfig;

import java.util.ArrayList;

/*
    Manages a library of automated tests
 */
public class AutomatedTestLibrary {
    private String libraryFile = null;
    private ApplicationConfig applicationConfig = new ApplicationConfig();
    private ArrayList<AutomatedTestRun> testLibrary = new ArrayList<>();
    public AutomatedTestLibrary() {
        libraryFile = String.format("%s/%s", applicationConfig.getConfigDirPath(),".autotestlibrary");
    }
    /*
        Helper function for naming
     */
    /*
    public ArrayList<String> getTestNames() {
        ArrayList<String> testNames = new ArrayList<String>();
        for ( AutomatedTestRun testRun : testLibrary ) {
            testNames.add(testRun.getTestName());
        }
        return testNames;
    }

    public void deleteTestRun( String testId ) {
        for ( int i = 0; i < testLibrary.size(); i++ ) {
            if ( testLibrary.get(i).getTestId().equals(testId)) {
                testLibrary.remove(i);
                break;
            }
        }
    }
    public ArrayList<AutomatedTestRun> getTestLibrary(){
        return testLibrary;
    }

    public void reload() {
        try {
            byte envBytes[] = Files.readAllBytes(Paths.get(libraryFile));
            ByteArrayInputStream bis = new ByteArrayInputStream(envBytes);
            ObjectInput ois = new ObjectInputStream(bis);
            Object obj = ois.readObject();
            testLibrary = (ArrayList<AutomatedTestRun>) obj;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void saveAll() {
        save();
        reload();
    }

    public void save() {
        try {
            FileOutputStream logStream = new FileOutputStream(libraryFile);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = null;
            oos = new ObjectOutputStream(bos);
            oos.writeObject(testLibrary);
            oos.flush();
            logStream.write(bos.toByteArray());
            logStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     */
}
