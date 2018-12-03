package serwer;

import java.net.*;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.util.BitSet;
import java.util.Random;

public class serwer_txt {

    private static final int PORT = 1111;
    private static DatagramSocket datagramSocket;
    private static DatagramPacket inPacket, outPacket;
    
    static public byte[] packet = new byte[256];
    public static byte[] buffer_in = new byte[256];
    public static byte[] buffer_out = new byte[256];
    public static boolean if_end = false;
    
    static String id1;
    static String id2;

    static int rand_id1, rand_id2;
    static int secret_number;
    static int attempts = 0;

    static String oper = "0";
    static String odp = "0";
    static String id = "0";
    static String liczba = "0";
    static String czas = "0";
    
    public static int counter = 0;

    public static void main(String [] args)
    {
        pack_in();
        System.out.println("Oczekiwanie na graczy... ");
        
        try 
        {
            datagramSocket = new DatagramSocket(PORT);
        }catch(SocketException e)
        {
            System.out.println("Nie mozna skorzystac z tego portu.");
            System.exit(1);
        }
        client();
    }
    
    public static void client()
    {
        wiadomosc txt = new wiadomosc();
        Random generator = new Random();
        rand_id1 = generator.nextInt(14) + 1;

        do {
            rand_id2 = generator.nextInt(14) + 1;
        } while(rand_id2 == rand_id1);
        
        //DOBRZE
        id1 = Integer.toString(rand_id1);
        id2 = Integer.toString(rand_id2);
        
        int clientPort;
        int clientPort1 = 0;
        
        try { 
            InetAddress clientAddress = null;
            InetAddress clientAddress1 = null;
            
            secret_number = generator.nextInt(30) + 1;
            
            System.out.println("\nWylosowana liczba tajna: " + secret_number);
            
            do 
            {
                //CZEKANIE NA DWOCH KLIENTOW
                if(counter == 0 || counter == 1)
                {
                    //OTRZYMANIE PIERWSZEGO PAKIETU
                    inPacket = new DatagramPacket(buffer_in, buffer_in.length);
                    datagramSocket.receive(inPacket);
                    clientAddress = inPacket.getAddress();
                    clientPort = inPacket.getPort();
                    counter++; 
                    
                    //PIERWSZY KLIENT
                    if(counter == 1)
                    {
                        System.out.println("\nPolaczono z adresem: " + clientAddress + "\nWygenerowane ID: " + id1);
                        clientAddress1 = inPacket.getAddress();
                        clientPort1 = inPacket.getPort();
                    }
                    //DRUGI KLIENT
                    else if(counter == 2)
                    {
                        System.out.println("\nPolaczono z adresem: " + clientAddress + " \nWygenerowane ID: " + id2);
                    }
                    unpack();
                    
                    oper = txt.POWITANIE;
                    odp = txt.OK;
                    if(counter == 1)
                    {
                        id = id1;
                        liczba = txt.NUM;
                    }
                    else if(counter == 2)
                    {
                        id = id2;
                        liczba = txt.NUM;
                    }
                    
                    //WYSLANIE PAKIETU Z ID
                    pack_in();
                    display_packet();
                    System.out.print("PAKIET: ");
                    display_packet();
                    outPacket = new DatagramPacket(buffer_out, buffer_out.length, clientAddress, clientPort);
                    datagramSocket.send(outPacket);
                    
                    //ODEBRANIE PAKIETU Z LICZBA L1 I L2
                    inPacket = new DatagramPacket(buffer_in, buffer_in.length);
                    datagramSocket.receive(inPacket);
                    clientAddress = inPacket.getAddress();
                    clientPort = inPacket.getPort();
                    unpack();

                    attempts += Integer.parseInt(liczba);
                    
                    //WYZNACZENIE LICZBY PROB I PRZESLANIE INFORMACJI DO OBU KLIENTOW
                    if(counter == 2)
                    {
                        attempts = attempts / 2;
                        System.out.println("\nLiczba prob zostala ustalona: " + attempts);
                        
                        //DRUGI KLIENT
                        liczba = Integer.toString(attempts);
                        pack_in();
                        outPacket = new DatagramPacket(buffer_out, buffer_out.length, clientAddress, clientPort);
                        datagramSocket.send(outPacket);
                        
                        //PIERWSZY KLIENT
                        id = id1;
                        liczba = Integer.toString(attempts);
                        pack_in();
                        outPacket = new DatagramPacket(buffer_out, buffer_out.length, clientAddress1, clientPort1);
                        datagramSocket.send(outPacket);
                    }
                }
                //JESLI DWOCH KLIENTOW JEST JUZ POLACZONYCH
                else
                {
                    inPacket = new DatagramPacket(buffer_in, buffer_in.length);
                    datagramSocket.receive(inPacket);
                    clientAddress = inPacket.getAddress();
                    clientPort = inPacket.getPort();
                    unpack();
                    
                    if(oper.equals(txt.POWITANIE) && id.equals(txt.FULL) && liczba.equals(txt.NUM) && odp.equals(txt.OK))
                    {
                        id = txt.FULL;
                        pack_in();
                        outPacket = new DatagramPacket(buffer_out, buffer_out.length, clientAddress, clientPort);
                        datagramSocket.send(outPacket);
                    }
                    
                    if(id.equals(txt.FULL))
                    {
                        System.out.println("\nKTOS PROBOWAL SIE POLACZYC");
                        liczba = Integer.toString(-1);
                    }
                    else
                    {
                        System.out.println("KLIENT o ID " + id + ": " + liczba);
                    }
                    
                    //ODSYLANIE KLIENTOM INFORMACJI O POPRAWNOSCI
                    if(Integer.parseInt(liczba) == secret_number)
                    {
                        if(if_end == false)
                        {
                            oper = txt.WYNIK;
                            odp = txt.WYGR;
                            pack_in();
                            outPacket = new DatagramPacket(buffer_out, buffer_out.length, clientAddress, clientPort);
                            datagramSocket.send(outPacket);
                            System.out.println("\nGra zakonczona. Wygral klient o ID: " + id);
                            if_end = true;
                        }
                        else
                        {
                            oper = txt.WYNIK;
                            odp = txt.PRZEGR;
                            pack_in();
                            outPacket = new DatagramPacket(buffer_out, buffer_out.length, clientAddress, clientPort);
                            datagramSocket.send(outPacket);
                            break;
                        }
                    }
                    else
                    {
                        oper = txt.WYNIK;
                        odp = txt.BLAD;
                        pack_in();
                        outPacket = new DatagramPacket(buffer_out, buffer_out.length, clientAddress, clientPort);
                        datagramSocket.send(outPacket);
                    }
                }
            } while(true);
      
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("\n Zamykanie polaczenia...");
            datagramSocket.close();
        }
    }

