package edu.cornell.cis3152.physics;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SavedDataHandler {
    private static String OS = System.getProperty("os.name").toLowerCase();

    static private boolean isAndroid = System.getProperty("java.runtime.name").contains("Android");
    static private boolean isMac = !isAndroid && OS.contains("mac");
    static private boolean isWindows = !isAndroid && OS.contains("windows");
    static private boolean isLinux = !isAndroid && OS.contains("linux");
    static private boolean isIos = !isAndroid && (!(isWindows || isLinux || isMac)) || OS.startsWith("ios");

    static private boolean isARM = System.getProperty("os.arch").startsWith("arm") || System.getProperty("os.arch").startsWith("aarch64");
    static private boolean is64Bit = System.getProperty("os.arch").contains("64") || System.getProperty("os.arch").startsWith("armv8");

    private static boolean isGwt = false;

    private static String applicationName = "TheGildedFlame";
    private HashMap<String, String> savedData;
    /**
     * Credit to Alfred Reibenschuh, 2021. for code related to System identification/file location
     * <a href="https://github.com/libgdx/libgdx/issues/6559#issuecomment-890307952">Original code</a>
     */

    public SavedDataHandler() {
        try {
            Class.forName("com.google.gwt.core.client.GWT");
            isGwt = true;
        }
        catch(Exception ignored) { /* IGNORED */ }

        boolean isMOEiOS = "iOS".equals(System.getProperty("moe.platform.name"));
        if (isMOEiOS || (!isAndroid && !isWindows && !isLinux && !isMac)) {
            isIos = true;
            isAndroid = false;
            isWindows = false;
            isLinux = false;
            isMac = false;
            is64Bit = false;
        }
        savedData =  new HashMap<String, String> ();
    }

    private static String getUserDataDirectory()
    {
        String DATA_HOME = null;

        if((DATA_HOME = System.getenv("XDG_DATA_HOME"))==null)
        {
            if(isLinux || isAndroid)
            {
                DATA_HOME = System.getProperty("user.home")+"/.local/share";
            }
            else if(isMac)
            {
                DATA_HOME = System.getProperty("user.home")+"/Library/Application Support";
            }
            else if(isIos)
            {
                DATA_HOME = System.getProperty("user.home")+"/Documents";
            }
            else if(isWindows)
            {
                if((DATA_HOME = System.getenv("APPDATA"))==null)
                {
                    DATA_HOME = System.getProperty("user.home")+"/Local Settings/Application Data";
                }
            }
        }
        return DATA_HOME+"/"+applicationName ;
    }

    public <T> void setDataVal (String dataName, T value) {
        File file = new File(getUserDataDirectory() + "/" + "savedData.json");
        FileHandle fileHandle = new FileHandle(file);

        fileParser(file);
        savedData.put(dataName, value.toString());

        try {
            fileHandle.writeString(fileCrafter(), false);
        } catch (Exception e) {
            System.err.println( "FAILED TO SAVE FILE\n" + e);
        }
    }

    public String getDataVal (String name) {
        File file = new File(getUserDataDirectory() + "/" + "savedData.json");
        fileParser(file);
        return savedData.get(name);
    }

    public void eraseSavedData(String name) {
        File file = new File(getUserDataDirectory() + "/" + "savedData.json");
        file.delete();
    }

    private void fileParser (File file) {
        List<String> data = new ArrayList<String>();
        savedData.clear();
        try {
            data = Files.readAllLines(file.toPath());
        } catch (Exception ignored) {  }

        for (String line : data) {
            String[] split = line.split(":");
            savedData.put(split[0], split[1].replace("\n", ""));
        }
    }

    private String fileCrafter() {
        StringBuilder output = new StringBuilder();
        for (String key : savedData.keySet()) {
            output.append(key).append(":").append(savedData.get(key)).append("\n");
        }
        return output.toString();
    }
}
