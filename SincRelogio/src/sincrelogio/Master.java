package sincrelogio;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.logging.Level;
import java.util.logging.Logger;

class Master
{
    
    SimpleDateFormat formato;
   
    String ip;
    Date tempo;
    long d;
    String slavesfile;
    String logfile;
    long passo;
    

    Master(String ip, String tempo, String d, String slavesfile, String logfile) throws ParseException {
        
        //formato da hora
        formato = new SimpleDateFormat("HH:mm:ss");
        
        this.ip = ip;
        this.tempo = formato.parse(tempo);
        
        //calcula a diferenca tolerada em long
        int segD = this.tempo.getSeconds() + formato.parse(d).getSeconds();
        
        int minD = this.tempo.getMinutes() + formato.parse(d).getMinutes();
        if (segD > 59){
            minD = minD + 1;
        }
        
        int horD = this.tempo.getHours();
        if (minD > 59){
            horD = horD + 1;
        }
        if (horD > 23){
            horD = 0;
        }
        
        this.d = formato.parse(horD + ":" + minD + ":" + segD).getTime() - this.tempo.getTime();
        
        //variaveis locais
        this.slavesfile = slavesfile;
        this.logfile = logfile;
        this.passo = 60000;
    }
    
    private long converterSegundos(long tempo){
        return tempo / 1000;
    }
    
    private void escreverLog(String texto, String arquivo, Boolean append) throws IOException{
        
        if(!append){
            PrintWriter writer = new PrintWriter(arquivo);
            writer.print("");
            writer.close();
        }
        Files.write(Paths.get(arquivo), texto.getBytes(), StandardOpenOption.APPEND);      
        
    }
    
   public void iniciar() throws Exception
    {
        
        Vector slavesDados = new Vector();

        //lê o arquivo de slaves
        FileReader arq = new FileReader(this.slavesfile);
        BufferedReader lerArq = new BufferedReader(arq);

        String linha = lerArq.readLine(); 

        int cont_slaves = 0;

        //le todos os slaves e define a quantidade de slaves
        do{
            slavesDados.add(linha);
            linha = lerArq.readLine();
            cont_slaves ++;
        }while (linha != null);

        //fecha o arquivo
        arq.close(); 

        long diferenca[] = new long[cont_slaves];
        long ajuste[] = new long[cont_slaves];
        
        //sao instancias thread para cada slave
        ThreadM Slaves[] = new ThreadM[cont_slaves];
        
        for (int i = 0; i < cont_slaves; i++){
            diferenca[i] = Long.MIN_VALUE;
            Slaves[i] = new ThreadM(slavesDados.get(i).toString());
        }
        
        //separador de linha para o log
        String newline = System.getProperty("line.separator");
        
        escreverLog("Acrescimo por rodada(segundos): " + converterSegundos(this.passo) + newline , this.logfile, false);
        
        int cont = 1;
        
        while(true){
            
            //espera 3 segundos para comecar
            TimeUnit.SECONDS.sleep(3);
            
            escreverLog(newline + "RODADA : " + cont + newline, this.logfile, true); 
            escreverLog("Hora Master: " + formato.format(this.tempo) + newline, this.logfile, true); 
            System.out.println("\n\nHora Master: " + formato.format(this.tempo));

            //reinicia a thread e busca a diferenca do slave
            for (int i = 0; i < cont_slaves; i++){
                diferenca[i] = Long.MIN_VALUE;
                ajuste[i] = 0;
                Slaves[i].reiniciar();
                Slaves[i].buscar_diferenca(formato.format(tempo), cont);
                
                diferenca[i] = Slaves[i].getDiferenca();
                escreverLog("Diferenca Relogio " + i + " (segundos): " + converterSegundos(diferenca[i]) + newline, this.logfile, true); 
                System.out.println("Diferenca Relogio " + i + " (segundos): " + converterSegundos(diferenca[i]));
            }


            long delta = 0;
            int cont_maquinas = 0;

            //Calcula o Delta e determina quais máquinas estão ligadas
            for(int i = 0; i < cont_slaves; i++){
                if(((diferenca[i] < d  && diferenca[i] > 0) || (diferenca[i] > d  && diferenca[i] < 0))   && diferenca[i] != Long.MIN_VALUE){
                    delta += diferenca[i];
                    cont_maquinas ++;
                }
            }

            delta = delta / (cont_maquinas + 1);

            escreverLog("Delta (segundos): " + converterSegundos(delta) + newline, this.logfile, true); 
            System.out.println("Delta (segundos): " + converterSegundos(delta));

            //Ajusta a hora com o delta
            this.tempo = new Date(tempo.getTime() + delta);

            escreverLog("Nova Hora Master: " + formato.format(this.tempo) + newline, this.logfile, true); 
            System.out.println("Nova Hora Master: " + formato.format(this.tempo));

            //retorna as diferencas de horas
            for(int i = 0; i < cont_slaves; i++){

                if(diferenca[i] != Long.MIN_VALUE){
                    ajuste[i] = diferenca[i] - delta;
                    Slaves[i].retornarAjuste(ajuste[i]);
                    escreverLog("Ajuste Thread1 " + i + " (segundos): " + converterSegundos(ajuste[i]) + newline, this.logfile, true); 
                    System.out.println("Ajuste Thread " + i + " (segundos): " + converterSegundos(ajuste[i]));
                }

            }
            
            cont ++;
            //atualiza a passada do tempo pela rodada
            this.tempo = new Date(this.tempo.getTime() + this.passo);
        }

    }
}