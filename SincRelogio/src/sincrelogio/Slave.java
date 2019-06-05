package sincrelogio;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

class Slave
{
    SimpleDateFormat formato;
    
    String ip;
    Date tempo;
    String logFile;
    long passo;
    
    Slave(String ip, String tempo, String logFile) throws ParseException{
        
        //define o formato da hora
        formato = new SimpleDateFormat("HH:mm:ss");
        
        //pega um valor de 1 a 3 que será o passo do relogio por rodada
        Random random = new Random();
        int nAleatorio = random.nextInt(2) + 1;
        
        this.ip = ip;
        this.tempo = formato.parse(tempo);
        this.logFile = logFile;
        this.passo = nAleatorio * 60000;
    }
    
    public boolean isNumeric(String str) { 
        //verifica se a string é um numero
        
        try {  
            Double.parseDouble(str);  
            return true;
        } catch(NumberFormatException e){  
            return false;  
        }  
    }
    
    private long converterSegundos(long tempo){
        //converte o long para segundos
        return (tempo / 1000) * -1;
    }
    
    private void escreverLog(String texto, String arquivo, Boolean append) throws IOException{
        //escreve o log no arquivo
        
        if(!append){
            PrintWriter writer = new PrintWriter(arquivo);
            writer.print("");
            writer.close();
        }
        
        Files.write(Paths.get(arquivo), texto.getBytes(), StandardOpenOption.APPEND);      
        
    }
    
    public void iniciar() throws Exception
    {
        //pega o separador de linha
        String newline = System.getProperty("line.separator");
        
        //cria um servidor com os dados passados pela linha de comando
        String[] ipport = ip.split(":");
        DatagramSocket serverSocket = new DatagramSocket(Integer.parseInt(ipport[1]));
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];
        
        System.out.println("Server Ligado");

        //define a rodada
        int cont = 1;
        
        escreverLog(newline + "Acrescimo por rodada (segundos): " + converterSegundos(this.passo) * -1 + newline, this.logFile, false); 
        
        while(true){
            
            receiveData = new byte[1024];
            sendData = new byte[1024];

            //Espera o recebimento da mensagem do Master
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            
            //Transforma a mensagem recebida em string
            String sentence = new String( receivePacket.getData());
         
            //se o recebido for um numero significa que é o inicio de uma rodada e pode se iniciar o resto da comunicacao
            if(isNumeric(sentence.trim())){
                
                escreverLog(newline + "Rodada: " + sentence.trim() + newline, this.logFile, true); 
                escreverLog("Hora: " + formato.format(this.tempo) + newline, this.logFile, true); 
                
                System.out.println("\n\nRodada: " + cont);
                System.out.println("\nHora Local: " + formato.format(this.tempo));
                
                //Pega o endereço e porta do master
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();

                //Envia a hora do slave
                sendData = formato.format(this.tempo).getBytes();
                DatagramPacket sendPacket =
                new DatagramPacket(sendData, sendData.length, IPAddress, port);
                serverSocket.send(sendPacket);

                receiveData = new byte[1024];
                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                
                //Espera o recebimento do ajuste a ser feito
                serverSocket.receive(receivePacket);

                //Transforma a mensagem recebida em string
                sentence = new String( receivePacket.getData());
                
                //pega a diferenca a ser aplicada na hora local
                long novaDiferencaMaster = Long.parseLong(sentence.trim());
                System.out.println("Diferenca Novo Master(segundos): " + converterSegundos(novaDiferencaMaster));
                escreverLog("Diferenca Novo Master(segundos): " + converterSegundos(novaDiferencaMaster) + newline, this.logFile, true); 

                //Ajusta a hora local
                this.tempo = new Date(this.tempo.getTime() - novaDiferencaMaster);

                escreverLog("Nova hora: " + formato.format(this.tempo) + newline, this.logFile, true); 
                System.out.println("Nova hora: " + formato.format(this.tempo));

                //acrescenta o passo na hora local
                this.tempo = new Date(this.tempo.getTime() + this.passo);
                
                cont ++;
            }
        }
    }
}