    public static void pack_in()
    {
        DecimalFormat df2 = new DecimalFormat("00");
        LocalTime local_t = LocalTime.now();
        String pom;
        czas = df2.format(local_t.getHour()) + ":" + df2.format(local_t.getMinute()) + ":" + df2.format(local_t.getSecond());
        pom = "Czas>" + czas + "<Operacja>" + oper + "<Odpowiedz>" + odp + "<Identyfikator>" + id + "<Liczba>" + liczba + "<";
        int x = pom.length();
        buffer_out = new byte[x];
        buffer_out = pom.getBytes();
    }

    public static void unpack()
    {
        String pom = new String(inPacket.getData(), 0, inPacket.getLength());
        
        czas = pom.substring(pom.indexOf(">") + 1, pom.indexOf("<"));
        pom = pom.substring(pom.indexOf("<") + 1, pom.length()); 
        
        oper = pom.substring(pom.indexOf(">") + 1, pom.indexOf("<"));
        pom = pom.substring(pom.indexOf("<") + 1, pom.length());
        
        odp = pom.substring(pom.indexOf(">") + 1, pom.indexOf("<"));
        pom = pom.substring(pom.indexOf("<") + 1, pom.length());
        
        id = pom.substring(pom.indexOf(">") + 1, pom.indexOf("<"));
        pom = pom.substring(pom.indexOf("<") + 1, pom.length());
        
        liczba = pom.substring(pom.indexOf(">") + 1, pom.indexOf("<"));
    }

    public static void display_packet()
    {
        String pom;
        pom = "&czas>>" + czas + "<<operacja>>" + oper + "<<odpowiedz>>" + odp + "<<identyfikator>>" + id + "<<liczba>>" + liczba + "&";
        System.out.println(pom);
    }
    
    public static void reset_packet()
    {
        oper = "null";
        odp = "null";
        id = "null";
        liczba = "null";
    }

    public static byte[] toByteArray(BitSet bits)
    {
        byte[] bytes = new byte[(bits.length() + 7) / 8];
        for (int i=0; i<bits.length(); i++) {
            if (bits.get(i)) {
                bytes[bytes.length-i/8-1] |= 1<<(i%8);
            }
        }
        return bytes;
    }

    public static BitSet fromByteArray(byte[] bytes)
    {
        BitSet bits = new BitSet();
        for (int i = 0; i < bytes.length * 8; i++) {
            if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
                bits.set(i);
            }
        }
        return bits;
    }
}