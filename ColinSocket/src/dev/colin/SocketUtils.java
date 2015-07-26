/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.colin;

import java.io.IOException;
import java.net.*;

/**
 * Defines the utility functions for network connection.
 * @author colin
 */
public class SocketUtils {
    /**
     * Combine InetAddress and port to the format xxx.xxx.xxx.xxx:ppppp;
     * @param  addr the address.
     * @param  port the port number.
     * @return the string in the format xxx.xxx.xxx.xxx:ppppp
     */
    public static String combine_ip_and_port(InetAddress addr, int port)
    {
        /**
         * We get substring(1) is to delete the prefix "/" in the
         * InetAddrss.
         */
        return addr.toString().substring(1) + ":" + Integer.toString(port);
    }
    
    public static InetAddress get_inet_in_host(String host) throws java.net.UnknownHostException
    {
        int index = host.indexOf(":");
        
        if (index != -1)
        {
            return InetAddress.getByName(host.substring(0, index));
        }
        else
        {
            return null;
        }
    }
    
    public static int get_port_in_host(String host)
    {
        String  str_port;
        int     index;
        int     port = 0;
        
        index = host.indexOf(":");
        
        if (index != -1)
        {
            str_port = host.substring(index + 1);
            port = Integer.parseInt(str_port);
        }
        
        return port;
    }
    
    /**
     * Convert a address in SocketAddress type into a string address in the
     * format with the "/" at the beginning of a SocketAddress address.
     * @param address the address in SocketAddress.
     * @return an address in the string without "/" at the beginning.
     */
    public static String socket_address_2_string(SocketAddress address)
    {
        /**
         * We get substring(1) is to delete the prefix "/" in the
         * SocketAddress.
         */
        return address.toString().substring(1);
    }
    
    /**
     * Convert a address in String without "/" into a address in SocketAddress.
     * @param address the address in String without "/".
     * @return an address in SocketAddress.
     * @throws java.net.UnknownHostException inherited from the function
     * getByName of the class INetAddress.
     */
    public static SocketAddress string_2_socket_address(String address)
            throws java.net.UnknownHostException
    {
        InetAddress inet_addr = get_inet_in_host(address);
        int port = get_port_in_host(address);
        
        return new InetSocketAddress(inet_addr, port);
    }
    
    /**
     * Check whether the input address is an IP address in xxx.xxx.xxx.xxx
     * @param addr  the input address string.
     * @return true if the input address is an IP address and false if the input
     * address is not an IP address.
     */
    public static boolean check_ip_address(String addr)
    {
        boolean ret_val = true;
        int parse_len = 0;
        int byte_count;
        
        for (byte_count = 0; byte_count < MAX_IPV4_ADDR_LEN; byte_count++)
        {
            int dot_value = 0;
            int i = 0;
            
            while (parse_len < addr.length())
            {
                String ch = addr.substring(parse_len, parse_len + 1);
                
                parse_len++; /*Prepare for the next loop*/
                if (ch.equals("."))
                {
                    /**
                     * break the loop to find the next "." if it is necessary.
                     */
                    break;
                }
                if (Character.isDigit(ch.charAt(0)))
                {
                    dot_value = dot_value * 10 + Integer.parseInt(ch);
                    if (dot_value > 255)
                    {
                        /**
                         * For the integer between tow dot, it is not necessary
                         * to be greater than 255.
                         */
                        return false;
                    }
                }
                else
                {
                    /**
                     * All characters should be digital except ".".
                     */
                    return false;
                }
            }
        }
        
        if (parse_len < addr.length())
        {
            /**
             * This means the string wasn't parsed over, so it should be not
             * a valid IP address.
             */
            ret_val = false;
        }
        
        return ret_val;
    }
    
    /**
     * Create a socket to connect the TCP server in the specified IP address and port.
     * @param addr IP address of the remote end. It should be IP address.
     * @param port Port number of the remote end.
     * @return the Socket object if we create the TCP socket successfully. If
     * create socket object failed, return null.
     * @throws IOException inherited from the construct function of Socket.
     */
    public static Socket connect_tcp_server(String addr, int port) throws IOException
    {
        Socket ret_soc = null;
        
        if (check_ip_address(addr) && (port > 0) && (port < 0x10000))
        {
            ret_soc = new Socket(addr, port);
        }
        
        return ret_soc;
    }
    
    public static DatagramSocket connect_udp_server(String addr, int port)
            throws UnknownHostException, SocketException
    {
        DatagramSocket  ret_soc = null;
        InetAddress     inet_addr;
        
        if (check_ip_address(addr) && (port > 0) && (port < 0x10000))
        {
            inet_addr = InetAddress.getByName(addr);
            ret_soc   = new DatagramSocket();
            
            ret_soc.connect(inet_addr, port);
        }
        
        return ret_soc;

    }
        
    /**
     * TCP_TYPE_NAME is the constant string name for TCP type sever, it is "TCP".
     */
    public static final String TCP_TYPE_NAME = "TCP";
    /**
     * UDP_TYPE_NAME is the constant string name for UDP type sever, it is "UDP".
     */
    public static final String UDP_TYPE_NAME = "UDP";

    /**
     * The maximum length for each received TCP or UDP packet.
     */
    public static final int MAX_PACKET_LEN = 1500;
    
    /**
     * The maximum length in byte of IPv4 address. It is 4.
     */
    public static final int MAX_IPV4_ADDR_LEN = 4;
}
