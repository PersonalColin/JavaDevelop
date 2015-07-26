/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.colin;

import java.net.Socket;

/**
 * Defines the group of socket and the corresponding thread of the socket.
 * @author colin
 */
public class SocketGroup {
    private final Socket socket;
    private final Thread thread;
    
    public SocketGroup(Socket socket, Thread thread)
    {
        this.socket = socket;
        this.thread = thread;
    }
    
    public Socket toSocket()
    {
        return this.socket;
    }
    
    public Thread toThread()
    {
        return this.thread;
    }
}
