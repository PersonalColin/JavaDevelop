/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.colin;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

/**
 * The thread for UDP connection to monitor data received from the remote end.
 * If there is any data received, it will push a connect event to let the application
 * know receiving data from a UDP end. Anyway, if the application think this is
 * not necessary, it can define an empty function for the connect event.
 * @author colin
 */
public class SocketUDPThread extends Thread {
    private final DatagramSocket socket;
    private final SessionEvent session_event;
    private final DatagramPacket data_packet;
    
    /**
     * Constructure to import socket of the UDP connection and the SessionEvent
     * interface for data received and connection closed.
     * @param socket The socket of the UDP connection.
     * @param event The SessionEvent interface object.
     */
    public SocketUDPThread(DatagramSocket socket, SessionEvent event)
    {
        this.socket = socket;
        this.session_event = event;
        this.data_packet = new DatagramPacket(
                new byte[SocketUtils.MAX_PACKET_LEN],
                SocketUtils.MAX_PACKET_LEN);
    }

    @Override
    public void run()
    {
        while (!isInterrupted())
        {
            try
            {
                while (true)
                {
                    this.socket.receive(this.data_packet);
                    
                    SocketAddress soc_addr = this.data_packet.getSocketAddress();
                    /**
                     * It is necessary to push connect event first. The app who
                     * provide the event interfaces probably needs to record the
                     * connection information first.
                     */
                    this.session_event.session_connect_event(soc_addr);
                    
                    this.session_event.session_read_event(soc_addr,
                            this.data_packet.getData(),
                            this.data_packet.getLength());
                }
            }
            catch (IOException ex)
            {
                System.out.println(ex.toString());
            }
        }
        System.out.println("The thread " + this.getName() + " is run over!");
    }    
}
