package me.TechsCode.TechDiscordBot.util;

import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Project {

    public static String[] getFiles(){
        ArrayList<String> names = new ArrayList<>();

        try {
            CodeSource src = Project.class.getProtectionDomain().getCodeSource();
            if (src != null) {
                URL jar = src.getLocation();
                ZipInputStream zip = new ZipInputStream(jar.openStream());
                while(true) {
                    ZipEntry e = zip.getNextEntry();
                    if (e == null)
                        break;
                    names.add(e.getName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return names.toArray(new String[names.size()]);
    }

    public static Class[] getClasses(String prefix){
        return Arrays.stream(getFiles())
                .filter(fileName -> fileName.endsWith(".class"))
                .map(className -> className.replace("/", ".").replace(".class", ""))
                .filter(fileName -> fileName.startsWith(prefix))
                .map(className -> {
                    try {
                        return Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toArray(Class[]::new);


    }
}
