/**
 * Copyright (c) 2015 Company.
 * All rights reserved.
 */
package com.mycompany.exercises.io;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

public class MyFileTest {

  public MyFileTest() {}

  @Ignore
  @Test
  public void testProcessStringsFromFile() {
    System.out.println("processStringsFromFile");
    Path file = null;
    MyFile.processStringsFromFile(file);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  @Test
  public void testListFilesInCurrentDirectory() {
    List<Path> files = MyFile.listFilesInCurrentDirectory();
    System.out.println("The names of all the files in the current directory: ");
    files.stream().forEach(System.out::println);
  }

  @Test
  public void testListFilesInDirectory() {
    List<Path> files = MyFile.listFilesInDirectory(Paths.get("."));
    System.out.println("The names of all the files in the directory: ");
    files.stream().forEach(System.out::println);
  }

  @Test
  public void testListSubdirectoriesInCurrentDirectory() {
    List<Path> files = MyFile.listSubdirectoriesInCurrentDirectory();
    System.out.println("The subdirectories in the current directory: ");
    files.stream().forEach(System.out::println);
  }

  @Test
  public void testListSubdirectoriesInDirectory() {
    List<Path> files = MyFile.listSubdirectoriesInDirectory(Paths.get("."));
    System.out.println("The subdirectories in the directory: ");
    files.stream().forEach(System.out::println);
  }

  @Test
  public void testListSelectFilesInDirectory() {
    List<Path> files = MyFile.listSelectFilesInDirectory(Paths.get("."), ".java");
    System.out.println("The names of all java files in the directory: ");
    files.stream().forEach(System.out::println);
  }

  @Test
  public void testListAllHiddenFilesInDirectory() {
    List<File> files = MyFile.listHiddenFilesInDirectory(Paths.get("."));
    System.out.println("The names of all hidden files in the directory: ");
    files.stream().forEach(System.out::println);
  }

  @Test
  public void testListImmediateSubdirectoriesInDirectory() {
    List<File> files = MyFile.listImmediateSubdirectoriesInDirectory(Paths.get("."));
    System.out.println("List the immediate (one level deep) subdirectories in the directory: ");
    files.stream().forEach(System.out::println);
  }

  // Manual test - refactor!
  @Ignore
  @Test
  public void testWatchForFileChangesInDirectory() {
    MyFile.watchForFileChangesInDirectory(Paths.get("."));
  }

}
