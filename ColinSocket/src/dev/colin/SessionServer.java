/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 * This class manager to TCP server socket or UDP server socket
 */

package dev.colin;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * This class can define a TCP or UDP server. With this class, we can make the
 * development of server easier. Firstly, we can use the construct function to
 * create a TCP server or UDP server and use the function start listen to start
 * the server. Then all connect, read and close event can be implemented in the
 * interface SessionEvent which is implemented by yourself. With this class, we
 * don't have care about the designation of thread for TCP connection or UDP 
 * connection. It has been implemented in the class.
 * @author Colin-WJ
 */
public class SessionServer {
    private String server_type;
    private ServerSocket tcp_server_socket;
    private DatagramSocket udp_server_socket;
    private HashMap<SocketAddress, SocketGroup>    socket_map;
    private SessionEvent    session_event;
    private Thread          udp_thread = null;

    /**
     * constructure with server type (TCP/UDP), port and the session event
     * interface forsession is connected and data received and session closed.
     * @param type  TCP/UDP server type.
     * @param port  port number to listen.
     * @param event event interface for connected, read and close.
     */
    public SessionServer(String type, int port, SessionEvent event)
    {
        this.server_type    = type;
        this.session_event  = event;
        this.socket_map     = new HashMap<>();
        
        try
        {
            /*Open the correct listening socket type according to the server
            we set.*/
            switch(type)
            {
                case SocketUtils.TCP_TYPE_NAME:
                {
                    tcp_server_socket = new ServerSocket(port);
                    break;
                }
                case SocketUtils.UDP_TYPE_NAME:
                {
                    udp_server_socket = new DatagramSocket(port);
                    break;
                }
            }
        }
        
        catch (IOException ex)
        {
            System.out.println(ex.toString());
        }
    }

    /**
     * Start to listen the port of the server. For UDP server, it will start
     * a thread for receiving data from the remote end. If the server receive
     * data from a remote UDP end for the first time, it will push a connect
     * event.
     */
    public void start_listen()
    {
        switch(this.server_type)
        {
            case SocketUtils.TCP_TYPE_NAME:
            {
                start_tcp_listen();
                break;
            }
            case SocketUtils.UDP_TYPE_NAME:
            {
                start_udp_listen();
                break;
            }
        }
    }
    
    /**
     * close the TCP or UDP server, with this function, all connected end will
     * be closed and deleted.
     */
    public void close_server()
    {
        try
        {
            switch(server_type)
            {
                case SocketUtils.TCP_TYPE_NAME:
                {
                    close_tcp_server();
                    break;
                }
                case SocketUtils.UDP_TYPE_NAME:
                {
                    close_udp_server();
                    break;
                }
            }
        }        
        catch (Exception ex)
        {
            System.out.println(ex.toString());
        }
    }
    
    /**
     * Close the specified connection according to the remote address.
     * @param remote_address the address of the remote end of the socket.
     */
    public void close_socket(SocketAddress remote_address)
    {
        try
        {
            switch (this.server_type)
            {
                case SocketUtils.TCP_TYPE_NAME:
                {
                    close_tcp_socket(remote_address);
                    break;
                }

                case SocketUtils.UDP_TYPE_NAME:
                {
                    close_udp_socket(remote_address);
                    break;
                }
            }
        }
        catch(Exception ex)
        {
            System.out.println(ex.toString());
        }
    }
    
    /**
     * Send data in bytes to the specified remote end.
     * @param remote_address the address of the remote end.
     * @param data the data to send.
     * @param length the length of the data to send.
     */
    public void socket_write_data(SocketAddress remote_address, byte[] data, int length)
    {
        try
        {
            switch(this.server_type)
            {
                case SocketUtils.TCP_TYPE_NAME:
                {
                    write_tcp_data(remote_address, data, length);
                    break;
                }
                case SocketUtils.UDP_TYPE_NAME:
                {
                    write_udp_data(remote_address, data, length);
                    break;
                }
            }
        }
        catch(Exception ex)
        {

            System.out.println(ex.toString());
        }
    }
    
    /**
     * Send data in String to the specified remote end.
     * @param remote_address the address of the remote end.
     * @param data the String data to send to the remote end.
     */
    public void socket_write_data(SocketAddress remote_address, String data)
    {
        try
        {
            switch(this.server_type)
            {
                case SocketUtils.TCP_TYPE_NAME:
                {
                    write_tcp_data(remote_address, data.getBytes(), data.length());
                    break;
                }
                case SocketUtils.UDP_TYPE_NAME:
                {
                    write_udp_data(remote_address, data.getBytes(), data.length());
                    break;
                }
            }
        }
        catch(Exception ex)
        {
            System.out.println(ex.toString());
        }
    }
    
