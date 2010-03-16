/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import java.util.*;
import java.io.*;
import java.net.*;

class BroadcastClientHandler extends Thread {

  protected Socket incoming;
  protected int id;
  protected BufferedReader in;
  protected PrintWriter out;

  public BroadcastClientHandler(Socket incoming, int id) {
    this.incoming = incoming;
    this.id = id;
    try {
      if (incoming != null) {
        in = new BufferedReader(new InputStreamReader(incoming.getInputStream()));         
        out = new PrintWriter(new OutputStreamWriter(incoming.getOutputStream()));         
      }
    } catch (Exception e) {
      System.out.println("Error: " + e);
    }
  }

  public synchronized void putMessage(String msg) {
    if (out != null) {
      out.println(msg);
      out.flush();
    }
  }

  public void run() {
    System.out.println("Client handler " + id + " started.");
    if (in != null &&
        out != null) {
      putMessage("Hello! This is Java BroadcastEchoServer. Enter BYE to exit.");      
      try {
        for (;;) {
          String str = in.readLine();
          if (str == null) {
            break;
          } else {
            putMessage("Echo: " + str);
            System.out.println("Received (" + id + "): " + str);

            if (str.trim().equals("BYE")) {
              break;
            } else {
              Enumeration en = BroadcastEchoServer.activeThreads.elements();
              while  (en.hasMoreElements()) {
                BroadcastClientHandler t =     
                    (BroadcastClientHandler) en.nextElement();
                if (t != this) {
                  t.putMessage("Broadcast(" + id + "): " + str);
                }
              }
            }
          }
        }
        incoming.close();
        BroadcastEchoServer.activeThreads.removeElement(this);
      } catch (IOException e) {}
    }
    System.out.println("Client thread " + id + " stopped.");
  }

}

public class BroadcastEchoServer {

  static protected Vector activeThreads;

  public static void main(String[] args) {
    System.out.println("BroadcastEchoServer started.");
    activeThreads = new Vector();
    int i = 1;
    try {
      ServerSocket s = new ServerSocket(8010);
      for (;;) {
        Socket incoming = s.accept();
        System.out.println("Spawning client thread " + i);
        BroadcastClientHandler newThread =
          new BroadcastClientHandler(incoming, i);
        activeThreads.addElement(newThread);
        newThread.start();
        i++;
      }
    } catch (Exception e) {
      System.out.println("Error: " + e);
    }

    System.out.println("BroadcastEchoServer stopped.");
  }
}