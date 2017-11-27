package com.pralay.fileMonitor;

import difflib.DiffUtils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;
import static java.nio.file.StandardWatchEventKinds.*;
import org.apache.commons.io.FilenameUtils;

public class FileMonitor {
    public static final String DEFAULT_WATCH_DIR = "./config";
    public static final String DEFAULT_SHADOW_DIR = "./configcopy";
    public static final int DEFAULT_WATCH_INTERVAL = 5;

    private Path watchDir;
    private Path shadowDir;
    private int watchInterval;
    private String watchFileName;
    private WatchService watchService;
    
    public FileMonitor(Path watchDir, Path shadowDir, int watchInterval, String watchFileName) throws IOException {
        this.watchDir = watchDir;
        this.shadowDir = shadowDir;
        this.watchInterval = watchInterval;
        this.watchFileName = watchFileName;
        watchService = FileSystems.getDefault().newWatchService();
    }

    public void run() throws InterruptedException, IOException {
    	//System.out.println("<--- Starting of File Monitoring --->");
        prepareShadowDir();
    	watchDir.register(watchService, ENTRY_MODIFY);
        while (true) {
            WatchKey watchKey = watchService.take();
            
            if(watchKey!=null){
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                Path oldFile = shadowDir.resolve((Path) event.context());
                Path newFile = watchDir.resolve((Path) event.context());
                List<String> oldContent;
                List<String> newContent;
                WatchEvent.Kind<?> eventType = event.kind();
                if (!(Files.isDirectory(newFile) || Files.isDirectory(oldFile))) {
                    if (eventType == ENTRY_MODIFY) { 
                        oldContent = fileToLines(oldFile);
                        newContent = fileToLines(newFile);
                        if(newFile.endsWith(watchFileName)){
                        	 printUnifiedDiff(newFile, oldFile, oldContent, newContent);
                        }
                        try {
                            Files.copy(newFile, oldFile, StandardCopyOption.REPLACE_EXISTING);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            watchKey.reset();
            Thread.sleep(1000 * watchInterval);
          }else System.out.println("No Events to update");
        }
    }

    private void prepareShadowDir() throws IOException {
        recursiveDeleteDir(shadowDir);
        Runtime.getRuntime().addShutdownHook(
            new Thread() {
                @Override
                public void run() {
                    try {
                        //System.out.println("Cleaning up shadow directory " + shadowDir);
                        recursiveDeleteDir(shadowDir);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        );
        recursiveCopyDir(watchDir, shadowDir);
}

    public static void recursiveDeleteDir(Path directory) throws IOException {
    	if (!directory.toFile().exists())
            return;
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void recursiveCopyDir(final Path sourceDir, final Path targetDir) throws IOException {
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, Paths.get(file.toString().replace(sourceDir.toString(), targetDir.toString())));
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(Paths.get(dir.toString().replace(sourceDir.toString(), targetDir.toString())));
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static List<String> fileToLines(Path path) throws IOException {
        List<String> lines = new LinkedList<>();
        String line;
        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
            while ((line = reader.readLine()) != null)
                lines.add(line);
        }
        catch (Exception e) {}
        return lines;
    }

    private static void printUnifiedDiff(Path oldPath, Path newPath, List<String> oldContent, List<String> newContent) {
    	List<String> diffLines = DiffUtils.generateUnifiedDiff(
            newPath.toString(),
            oldPath.toString(),
            oldContent,
            DiffUtils.diff(oldContent, newContent),
            0
        );
        
    	//System.out.println("If there is any change of input file: "+((diffLines.size()>0)?"Yes":"No"));
        if(diffLines.size()>0){
        	//System.out.println("<-- File Changes Exist. Printing below the changes -->");
        	for (String diffLine : diffLines){
	        	   if(!diffLine.startsWith("@@") && !diffLine.contains(oldPath.toString()) && !diffLine.contains(newPath.toString())){
	        		   if(diffLine.charAt(0)=='+' && diffLine.length()>1){
	        			   System.out.println("Line Added: "+diffLine.substring(1));
	        		   }else if(diffLine.charAt(0)=='-' && diffLine.length()>1){
	        			   System.out.println("Line Deleted: "+diffLine.substring(1));
	        		   }
	        	   }
	        }
        }else {//System.out.println("<-- File changes didn't exist -->");
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
    	String watchDirName=null;
    	String shadowDirName=null;
    	String watchFileName=null;
    	int watchInterval=0;
    	
    	if(args.length<0){
    		System.err.println("Please provide the file location to watch."
   				 + "\nFor Example - /app/test/test.txt ");
    		return;
    	}else{
    		if(args[0].contains("\\") || args[0].contains("//")){
    			System.err.println("Please provide a valid path. Path string should not contain double front slash or back slash - // or \\\\.");
    			return;
    		}else{
	    		watchDirName = FilenameUtils.getFullPath(args[0]);
	    		watchFileName = FilenameUtils.getName(args[0]);
	    		shadowDirName = watchDirName.substring(0,watchDirName.length()-1)+"copy";
	    		watchInterval = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_WATCH_INTERVAL;
	    	}
    	}
    	
        System.out.println("Input Directory: "+watchDirName);
        System.out.println("Input FileName: "+watchFileName);
        
        PrintStream ps = new PrintStream("./fileUpdateDetails.txt");
        System.setOut(ps);
        new FileMonitor(Paths.get(watchDirName), Paths.get(shadowDirName), watchInterval, watchFileName).run();
        ps.close();
    }
}