    private void start_tcp_listen()
    {
        new Thread(){
            @Override
            public void run()
            {
                try
                {
                    System.out.println("TCP server started.");
                    while (true)
                    {
                        // Waiting for a TCP client connecting.
                        Socket s = tcp_server_socket.accept();

                        SocketAddress soc_addr = s.getRemoteSocketAddress();
                        session_event.session_connect_event(soc_addr);

                        // Create a new thread for the connected TCP client.
                        Thread tr = new SocketTCPThread(s, session_event);
                        tr.start();
                        socket_map.put(soc_addr, new SocketGroup(s, tr));
                    }
                }

                catch (IOException ex)
                {
                    System.out.println(ex.toString());
                }
            }
        }.start();
    }
    
    private void start_udp_listen()
    {
        System.out.println("UDP server started.");
        try
        {
            udp_thread = new SocketUDPThread(this.udp_server_socket, new SessionEvent(){
                @Override
                public void session_connect_event(SocketAddress address)
                {
                    if (put_new_soc_entry(address, null))
                    {
                        session_event.session_connect_event(address);
                    }
                }

                @Override
                public void session_close_event(SocketAddress address)
                {
                    session_event.session_close_event(address);
                }

                @Override
                public void session_read_event(SocketAddress address, byte[] data, int length)
                {
                    session_event.session_read_event(address, data, length);
                }
            });
            udp_thread.start();
        }
        catch(Exception ex)
        {
            System.out.println(ex.toString());
        }
    }
    
    private void close_tcp_server()
    {
        try
        {
            tcp_server_socket.close();

            /*Close and clear all client sockets*/
            Set<Map.Entry<SocketAddress, SocketGroup>> sets = socket_map.entrySet();
            Socket soc;
            Thread tr;

            for (Map.Entry<SocketAddress, SocketGroup> entry : sets)
            {
                /*Get the socket and thread for the connection.*/
                soc = entry.getValue().toSocket();
                tr  = entry.getValue().toThread();
                session_event.session_close_event(entry.getKey());

                /*Close the socket of the connection*/
                soc.close();

                /*stop the thread for the connection.*/
                tr.interrupt();
            }

            /*Clear all mappings in the hash map.*/
            socket_map.clear();
        }
        catch(Exception ex)
        {
            System.out.println(ex.toString());
        }
    }
    
    private void close_udp_server()
    {
        try
        {
            /*Close and clear all client sockets*/
            Set<Map.Entry<SocketAddress, SocketGroup>> sets = socket_map.entrySet();

            for (Map.Entry<SocketAddress, SocketGroup> entry : sets)
            {
                session_event.session_close_event(entry.getKey());
            }

            /*Clear all mappings in the hash map.*/
            socket_map.clear();
            
            if (null != udp_thread)
            {
                udp_thread.interrupt();
                udp_thread = null;
            }
            this.udp_server_socket.close();
        }
        catch(Exception ex)
        {
            System.out.println(ex.toString());
        }
    }
    
    private void close_tcp_socket(SocketAddress remote_address)
    {
        try
        {
            SocketGroup soc_group = socket_map.get(remote_address);

            soc_group.toSocket().close();
            soc_group.toThread().interrupt();

            socket_map.remove(remote_address);
        }
        
        catch(Exception ex)
        {
            System.out.println(ex.toString());
        }
    }
    
    private void close_udp_socket(SocketAddress remote_address)
    {
        // Todo: delete the remote address from the client table.
    }
    
    private void write_tcp_data(SocketAddress remote_address, byte[] data, int length) throws IOException
    {
        Socket soc = socket_map.get(remote_address).toSocket();
        new DataOutputStream(soc.getOutputStream()).write(data, 0, length);
    }
    
    private void write_udp_data(SocketAddress remote_address, byte[] data, int length)
            throws IOException, UnknownHostException
    {
        DatagramPacket  data_packet;

        data_packet = new DatagramPacket(data, length, remote_address);
        this.udp_server_socket.send(data_packet);
    }
    
    private boolean put_new_soc_entry(SocketAddress soc_addr, SocketGroup soc_group)
    {
        boolean ret_val = false;
        
        if (false == this.socket_map.containsKey(soc_addr))
        {
            this.socket_map.put(soc_addr, soc_group);
            ret_val = true;
        }
        
        return ret_val;
    }
}
