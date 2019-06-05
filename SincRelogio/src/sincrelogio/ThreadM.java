/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sincrelogio;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ThreadM {
    
    String ip;
    String porta;
    long diferenca;
    SimpleDateFormat formato;
    
    
    public ThreadM(String endereco){
        
        //define o formato da hora
        this.formato = new SimpleDateFormat("HH:mm:ss");
        String end[] = endereco.split(":");
        
        //define as variáveis locais
        this.ip = end[0];
        this.porta = end[1];
        this.diferenca = Long.MIN_VALUE;
        
    }
    
    public void reiniciar(){
        //define a diferencia default como o minimo valor de long possível
        //usado para determinar se foi realizado a comunicacao com o slave
        this.diferenca = Long.MIN_VALUE;
    }

    
    public void buscar_diferenca(String horaMaster, int rodada) throws InterruptedException{
    //busca a hora local do slave e calcula a diferenca com a hora do master
        
        //define uma variável local para que a thread salve a diferenca localmente
        long diferenca[] = {Long.MIN_VALUE};
        
        //cria a thread
        Thread t1 = new Thread(){
          
            @Override
            public void run(){
                
                DatagramSocket clientSocket;
                InetAddress IPAddress;
                
                try{
                    
                    //define o cliente UDP
                    clientSocket = new DatagramSocket();
                    IPAddress = InetAddress.getByName(ip);

                    //buffers
                    byte[] sendData = new byte[1024];
                    byte[] receiveData = new byte[1024];

                    //pega a rodada do master para passar para o slave
                    String sentence = Integer.toString(rodada);
                    sendData = sentence.getBytes();

                    //prepara o pacote e envia
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Integer.parseInt(porta));
                    clientSocket.send(sendPacket);

                    //recebe a hora do slave
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    clientSocket.receive(receivePacket);

                    //transforma a mensagem recebida em string
                    String modifiedSentence = new String(receivePacket.getData());
                    
                    //transforma as horas em long e faz a subtracao das horas
                    long diferencaM = ((formato.parse(horaMaster).getTime() - formato.parse(modifiedSentence.trim()).getTime())) * -1;
                    
                    //salva localmente a diferenca
                    diferenca[0] = diferencaM;

                    clientSocket.close();
                
                }catch (SocketException ex) {
                    Logger.getLogger(Master.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnknownHostException ex) {
                    Logger.getLogger(Master.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Master.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ParseException ex) {
                    Logger.getLogger(ThreadM.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
            
        };
        
        //inicia a thread
        t1.start();
        
        //espera 3 segundos
        TimeUnit.SECONDS.sleep(3);
        
        //salva a diferenca
        this.diferenca = diferenca[0];
        
    }
    
    public void retornarAjuste(long diferencaM){
    //retorna para o slave a diferenca definida pelo master 
        
        Thread t2 = new Thread(){
          
            @Override
            public void run(){
        
                DatagramSocket clientSocket;
                InetAddress IPAddress;

                try {
                    
                    //cria o cliente
                    clientSocket = new DatagramSocket();
                    IPAddress = InetAddress.getByName(ip);

                    //Declara as variáveis
                    byte[] sendData = new byte[1024];

                    //pega a diferenca e transforma em string
                    sendData = String.valueOf(diferencaM).getBytes();

                    //Prepara o pacote e envia para o Slave
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Integer.parseInt(porta));
                    clientSocket.send(sendPacket);

                    clientSocket.close();

                } catch (SocketException ex) {
                    Logger.getLogger(Master.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnknownHostException ex) {
                    Logger.getLogger(Master.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Master.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        
        //inicia a thread.
        t2.start();
        
    }
    
    public long getDiferenca(){
        return this.diferenca;
    }
    
}