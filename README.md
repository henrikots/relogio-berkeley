# relogio-berkeley
Implementação do Relogio de Berkeley para Sistemas Distribuido em java, utilizando Threads e comunicação com o protocolo UDP.

# Arquivos:

AMOSTRA.docx: esse arquivo contém os casos de testes realizados para validar o programa. Foi feito teste de conexão em sistemas diferentes(windows e linux), teste de consistência do master diante de falhas dos slaves e validação do resultado dos ajustes.

PROJETO.docx: aqui é explicado como todo o programa funciona, quais são e como funcionam suas classes e como é a interação com os arquivos externos

Pasta logs: Onde todos os os logs vão ser armazenados. toda vez que o programa for reiniciado os logs são apagados e reescritos.

Pasta executar: Contém os arquivos de execução configurável do sistema.

Pasta SincRelogio: Contém os arquivos de código fonte, arquivo slaves txt que armazena todos os ips dos slaves que o master tem que coordenar e na pasta dist fica armazenado o ‘.jar’ do projeto.
 

# Execução:

Para iniciar o master  deve ser utilizado o código abaixo na linha de comando:

Na pasta raiz:

java -jar "SincRelogio\dist\SincRelogio.jar" "-m" "IP:PORTA" "HH:mm:ss" "HH:mm:ss" "SincRelogio\slaves.txt" "logs\nome_arquivo_log.txt"

Sendo o primeiro argumento “-m” a identificação de que será iniciado um master, "IP:PORTA" do master, "HH:mm:ss" hora do master, "HH:mm:ss" tolerância da diferença de hora, "..\SincRelogio\slaves.txt" arquivo que possuem todos os ips dos slaves e "..\logs\nome_arquivo_log.txt" que será gravado toda as interações do master.

Para iniciar os slaves deve ser utilizado o código abaixo na linha de comando:

Na pasta raiz:

 java -jar "SincRelogio\dist\SincRelogio.jar" "-s" "IP:PORTA" "HH:mm:ss" "logs\nome_arquivo_log.txt"	

O primeiro argumento “-s” define que será instanciado um relógio slave,  "IP:PORTA"  com o ip e porta do relógio, “HH:mm:ss” com a hora local do relógio e "..\logs\nome_arquivo_log.txt" como o nome do arquivo que será salvo os logs do slave.

Para cada slave que for iniciado seu IP:PORTA deve estar também no “slaves.txt”.
