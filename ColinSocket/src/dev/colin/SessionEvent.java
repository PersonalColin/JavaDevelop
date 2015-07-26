/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dev.colin;

import java.net.*;

/**
 * Declarate the interfaces for the event when a socket is connected, when a
 * socket is closed and when a socket receives data from the remote end. The
 * interfaces can be realized according to the detailed application. It is 
 * suitable for both TCP socket and UDP socket.
 * @author Colin-WJ
 */
public interface SessionEvent {
    /**
     * 
     * @param address    The address of the remote end in SocketAddress type.
     */
    public void session_connect_event(SocketAddress address);
    
    /**
     * 
     * @param address  The address of the remote end in SocketAddress type.
     */
    public void session_close_event(SocketAddress address);
    
    /***
     * @param address The address of the remote end in SocketAddress type.
     * @param data the data array for the received data from the session.
     * @param length the length of the received data.
     */
    public void session_read_event(SocketAddress address, byte[] data, int length);
}
