package klient;
import java.io.*;
import java.net.*;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.util.*;

public class klient_txt
{
    static wiadomosc txt = new wiadomosc();
    private static InetAddress host;
    private static final int PORT = 1111;
    
    private static DatagramSocket datagramSocket;
    private static DatagramPacket inPacket,outPacket;
    
    public static byte[] buffer_in = new byte[256];
    public static byte[] buffer_out = new byte[256];
    static public byte[] packet = new byte[256];
    public static boolean no_entry = false;

    public static int attempts;
    
    static String oper = "0";
    static String odp = "0";
    static String id = "0";
    static String liczba = "0";
    static String czas = "0";

    public static void main(String[] args)
    {
        try
        {
            byte[] ipAddr = new byte[] {(byte)169, (byte)254, (byte)0, (byte)51};
            host = InetAddress.getByAddress(ipAddr);
        }
        catch(UnknownHostException uhEx)
        {
           System.out.println("Nie znaleziono ID hosta... ");
           System.exit(1);
        }
        
        serwer();
    }

    private static void serwer()
    {    
        try 
        {
            datagramSocket = new DatagramSocket();
            Scanner scanner = new Scanner(System.in);
            int number;  
            
            oper = txt.POWITANIE;
            odp = txt.OK;
            id = txt.FULL;
            liczba = txt.NUM;
            
            //WYSLANIE POWITALNEGO PAKIETU
            pack_in();
            outPacket = new DatagramPacket(buffer_out, buffer_out.length, host, PORT);
            datagramSocket.send(outPacket);
            
            //OTRZYMANIE PAKIETU Z ID;
            buffer_in = new byte[256];
            inPacket = new DatagramPacket(buffer_in, buffer_in.length);
            datagramSocket.receive(inPacket);
            unpack();
            
            if(id.equals(txt.FULL))
            {
                System.out.println("MAX 2 KLIENTOW");
                datagramSocket.close();
                System.exit(0);
            }

            System.out.println("Twoje ID: " + id);
            
            number = 1;
            while((number % 2 == 1) || (number > 31) || (number < 1))
            {
                System.out.print("\nPodaj dowolna liczbe parzysta L (od 2 do 30): ");
                number = scanner.nextInt();
            }
            
            oper = txt.LICZBA;
            odp = txt.OK;
            liczba = Integer.toString(number);
            
            //WYSLANIE PAKIETU Z LICZBA
            pack_in();
            outPacket = new DatagramPacket(buffer_out, buffer_out.length, host, PORT);
            datagramSocket.send(outPacket);
                
            //OTRZYMANIE PAKIETU Z LICZBA PROB
            inPacket = new DatagramPacket(buffer_in, buffer_in.length);
            datagramSocket.receive(inPacket);
            unpack();
                
            attempts = Integer.parseInt(liczba);
            System.out.println("\n\nLiczba prob wynosi: " + attempts);
            
            //ZGADYWANIE LICZBY TAJNEJ
            for (int i=0; i<attempts; i++) 
            {
                number = -1;
                while((number > 31) || (number < 0))
                {
                    System.out.print("Podaj liczbe (od 0 do 31): ");
                    number = scanner.nextInt();
                }
                
                //WYSLANIE PAKIETU Z LICZBA
                liczba = Integer.toString(number);
                pack_in();
                outPacket = new DatagramPacket(buffer_out, buffer_out.length, host, PORT);
                datagramSocket.send(outPacket);

                //ODEBRANIE PAKIETU Z WYNIKIEM ZGADYWANIA   
                inPacket = new DatagramPacket(buffer_in, buffer_in.length);
                datagramSocket.receive(inPacket);
                unpack();
                System.out.println("oper: " + oper);
                System.out.println("odp: " + odp);

                //WYGRANA
                if (oper.equals(txt.WYNIK) && odp.equals(txt.WYGR)) {
                    System.out.println("\nGratulacje! Wygrales.");
                    break;
                }

                //PRZECIWNIK WYGRAL
                else if (oper.equals(txt.WYNIK) && odp.equals(txt.PRZEGR)) {
                    System.out.println("\nNiestety przeciwnik byl lepszy. Przegrales 😞");
                    break;
                }

                //BLEDNA ODPOWIEDZ
                else if (oper.equals(txt.WYNIK) && odp.equals(txt.BLAD)) {
                    
                    if(i == attempts - 1)
                    {
                        System.out.println("\nWykorzystales wszystkie proby");
                    }
                    else{
                        System.out.println("\nPudlo! Probuj dalej.");
                    }
                }
            }
        }
        catch(IOException ioEx) {
            ioEx.printStackTrace();
        }
        finally
        {
            System.out.println("\nZamykanie polaczenia... ");
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