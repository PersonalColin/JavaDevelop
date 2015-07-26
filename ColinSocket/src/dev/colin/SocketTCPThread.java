/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dev.colin;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * The Thread for TCP socket connection to monitor the data from the remote end.
 * If there is any data from the remote end, it will invoke the session_read_event
 * function in the interface SessionEvent.
 * @author Colin-WJ
 */
public class SocketTCPThread extends Thread
{
    private final Socket socket;
    private final SocketAddress soc_addr;
    private final SessionEvent session_event;
    private final DataInputStream buff_reader;
    
    /**
     * Constructure to import socket of the TCP connection and the SessionEvent
     * interface for data received and connection closed.
     * @param socket The socket of the TCP connection.
     * @param event The SessionEvent interface object.
     * @throws IOException It is inherited from getInputStream of Socket class.
     */
    public SocketTCPThread(Socket socket, SessionEvent event) throws IOException
    {
        this.socket = socket;
        this.session_event = event;
        this.buff_reader = new DataInputStream(socket.getInputStream());

        this.soc_addr = socket.getRemoteSocketAddress();
    }

    @Override
    public void run()
    {
        while (!isInterrupted())
        {
            try
            {
                byte[]  data = new byte[SocketUtils.MAX_PACKET_LEN];
                int     length;
                                
                while ((length = buff_reader.read(data)) > 0)
                {
                    this.session_event.session_read_event(this.soc_addr, data, length);
                }
                this.session_event.session_close_event(this.soc_addr);
                break;
            }
            catch (IOException ex)
            {
                System.out.println(ex.toString());
            }
        }
        System.out.println("The thread " + this.getName() + " is run over!");
    }
}
