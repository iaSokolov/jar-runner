package ru.isokolov.jar.runner;

import java.io.IOException;

public class JarApplicationProcessor {    
    private static Process process;               
    
    public static Process run(String jarPath) throws IOException {
        if (process != null) {
            stop();
        }
       
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", jarPath);
        process = processBuilder.start(); 
        
        return process;
    }
    
    public static void stop() {
        if (process != null) {
            process.destroy();
            process = null;
        }
    }
    
    public static Process getProcess() {
        return process;
    }
}
