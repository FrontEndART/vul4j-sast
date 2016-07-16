/**
 * Copyright (c) 2016 Company.
 * All rights reserved.
 */
package com.mycompany.concurrency.threads;

/**
 * Helper class to execute other classes.
 */
public final class ThreadsLauncher {

  private ThreadsLauncher() {}

  public static void main(final String[] args) {
    executeInSync();
  }

  private static void executeInSync() {
    StringBuffer sb = new StringBuffer("A");

    new InSync(sb).start();
    new InSync(sb).start();
    new InSync(sb).start();
  }
}
