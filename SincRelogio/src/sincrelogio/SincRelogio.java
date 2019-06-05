/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sincrelogio;

public class SincRelogio {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        
        String tipo = args[0];
        
        
        if (tipo.equals("-m")){
            //instancia um master
            
            String ip = args[1];
            String tempo = args[2];
            String d = args[3];
            String slavesfile = args[4];
            String logfile = args[5];
                        
            Master master = new Master(ip, tempo, d, slavesfile, logfile);
            master.iniciar();
            
        } else if(tipo.equals("-s")){
            //instancia um slave
            
            String ip = args[1];
            String tempo = args[2];
            String logfile = args[3];
            
            Slave slave = new Slave(ip, tempo, logfile);
            slave.iniciar();
            
        }
        
    }
    
}
